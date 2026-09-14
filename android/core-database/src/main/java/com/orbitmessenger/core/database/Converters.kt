package com.orbitmessenger.core.database

import androidx.room.TypeConverter
import com.orbitmessenger.core.database.entity.MessageStatus
import com.orbitmessenger.core.database.entity.MessageType

class Converters {
    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = enumValueOf(value)

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = enumValueOf(value)
}
