package com.example.edu_prac_3.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Учебная дисциплина — «родительская» сущность модели данных.
 *
 * Связь с оценками: одна дисциплина может иметь много оценок (1 – N).
 * Название дисциплины уникально: повторное добавление того же предмета
 * отсекается на уровне базы (уникальный индекс).
 */
@Entity(
    tableName = "disciplines",
    indices = [Index(value = ["name"], unique = true)]
)
data class Discipline(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    /** Название предмета, обязательное поле, уникальное. */
    @ColumnInfo(name = "name")
    val name: String,

    /** Преподаватель — необязательное поле. */
    @ColumnInfo(name = "teacher")
    val teacher: String? = null,

    /** Метка создания записи, мс с epoch. Нужна для стабильного порядка вывода. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
