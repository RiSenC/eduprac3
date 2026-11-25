package com.example.edu_prac_3.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Юнит-тесты формата даты оценки (JVM, без эмулятора).
 * Формат ISO `yyyy-MM-dd` выбран осознанно: он сортируется как строка
 * и не требует java.time на minSdk 24.
 */
class GradeDateTest {

    @Test
    fun validIsoDate_isAccepted() {
        assertTrue(GradeDate.isValid("2026-10-26"))
        assertTrue(GradeDate.isValid("2024-02-29")) // високосный год
    }

    @Test
    fun nonexistentDate_isRejected() {
        assertFalse(GradeDate.isValid("2026-02-30"))
        assertFalse(GradeDate.isValid("2026-13-01"))
        assertFalse(GradeDate.isValid("2026-00-10"))
    }

    @Test
    fun wrongFormat_isRejected() {
        assertFalse(GradeDate.isValid("26.10.2026"))
        assertFalse(GradeDate.isValid("2026-1-5"))
        assertFalse(GradeDate.isValid(""))
        assertFalse(GradeDate.isValid("2026/10/26"))
    }

    @Test
    fun fromParts_padsWithZeros() {
        assertEquals("2026-01-05", GradeDate.fromParts(2026, 1, 5))
        assertEquals("2026-11-25", GradeDate.fromParts(2026, 11, 25))
    }

    @Test
    fun formatRu_returnsRussianStyle() {
        assertEquals("26.10.2026", GradeDate.formatRu("2026-10-26"))
    }

    @Test
    fun formatRu_returnsInputForBrokenDate() {
        assertEquals("не дата", GradeDate.formatRu("не дата"))
    }

    @Test
    fun normalize_acceptsBothFormats() {
        assertEquals("2026-10-26", GradeDate.normalize("2026-10-26"))
        assertEquals("2026-10-26", GradeDate.normalize("26.10.2026"))
        assertNull(GradeDate.normalize("32.10.2026"))
        assertNull(GradeDate.normalize(""))
    }

    @Test
    fun today_isValidIso() {
        assertTrue(GradeDate.isValid(GradeDate.today()))
    }

    @Test
    fun startOfMonth_returnsFirstDay() {
        assertEquals("2026-10-01", GradeDate.startOfMonth("2026-10-26"))
        assertNull(GradeDate.startOfMonth("2026-10-32"))
    }

    @Test
    fun isoStrings_areChronologicallyOrdered() {
        val dates = listOf("2026-10-26", "2026-09-14", "2026-11-01")
        assertEquals(
            listOf("2026-09-14", "2026-10-26", "2026-11-01"),
            dates.sorted()
        )
    }
}
