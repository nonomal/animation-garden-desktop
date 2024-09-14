package me.him188.ani.datasources.ikaros

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal class DateFormater {
    /**
     * UTC Date Str format.
     * Such as: 2023-10-13T00:00:00
     */
    private val utcDateFormat: DateFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss",
        Locale.getDefault(),
    )

    fun utcDateStr2timeStamp(dateStr: String): Long {
        if (dateStr.isEmpty()) {
            return 0
        }
        return utcDateFormat.parse(dateStr).time
    }

    companion object {
        val default by lazy(LazyThreadSafetyMode.PUBLICATION) {
            DateFormater()
        }
    }

}
