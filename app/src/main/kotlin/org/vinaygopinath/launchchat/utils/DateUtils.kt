package org.vinaygopinath.launchchat.utils

import org.vinaygopinath.launchchat.extensions.withMillisecondPrecision
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

class DateUtils @Inject constructor(
    private val clockProvider: ClockProvider
) {

    fun getCurrentInstant(): Instant {
        return clockProvider.getClock().instant().withMillisecondPrecision()
    }

    fun getDateTimeString(instant: Instant, formatStyle: FormatStyle = FormatStyle.SHORT): String {
        return DateTimeFormatter.ofLocalizedDateTime(formatStyle)
            .format(instant.atZone(clockProvider.getClock().zone))
    }
}