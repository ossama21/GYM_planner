package com.H_Oussama.gymplanner.data.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for Room to convert between Date objects and Long timestamp values
 */
class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 
 
 