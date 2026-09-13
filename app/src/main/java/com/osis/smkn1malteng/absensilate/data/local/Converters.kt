package com.osis.smkn1malteng.absensilate.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(value: List<Long>) = value.joinToString(",")
    @TypeConverter
    fun toList(value: String) = if (value.isEmpty()) emptyList() else value.split(",").map { it.toLong() }
}
