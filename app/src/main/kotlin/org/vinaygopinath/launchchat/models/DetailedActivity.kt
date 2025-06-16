package org.vinaygopinath.launchchat.models

import androidx.room.Embedded
import androidx.room.Relation

data class DetailedActivity(
    @Embedded val activity: Activity,
    @Relation(
        parentColumn = "id",
        entityColumn = "activity_id"
    ) val actions: List<Action>
)
