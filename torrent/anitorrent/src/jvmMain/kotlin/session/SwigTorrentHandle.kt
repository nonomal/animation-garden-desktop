package me.him188.ani.app.torrent.anitorrent.session

import me.him188.ani.app.torrent.anitorrent.HandleId
import me.him188.ani.app.torrent.anitorrent.binding.PeerInfoList
import me.him188.ani.app.torrent.anitorrent.binding.torrent_handle_t
import me.him188.ani.app.torrent.anitorrent.binding.torrent_info_t
import me.him188.ani.app.torrent.api.files.FilePriority
import me.him188.ani.app.torrent.api.peer.PeerInfo

class SwigTorrentHandle(
    internal val native: torrent_handle_t,
) : TorrentHandle {
    override val id: HandleId get() = native.id
    override val isValid: Boolean get() = native.is_valid

    override fun postStatusUpdates() {
        native.post_status_updates()
    }

    override fun postSaveResume() {
        native.post_save_resume()
    }

    override fun resume() {
        native.resume()
    }

    override fun setFilePriority(index: Int, priority: FilePriority) {
        native.set_file_priority(index, priority.toLibtorrentValue())
    }

    override fun getState(): TorrentHandleState {
        val state = native._state
        if (state == -1) {
            error("Failed to get state, native returned -1 (session is invalid)")
        }
        return TorrentHandleState.entries[state]
    }

    override fun reloadFile(): TorrentDescriptor {
        val res = native.reload_file()
        if (res != torrent_handle_t.reload_file_result_t.kReloadFileSuccess) {
            throw IllegalStateException("Failed to reload file, native returned $res")
        }
        val info = native.get_info_view()
        return SwigTorrentDescriptor(
            info ?: throw IllegalStateException("Failed to get info view, native get_info_view returned null"),
        )
    }

    override fun getPeers(): List<PeerInfo> {
        val peerInfoList = PeerInfoList()
        native.get_peers(peerInfoList)
        return peerInfoList.map { SwigPeerInfo(it) }
    }

    override fun setPieceDeadline(index: Int, deadline: Int) {
        native.set_piece_deadline(index, deadline)
    }

    override fun clearPieceDeadlines() {
        native.clear_piece_deadlines()
    }

    override fun addTracker(tracker: String, tier: Short, failLimit: Short) {
        native.add_tracker(tracker, tier, failLimit)
    }

    override fun getMagnetUri(): String? = native.make_magnet_uri().takeIf { it.isNotBlank() }
}

class SwigTorrentDescriptor(
    private val native: torrent_info_t,
) : TorrentDescriptor {
    override val name: String
        get() = native.name
    override val fileCount: Int
        get() {
            val count = native.file_count()
            if (count > Int.MAX_VALUE.toLong()) {
                error("File count is too large to fit into Int: $count")
            }
            return count.toInt()
        }

    override fun fileAtOrNull(index: Int): TorrentFileInfo? = native.file_at(index)?.let(::SwigTorrentFileInfo)

    override val numPieces: Int
        get() = native.num_pieces
    override val lastPieceSize: Long
        get() = native.last_piece_size.toUInt().toLong()
    override val pieceLength: Long
        get() = native.piece_length.toUInt().toLong()
}

private fun FilePriority.toLibtorrentValue(): Short = when (this) {
    FilePriority.IGNORE -> 0
    FilePriority.LOW -> 1
    FilePriority.NORMAL -> 4
    FilePriority.HIGH -> 7
}