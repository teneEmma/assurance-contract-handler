package com.kod.assurancecontracthandler.common.utilities

import androidx.room.TypeConverter
import java.util.Date

class TimeConverters {
    @TypeConverter
    fun fromTimestampToDate(timeStamp: Long?): Date?{
        return timeStamp?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimeStamp(date: Date?): Long?{
        return date?.time
    }
}