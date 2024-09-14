package me.him188.ani.app.torrent.io

import kotlinx.coroutines.test.runTest
import me.him188.ani.app.torrent.api.pieces.Piece
import me.him188.ani.app.torrent.api.pieces.PieceState
import me.him188.ani.app.torrent.api.pieces.lastIndex
import me.him188.ani.utils.io.readAllBytes
import me.him188.ani.utils.io.readBytes
import me.him188.ani.utils.io.readExactBytes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal const val sampleText =
    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
internal val sampleTextByteArray = sampleText.toByteArray()


/**
 * @see OffsetTorrentInputTest
 */
internal sealed class TorrentInputTest {
    class NoShift : TorrentInputTest() {
        override val logicalPieces = Piece.buildPieces(sampleTextByteArray.size.toLong(), 16)

        @Test
        fun seekReadLastPiece() = runTest {
            logicalPieces.last().state.emit(PieceState.FINISHED)
            input.seek(logicalPieces.last().offset + 2)
            input.readBytes().decodeToString().run {
                assertEquals("Lorem Ipsum.", this)
                assertEquals(sampleTextByteArray.size % 16 - 2, length)
            }
        }
    }

    class WithShift : TorrentInputTest() {
        override val logicalPieces = Piece.buildPieces(sampleTextByteArray.size.toLong(), 16, initial = 1000)

        @Test
        fun seekReadLastPiece() = runTest {
            logicalPieces.last().state.emit(PieceState.FINISHED)
            input.seek(logicalPieces.last().offset - 1000 + 2)
            input.readBytes().run {
                assertEquals("Lorem Ipsum.", String(this))
                assertEquals(sampleTextByteArray.size % 16 - 2, size)
            }
        }
    }

    class LargerPieces : TorrentInputTest() {
        override val logicalPieces = Piece.buildPieces(sampleTextByteArray.size.toLong(), 16, initial = 1000).let {
            it + Piece(it.size, 16, it.last().lastIndex + 1)
        }

        @Test
        fun seekReadLastPiece() = runTest {
            logicalPieces.dropLast(1).last().state.emit(PieceState.FINISHED)
            input.seek(logicalPieces.dropLast(1).last().offset - 1000 + 2)
            input.readBytes().run {
                assertEquals("Lorem Ipsum.", String(this))
                assertEquals(sampleTextByteArray.size % 16 - 2, size)
            }
        }
    }

    @TempDir
    lateinit var tempDir: File

    protected abstract val logicalPieces: List<Piece>

    private val tempFile by lazy {
        tempDir.resolve("test.txt").apply {
            parentFile.mkdirs()
            writeText(sampleText)
        }
    }

    private val bufferSize = 20
    protected val input: TorrentInput by lazy {
        TorrentInput(
            RandomAccessFile(tempFile, "r"),
            logicalPieces,
            bufferSize = bufferSize,
        )
    }

    @AfterEach
    fun afterTest() {
        input.close()
    }

    @Test
    fun findPiece() {
        assertEquals(0, input.findPieceIndex(0))
        assertEquals(0, input.findPieceIndex(2))
        assertEquals(0, input.findPieceIndex(15))
        assertEquals(1, input.findPieceIndex(16))
        assertEquals(1, input.findPieceIndex(20))
        assertEquals(sampleTextByteArray.size / 16, input.findPieceIndex(sampleTextByteArray.lastIndex.toLong()))
    }

    @Test
    fun readFirstPieceNoSuspend() = runTest {
        logicalPieces.first().state.emit(PieceState.FINISHED)
        input.readBytes().run {
            assertEquals(16, size)
            assertEquals("Lorem Ipsum is s", String(this))
        }
        assertEquals(16L, input.position)
    }

    // TODO: TorrentInputTest has been disabled because we changed `seek` from suspend to blocking to improve speed

//    @Test
//    fun readFirstPieceSuspendResume() = runTest {
//        launch(start = CoroutineStart.UNDISPATCHED) {
//            yield()
//            logicalPieces.first().state.emit(PieceState.FINISHED)
//        }
//        file.readBytes().run {
//            assertEquals(16, size)
//            assertEquals("Lorem Ipsum is s", String(this))
//        }
//        assertEquals(16L, file.offset)
//    }

    @Test
    fun seekFirstNoSuspend() = runTest {
        logicalPieces.first().state.emit(PieceState.FINISHED)
        input.seek(1)
        assertEquals(1L, input.position)
    }

//    @Test
//    fun seekFirstSuspend() = runTest {
//        launch(start = CoroutineStart.UNDISPATCHED) {
//            yield()
//            logicalPieces.first().state.emit(PieceState.FINISHED)
//        }
//        assertCoroutineSuspends {
//            file.seek(1)
//        }
//        assertEquals(1L, file.offset)
//    }

//    @Test
//    fun `seek first complete only when get that piece`() = runTest {
//        logicalPieces[2].state.emit(PieceState.FINISHED)
//        assertCoroutineSuspends {
//            file.seek(1)
//        }
//        assertEquals(1L, file.offset)
//    }

//    @Test
//    fun seekToSecondPiece() = runTest {
//        launch(start = CoroutineStart.UNDISPATCHED) {
//            yield()
//            logicalPieces[1].state.emit(PieceState.FINISHED)
//            println("Piece finished")
//        }
//        file.seek(17)
//        assertEquals(17L, file.offset)
//    }

    @Test
    fun seekReadSecondPiece() = runTest {
        logicalPieces[1].state.emit(PieceState.FINISHED)
        input.seek(16)
        assertEquals(16L, input.position)
        input.readBytes().run {
            assertEquals(16, size)
            assertEquals(16L..<32L, input.bufferedOffsetRange)
            assertEquals("imply dummy text", String(this))
        }
    }

    @Test
    fun seekReadSecondPieceMiddle() = runTest {
        logicalPieces[1].state.emit(PieceState.FINISHED)
        input.seek(17)
        assertEquals(17L, input.position)
        input.readBytes().run {
            assertEquals(16L..<32L, input.bufferedOffsetRange)
            assertEquals("mply dummy text", String(this))
        }
    }

    @Test
    fun `seek buffer both direction`() = runTest {
        logicalPieces[0].state.emit(PieceState.FINISHED)
        logicalPieces[1].state.emit(PieceState.FINISHED)
        input.seek(17)
        assertEquals(17L, input.position)
        input.readBytes().run {
            assertEquals(0L..<32L, input.bufferedOffsetRange)
            assertEquals("mply dummy text", String(this))
        }
    }

    @Test
    fun `seek buffer both direction then seek back`() = runTest {
        logicalPieces[0].state.emit(PieceState.FINISHED)
        logicalPieces[1].state.emit(PieceState.FINISHED)
        input.seek(17)
        assertEquals(17L, input.position)
        input.readBytes().run {
            assertEquals(0L..<32L, input.bufferedOffsetRange)
            assertEquals("mply dummy text", String(this))
        }
        input.seek(0)
        input.readBytes().run {
            assertEquals(0L..<32L, input.bufferedOffsetRange)
            assertEquals("Lorem Ipsum is simply dummy text", String(this))
        }
    }

    @Test
    fun `buffer single finished pieces, initial zero`() = runTest {
        logicalPieces[0].state.value = PieceState.FINISHED
        // other pieces not finished
        assertEquals(logicalPieces[0].size, input.computeMaxBufferSizeForward(0, 100000))
        assertEquals(0, input.computeMaxBufferSizeBackward(0, 100000))
    }

    @Test
    fun `buffer single finished pieces, from intermediate`() = runTest {
        logicalPieces[0].state.value = PieceState.FINISHED
        // other pieces not finished
        assertEquals(logicalPieces[0].size - 10, input.computeMaxBufferSizeForward(10, 100000))
        assertEquals(10, input.computeMaxBufferSizeBackward(10, 100000))
    }

    @Test
    fun `buffer multiple finished pieces, from intermediate`() = runTest {
        logicalPieces[0].state.value = PieceState.FINISHED
        logicalPieces[1].state.value = PieceState.FINISHED
        // other pieces not finished
        assertEquals(logicalPieces[0].size - 0 + logicalPieces[0].size, input.computeMaxBufferSizeForward(0, 100000))
        assertEquals(0, input.computeMaxBufferSizeBackward(0, 100000))
        assertEquals(logicalPieces[0].size - 10 + logicalPieces[0].size, input.computeMaxBufferSizeForward(10, 100000))
        assertEquals(10, input.computeMaxBufferSizeBackward(10, 100000))
    }

    @Test
    fun `buffer zero byte (corner case)`() = runTest {
        logicalPieces[0].state.value = PieceState.FINISHED
        // other pieces not finished
        assertEquals(0, input.computeMaxBufferSizeForward(logicalPieces[0].size, 100000))
    }

    @Test
    fun `computeMaxBufferSize starting from piece not finished`() = runTest {
        // all pieces not finished
        assertEquals(0, input.computeMaxBufferSizeForward(0, 100000))
    }

    @Test
    fun `compute backward when curr piece not ready`() = runTest {
        assertEquals(0, input.computeMaxBufferSizeBackward(100, 100000))
    }

    @Test
    fun `compute backward when backward piece not ready`() = runTest {
        logicalPieces[1].state.value = PieceState.FINISHED
        assertEquals(2, input.computeMaxBufferSizeBackward(18, 100000))
    }


    @Test
    fun `reuse zero byte`() = runTest {
        for (logicalPiece in logicalPieces) {
            logicalPiece.state.value = PieceState.FINISHED
        }

        input.seek(30)
        assertEquals(0, input.read(ByteArray(0)))
        assertEquals(-1L..-1, input.bufferedOffsetRange)
    }


    ///////////////////////////////////////////////////////////////////////////
    // Reuse buffer
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `double prepareBuffer`() = runTest {
        for (logicalPiece in logicalPieces) {
            logicalPiece.state.value = PieceState.FINISHED
        }

        // buffer size is 20

        input.seek(30)
        assertEquals(1, input.read(ByteArray(1))) // fill buffer
        assertEquals(30 - bufferSize..<30L + bufferSize, input.bufferedOffsetRange)
        // 10..<50

        input.seek(0) // 超出 buffer 范围
        input.prepareBuffer()
        input.prepareBuffer()
        assertEquals(0L..<bufferSize, input.bufferedOffsetRange)
        // 0..<20, last 10 was reused from previous buffer

        assertEquals("Lorem Ipsum is simpl", input.readExactBytes(20).decodeToString())
    }

    @Test
    fun `reuse buffer from previous start`() = runTest {
        for (logicalPiece in logicalPieces) {
            logicalPiece.state.value = PieceState.FINISHED
        }

        // buffer size is 20

        input.seek(30)
        assertEquals(1, input.read(ByteArray(1))) // fill buffer
        assertEquals(30 - bufferSize..<30L + bufferSize, input.bufferedOffsetRange)
        // 10..<50

        input.seek(0) // 超出 buffer 范围
        input.prepareBuffer()
        assertEquals(0L..<bufferSize, input.bufferedOffsetRange)
        // 0..<20, last 10 was reused from previous buffer

        assertEquals("Lorem Ipsum is simpl", input.readExactBytes(20).decodeToString())
    }

    @Test
    fun `new buffer includes entire previous as head`() = runTest {
        for (logicalPiece in logicalPieces) {
            logicalPiece.state.value = PieceState.FINISHED
        }

        // buffer size is 20

        input.seek(0)
        input.prepareBuffer()
        assertEquals(0L..<bufferSize, input.bufferedOffsetRange)

        input.seek(bufferSize.toLong()) // 超出 buffer 范围
        input.prepareBuffer()
        assertEquals(0L..<bufferSize * 2, input.bufferedOffsetRange)
        // 0..<20, last 10 was reused from previous buffer

        assertEquals(sampleText.drop(bufferSize).take(20), input.readExactBytes(20).decodeToString())
        assertEquals(sampleText.drop(bufferSize + 20), input.readAllBytes().decodeToString())
        assertEquals("", input.readAllBytes().decodeToString())
    }

    @Test
    fun `new buffer includes entire previous as tail`() = runTest {
        for (logicalPiece in logicalPieces) {
            logicalPiece.state.value = PieceState.FINISHED
        }

        // buffer size is 20

        input.seek(sampleText.lastIndex.toLong()) // 576
        input.prepareBuffer()
        assertEquals(sampleText.lastIndex.toLong() - bufferSize..<sampleText.length, input.bufferedOffsetRange)

        input.seek(sampleText.lastIndex.toLong() - bufferSize - 1) // 超出 buffer 范围
        assertEquals(
            sampleText.substring(sampleText.lastIndex - bufferSize - 1),
            input.readAllBytes().decodeToString(),
        )
    }

    @TestFactory
    fun `reuse buffer from previous end`() = (20L..60L step 4).map { index ->
        DynamicTest.dynamicTest("$index") {
            for (logicalPiece in logicalPieces) {
                logicalPiece.state.value = PieceState.FINISHED
            }

            // buffer size is 20

            input.seek(30)
            assertEquals(1, input.read(ByteArray(1))) // fill buffer
            assertEquals(30 - bufferSize..<30L + bufferSize, input.bufferedOffsetRange)
            // 10..<50

            input.seek(index)
            input.prepareBuffer()
            assertEquals(index - bufferSize..<index + bufferSize, input.bufferedOffsetRange)
            // 40..<80, first 10 was reused from previous buffer

            assertEquals(sampleText.substring(index.toInt()).take(10), input.readExactBytes(10).decodeToString())
            assertEquals(sampleText.substring(index.toInt() + 10), input.readAllBytes().decodeToString())

            input.seek(0)
            assertEquals(sampleText, input.readAllBytes().decodeToString())
        }
    }

    @Test
    fun `buffer when piece not ready, then ready and re-buffer`() = runTest {
        for (logicalPiece in logicalPieces) {
            logicalPiece.state.value = PieceState.FINISHED
        }
        logicalPieces[2].state.value = PieceState.DOWNLOADING // 48..<64

        // buffer size is 20

        input.seek(16)
        input.prepareBuffer()
        assertEquals(0..<32L, input.bufferedOffsetRange) // 这里不是 36, 因为 2 号 piece 还没好
        // 16..<32

        logicalPieces[2].state.value = PieceState.FINISHED // 现在 2 号 piece 好了

        input.seek(32)
        input.prepareBuffer()
        assertEquals(32L - bufferSize..<32L + bufferSize, input.bufferedOffsetRange)

        assertEquals(sampleText.substring(32), input.readAllBytes().decodeToString())
    }

    @Test
    fun `seek no reuse`() = runTest {
        for (logicalPiece in logicalPieces) {
            logicalPiece.state.value = PieceState.FINISHED
        }

        // buffer size is 20

        input.seek(30)
        assertEquals(1, input.read(ByteArray(1))) // fill buffer
        assertEquals(30 - bufferSize..<30L + bufferSize, input.bufferedOffsetRange)
        // 10..<50

        input.seek(100) // 远超出 buffer 范围
        input.prepareBuffer()
        assertEquals(100L - bufferSize..<100L + bufferSize, input.bufferedOffsetRange)
        // 40..<80, first 10 was reused from previous buffer

        assertEquals(sampleText.substring(100..<110), input.readExactBytes(10).decodeToString())
    }

    @Test
    fun `random seek and read`() {
        for (logicalPiece in logicalPieces) {
            logicalPiece.state.value = PieceState.FINISHED
        }

        val random = Random(2352151)
        repeat(1000) {
            val pos = random.nextLong(0L..<sampleText.length).absoluteValue
            input.seek(pos)
//                val length = Random.nextInt()
            assertEquals(sampleText.substring(pos.toInt()), input.readAllBytes().decodeToString())
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Error cases
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `seek negative offset`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            input.seek(-1)
        }
    }

    @Test
    fun `read negative length`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            input.read(ByteArray(1), 1, -1)
        }
    }

    @Test
    fun `read negative offset`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            input.read(ByteArray(1), -1, 1)
        }
    }

    @Test
    fun `seek over size then read`() = runTest {
        input.seek(Long.MAX_VALUE)
        assertEquals(-1, input.read(ByteArray(10)))
    }

    @Test
    fun `read closed`() = runTest {
        input.close()
        assertFailsWith<IllegalStateException> {
            input.read(ByteArray(2))
        }
    }

    @Test
    fun `seek closed`() = runTest {
        input.close()
        assertFailsWith<IllegalStateException> {
            input.seek(10)
        }
    }
}
