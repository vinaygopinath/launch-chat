package org.vinaygopinath.launchchat.factories

import org.vinaygopinath.launchchat.extensions.withMillisecondPrecision
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.Activity.Source
import java.time.Instant

object ActivityFactory {

    fun build(
        id: Long = 0,
        content: String = "some-content",
        source: Source = Source.TEXT_SHARE,
        message: String? = null,
        occurredAt: Instant = Instant.now().withMillisecondPrecision(),
        note: String? = null
    ): Activity {
        return Activity(
            id = id,
            content = content,
            source = source,
            message = message,
            occurredAt = occurredAt,
            note = note
        )
    }
}