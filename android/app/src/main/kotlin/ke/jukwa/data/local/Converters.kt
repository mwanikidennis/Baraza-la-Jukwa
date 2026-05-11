package ke.jukwa.data.local

import androidx.room.TypeConverter
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(value: String?): UUID? = value?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.joinToString(",")

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.split(",")?.filter { it.isNotEmpty() }

    @TypeConverter
    fun fromAnonymityMode(mode: String?): String? = mode

    @TypeConverter
    fun toAnonymityMode(value: String?): String? = value
}
