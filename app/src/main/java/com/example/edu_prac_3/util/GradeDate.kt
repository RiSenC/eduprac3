package com.example.edu_prac_3.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Работа с датой оценки.
 *
 * В базе дата хранится строкой ISO `yyyy-MM-dd`:
 *  - лексикографический порядок совпадает с хронологическим (сортировка и BETWEEN);
 *  - не нужен java.time и desugaring на minSdk 24;
 *  - значение читаемо при просмотре базы в DB Browser.
 *
 * Класс не зависит от Android-API, поэтому покрывается JUnit-тестами на JVM.
 */
object GradeDate {

    const val ISO_PATTERN: String = "yyyy-MM-dd"
    const val RU_PATTERN: String = "dd.MM.yyyy"

    private val ISO_REGEX = Regex("""^\d{4}-\d{2}-\d{2}$""")

    private fun isoFormat(): SimpleDateFormat =
        SimpleDateFormat(ISO_PATTERN, Locale.US).apply { isLenient = false }

    private fun ruFormat(): SimpleDateFormat =
        SimpleDateFormat(RU_PATTERN, Locale.forLanguageTag("ru-RU")).apply { isLenient = false }

    /** Текущая дата в формате ISO — значение по умолчанию в форме добавления оценки. */
    fun today(): String = isoFormat().format(Date())

    /** Собирает строку ISO из чисел (месяц 1–12). */
    fun fromParts(year: Int, month: Int, day: Int): String =
        String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

    /** true, если строка — существующая календарная дата в формате ISO. */
    fun isValid(iso: String): Boolean {
        val value = iso.trim()
        if (!ISO_REGEX.matches(value)) return false
        return parse(value) != null
    }

    /** Разбор ISO-строки; null для неверной даты. */
    fun parse(iso: String): Date? = try {
        isoFormat().parse(iso.trim())
    } catch (_: Exception) {
        null
    }

    /** ISO -> «дд.мм.гггг» для отображения в списках. */
    fun formatRu(iso: String): String {
        val date = parse(iso) ?: return iso
        return ruFormat().format(date)
    }

    /** «дд.мм.гггг» или ISO -> ISO; null, если распознать не удалось. */
    fun normalize(raw: String): String? {
        val value = raw.trim()
        if (isValid(value)) return value
        val ruDate = try {
            ruFormat().parse(value)
        } catch (_: Exception) {
            null
        } ?: return null
        return isoFormat().format(ruDate)
    }

    /** Первый день месяца, содержащего дату (для фильтра по периоду). */
    fun startOfMonth(iso: String): String? {
        val date = parse(iso) ?: return null
        val calendar = java.util.Calendar.getInstance().apply { time = date }
        return fromParts(
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            1
        )
    }
}
