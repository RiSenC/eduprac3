package com.example.edu_prac_3.data.local

import com.example.edu_prac_3.util.GradeDate

/**
 * Демонстрационные данные для первого запуска приложения.
 *
 * Правила:
 *  - данные вымышленные (требование ТЗ: никаких реальных студентов);
 *  - заполняем только пустую базу, поэтому повторный запуск ничего не дублирует;
 *  - одна дисциплина («Физика») намеренно остаётся без оценок — на ней
 *    проверяется пустое состояние и отсутствие деления на ноль.
 *
 * Контрольный набор: «Математический анализ» — оценки 5, 4, 3, 5,
 * средний балл 4.25 (пример из раздела 9 ТЗ).
 */
object DemoData {

    suspend fun seedIfEmpty(database: AppDatabase) {
        val disciplineDao = database.disciplineDao()
        if (disciplineDao.count() > 0) return

        val ids = disciplineDao.insertAll(
            listOf(
                Discipline(name = "Математический анализ", teacher = "Смирнова О. В."),
                Discipline(name = "Программирование", teacher = "Кузнецов А. П."),
                Discipline(name = "История", teacher = "Волкова Е. С."),
                Discipline(name = "Физика", teacher = "Орлов Д. Н.")
            )
        )

        val math = ids[0]
        val programming = ids[1]
        val history = ids[2]

        database.gradeDao().insertAll(
            listOf(
                Grade(
                    disciplineId = math,
                    value = 5,
                    date = GradeDate.fromParts(2026, 9, 14),
                    workType = WorkType.PRACTICE,
                    comment = "Пределы и непрерывность"
                ),
                Grade(
                    disciplineId = math,
                    value = 4,
                    date = GradeDate.fromParts(2026, 9, 28),
                    workType = WorkType.CONTROL,
                    comment = "Контрольная № 1"
                ),
                Grade(
                    disciplineId = math,
                    value = 3,
                    date = GradeDate.fromParts(2026, 10, 12),
                    workType = WorkType.ANSWER,
                    comment = "Ответ у доски"
                ),
                Grade(
                    disciplineId = math,
                    value = 5,
                    date = GradeDate.fromParts(2026, 10, 26),
                    workType = WorkType.LAB,
                    comment = "Лабораторная: численные методы"
                ),
                Grade(
                    disciplineId = programming,
                    value = 5,
                    date = GradeDate.fromParts(2026, 9, 21),
                    workType = WorkType.LAB,
                    comment = "Работа с массивами"
                ),
                Grade(
                    disciplineId = programming,
                    value = 5,
                    date = GradeDate.fromParts(2026, 10, 5),
                    workType = WorkType.PRACTICE,
                    comment = "Рефакторинг кода"
                ),
                Grade(
                    disciplineId = programming,
                    value = 4,
                    date = GradeDate.fromParts(2026, 10, 19),
                    workType = WorkType.CONTROL,
                    comment = "Контрольная: коллекции"
                ),
                Grade(
                    disciplineId = history,
                    value = 3,
                    date = GradeDate.fromParts(2026, 9, 18),
                    workType = WorkType.ANSWER,
                    comment = "Семинар"
                ),
                Grade(
                    disciplineId = history,
                    value = 2,
                    date = GradeDate.fromParts(2026, 10, 2),
                    workType = WorkType.CONTROL,
                    comment = "Пропущенная тема"
                ),
                Grade(
                    disciplineId = history,
                    value = 4,
                    date = GradeDate.fromParts(2026, 10, 23),
                    workType = WorkType.PRACTICE,
                    comment = "Работа с источниками"
                )
            )
        )
    }
}
