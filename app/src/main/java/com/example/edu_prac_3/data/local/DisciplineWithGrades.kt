package com.example.edu_prac_3.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Связь 1 – N, прочитанная одним запросом: дисциплина вместе со своими оценками.
 * Пригодится на экране дисциплины (спринт 3) и при расчёте среднего (спринт 4).
 */
data class DisciplineWithGrades(
    @Embedded
    val discipline: Discipline,

    @Relation(
        parentColumn = "id",
        entityColumn = "discipline_id"
    )
    val grades: List<Grade> = emptyList()
)
