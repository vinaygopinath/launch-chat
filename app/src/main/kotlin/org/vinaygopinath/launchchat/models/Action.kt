package org.vinaygopinath.launchchat.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "actions",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            childColumns = ["activity_id"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("activity_id")
    ]
)
data class Action(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo("activity_id") val activityId: Long,
    @ColumnInfo("phone_number") val phoneNumber: String,
    @ColumnInfo val type: Type,
    @ColumnInfo("occurred_at") val occurredAt: Instant
) {
    enum class Type {
        WHATSAPP,
        SIGNAL,
        TELEGRAM
    }
}
