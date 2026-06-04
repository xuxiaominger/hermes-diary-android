package com.hermesdiary.app.data

import androidx.room.TypeConverter

/**
 * Room 类型转换器 — 把枚举转成字符串存数据库
 */
class Converters {

    @TypeConverter
    fun fromEntryType(value: EntryType): String {
        return value.name
    }

    @TypeConverter
    fun toEntryType(value: String): EntryType {
        return EntryType.valueOf(value)
    }
}
