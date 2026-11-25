package com.example.edu_prac_3.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Единая точка доступа к базе Room.
 *
 * Схема: [Discipline] (1) — (N) [Grade]. Версия 1 — первая схема спринта 2;
 * при последующих изменениях полей версия увеличивается и добавляется миграция.
 *
 * exportSchema = true: схема складывается в app/schemas и хранится в репозитории,
 * чтобы изменения структуры были видны в code review и в истории коммитов.
 */
@Database(
    entities = [Discipline::class, Grade::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun disciplineDao(): DisciplineDao

    abstract fun gradeDao(): GradeDao

    companion object {
        const val DATABASE_NAME = "gradebook.db"

        @Volatile
        private var instance: AppDatabase? = null

        /** Синглтон базы: Room не любит несколько открытых экземпляров на файл. */
        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                // В спринте 2 схема только создаётся; миграции появятся вместе с изменениями полей.
                .build()

        /** Сброс синглтона — нужен тестам и инструментальным проверкам. */
        fun resetForTests() {
            synchronized(this) {
                instance?.close()
                instance = null
            }
        }
    }
}
