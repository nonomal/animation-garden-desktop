package me.him188.ani.app.platform

import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import kotlinx.io.IOException
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.Duration.Companion.days

object JvmLogHelper {
    @Throws(IOException::class)
    fun deleteOldLogs(logsFolder: Path) {
        val now = Clock.System.now()
        logsFolder.run {
            if (isDirectory()) {
                listDirectoryEntries()
            } else emptyList()
        }.forEach { file ->
            if (file.extension == "log" && file.name.startsWith("app")
                && now - file.getLastModifiedTime().toInstant().toKotlinInstant() > 3.days
            ) {
                file.deleteIfExists()
            }
        }
    }
}
