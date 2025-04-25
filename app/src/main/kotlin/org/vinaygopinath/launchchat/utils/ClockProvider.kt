package org.vinaygopinath.launchchat.utils

import java.time.Clock

class ClockProvider {

    private val clock = Clock.systemDefaultZone()

    fun getClock(): Clock = clock
}