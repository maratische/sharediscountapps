package maratische.android.sharediscountapps

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimeUtil {
    companion object {

        // Определение форматтера даты и времени
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")

        fun formatTimeFromLong(timeInMillis: Long): String = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault()).format(formatter)

    }
}