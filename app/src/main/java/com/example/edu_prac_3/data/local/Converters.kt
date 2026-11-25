package com.example.edu_prac_3.data.local

import androidx.room.TypeConverter

/**
 * TypeConverter-ы Room: переводят «богатые» типы Kotlin в типы, понятные SQLite.
 * Пока конвертер один — [WorkType], но класс уже выделен, чтобы в следующих
 * спринтах добавлять сюда новые типы без переделки базы.
 */
class Converters {

    @TypeConverter
    fun workTypeToCode(type: WorkType): String = type.code

    @TypeConverter
    fun codeToWorkType(code: String): WorkType = WorkType.fromCode(code)
}
