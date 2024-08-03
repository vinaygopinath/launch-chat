package org.vinaygopinath.launchchat.utils

import java.time.Instant
import java.time.temporal.ChronoField
import javax.inject.Inject

class DateUtils @Inject constructor(
    private val clockProvider: ClockProvider
) {

    fun getCurrentInstant(): Instant {
        return clockProvider.getClock().instant().with(ChronoField.MILLI_OF_SECOND, 0)
    }
}