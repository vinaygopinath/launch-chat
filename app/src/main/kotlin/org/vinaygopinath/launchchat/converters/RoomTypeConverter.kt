package org.vinaygopinath.launchchat.converters

import androidx.room.TypeConverter
import java.time.Instant

class RoomTypeConverter {

    @TypeConverter
    fun convertInstantToLong(instant: Instant) = instant.toEpochMilli()

    @TypeConverter
    fun convertLongToInstant(long: Long): Instant = Instant.ofEpochMilli(long)
}