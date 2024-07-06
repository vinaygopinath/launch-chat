package org.vinaygopinath.launchchat.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "activities"
)
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val content: String,
    @ColumnInfo val source: ContentSource,
    @ColumnInfo val message: String?,
    @ColumnInfo val occurredAt: Instant
)