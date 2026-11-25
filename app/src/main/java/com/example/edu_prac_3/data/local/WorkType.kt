package com.example.edu_prac_3.data.local

/**
 * Вид учебной работы, за которую выставлена оценка.
 *
 * В базе хранится строковый код ([code]), а не порядковый номер: так данные
 * остаются читаемыми в БД и не «съезжают» при изменении порядка констант.
 */
enum class WorkType(val code: String, val title: String) {
    CONTROL("control", "Контрольная"),
    PRACTICE("practice", "Практическая"),
    LAB("lab", "Лабораторная"),
    ANSWER("answer", "Ответ"),
    OTHER("other", "Другое");

    companion object {
        /** Значение по умолчанию, если в базе встретился неизвестный код. */
        val DEFAULT: WorkType = OTHER

        fun fromCode(code: String): WorkType =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT

        fun fromTitle(title: String): WorkType? =
            entries.firstOrNull { it.title.equals(title.trim(), ignoreCase = true) }
    }
}
