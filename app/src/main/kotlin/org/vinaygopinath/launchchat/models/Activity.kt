package org.vinaygopinath.launchchat.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
@Entity(
    tableName = "activities",
    indices = [
        Index("occurred_at")
    ]
)
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val content: String,
    @ColumnInfo val source: Source,
    @ColumnInfo val message: String?,
    @ColumnInfo("occurred_at") val occurredAt: Instant,
    @ColumnInfo val note: String? = null
) : Parcelable {
    enum class Source {
        TEL,
        SMS,
        MMS,
        TEXT_SHARE,
        CONTACT,
        DIAL,
        UNKNOWN,
        MANUAL_INPUT,
        HISTORY
    }
}
