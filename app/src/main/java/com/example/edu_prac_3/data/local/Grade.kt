package com.example.edu_prac_3.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Оценка студента по конкретной дисциплине.
 *
 * Связь: [disciplineId] ссылается на `disciplines.id` (1 – N).
 * При удалении дисциплины её оценки удаляются каскадно (ON DELETE CASCADE) —
 * это осознанное решение спринта 2, зафиксировано в docs/DATA_MODEL.md.
 *
 * Дата хранится строкой в формате ISO `yyyy-MM-dd`: такой формат корректно
 * сортируется лексикографически и сравнивается через BETWEEN, а также не
 * требует desugaring-а java.time на minSdk 24.
 */
@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = Discipline::class,
            parentColumns = ["id"],
            childColumns = ["discipline_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["discipline_id"]),
        Index(value = ["date"])
    ]
)
data class Grade(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    /** Идентификатор дисциплины, к которой относится оценка. */
    @ColumnInfo(name = "discipline_id")
    val disciplineId: Long,

    /** Значение оценки: по умолчанию пятибалльная шкала 2–5 (см. GradeRules). */
    @ColumnInfo(name = "value")
    val value: Int,

    /** Дата выставления оценки в формате ISO `yyyy-MM-dd`. */
    @ColumnInfo(name = "date")
    val date: String,

    /** Вид работы: контрольная, практическая, лабораторная, ответ, другое. */
    @ColumnInfo(name = "work_type")
    val workType: WorkType = WorkType.OTHER,

    /** Комментарий — необязательное поле. */
    @ColumnInfo(name = "comment")
    val comment: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
