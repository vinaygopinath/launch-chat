package org.vinaygopinath.launchchat.converters

import androidx.room.TypeConverter
import org.vinaygopinath.launchchat.models.ChatApp
import java.time.Instant

class RoomTypeConverter {

    @TypeConverter
    fun convertInstantToLong(instant: Instant) = instant.toEpochMilli()

    @TypeConverter
    fun convertLongToInstant(long: Long): Instant = Instant.ofEpochMilli(long)

    @TypeConverter
    fun convertNullableInstantToNullableLong(instant: Instant?) = instant?.toEpochMilli()

    @TypeConverter
    fun convertNullableLongToNullableInstant(long: Long?): Instant? =
        long?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun convertNullableIdentifierTypeToNullableString(identifierType: ChatApp.IdentifierType?): String? =
        identifierType?.internalName

    @TypeConverter
    fun convertNullableStringToNullableIdentifierType(
        nullableIdentifierType: String?
    ): ChatApp.IdentifierType? {
        return nullableIdentifierType?.let { identifierTypeStr ->
            ChatApp.IdentifierType.entries.firstOrNull { it.internalName == identifierTypeStr }
        }
    }

    @TypeConverter
    fun convertNullableLaunchTypeToNullableString(launchType: ChatApp.LaunchType?): String? =
        launchType?.internalName

    @TypeConverter
    fun convertStringToLaunchType(nullableLaunchType: String?): ChatApp.LaunchType? {
        return nullableLaunchType?.let { launchTypeStr ->
            ChatApp.LaunchType.entries.firstOrNull { it.internalName == launchTypeStr }
        }
    }
}

