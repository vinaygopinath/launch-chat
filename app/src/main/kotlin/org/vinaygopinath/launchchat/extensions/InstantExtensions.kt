package org.vinaygopinath.launchchat.extensions

import java.time.Instant
import java.time.temporal.ChronoField

fun Instant.withMillisecondPrecision(): Instant {
    return this.with(ChronoField.MILLI_OF_SECOND, 0)
}
