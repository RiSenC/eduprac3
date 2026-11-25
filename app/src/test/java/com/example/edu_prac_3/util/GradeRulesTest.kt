package com.example.edu_prac_3.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Юнит-тесты правил оценивания (JVM, без эмулятора).
 * Проверяют раздел 4 ТЗ: допустимый диапазон оценок и обязательность полей.
 */
class GradeRulesTest {

    @Test
    fun allowedValues_areTwoToFive() {
        assertEquals(listOf(2, 3, 4, 5), GradeRules.values())
    }

    @Test
    fun valueInsideRange_isValid() {
        assertTrue(GradeRules.isValidValue(2))
        assertTrue(GradeRules.isValidValue(5))
        assertTrue(GradeRules.isValidValue(4))
    }

    @Test
    fun valueOutsideRange_isInvalid() {
        assertFalse(GradeRules.isValidValue(1))
        assertFalse(GradeRules.isValidValue(6))
        assertFalse(GradeRules.isValidValue(0))
        assertFalse(GradeRules.isValidValue(-3))
    }

    @Test
    fun validateValue_returnsMessageForInvalidValue() {
        assertNull(GradeRules.validateValue(4))
        assertNotNull(GradeRules.validateValue(1))
        assertNotNull(GradeRules.validateValue(6))
        assertTrue(GradeRules.validateValue(6)!!.contains("2"))
        assertTrue(GradeRules.validateValue(6)!!.contains("5"))
    }

    @Test
    fun parseValue_acceptsIntegerWithSpaces() {
        assertEquals(4, GradeRules.parseValue("4"))
        assertEquals(5, GradeRules.parseValue("  5  "))
    }

    @Test
    fun parseValue_rejectsNonNumbers() {
        assertNull(GradeRules.parseValue(""))
        assertNull(GradeRules.parseValue("  "))
        assertNull(GradeRules.parseValue("отлично"))
        assertNull(GradeRules.parseValue("4.5"))
        assertNull(GradeRules.parseValue("4a"))
    }

    @Test
    fun parseAndValidate_rejectsOutOfRange() {
        assertTrue(GradeRules.parseAndValidate("6").isFailure)
        assertTrue(GradeRules.parseAndValidate("1").isFailure)
        assertTrue(GradeRules.parseAndValidate("нет").isFailure)
        assertEquals(5, GradeRules.parseAndValidate("5").getOrNull())
    }

    @Test
    fun validateName_rejectsEmptyAndSpaces() {
        assertNotNull(GradeRules.validateName(""))
        assertNotNull(GradeRules.validateName("   "))
        assertNotNull(GradeRules.validateName("A"))
        assertNull(GradeRules.validateName("Математический анализ"))
    }

    @Test
    fun validateName_rejectsTooLongName() {
        val longName = "Д".repeat(GradeRules.NAME_MAX_LENGTH + 1)
        assertNotNull(GradeRules.validateName(longName))
        assertNull(GradeRules.validateName("Д".repeat(GradeRules.NAME_MAX_LENGTH)))
    }

    @Test
    fun validateTeacher_isOptional() {
        assertNull(GradeRules.validateTeacher(null))
        assertNull(GradeRules.validateTeacher(""))
        assertNull(GradeRules.validateTeacher("Смирнова О. В."))
        assertNotNull(GradeRules.validateTeacher("П".repeat(GradeRules.TEACHER_MAX_LENGTH + 1)))
    }

    @Test
    fun validateComment_limitsLength() {
        assertNull(GradeRules.validateComment(null))
        assertNull(GradeRules.validateComment("Контрольная № 1"))
        assertNotNull(GradeRules.validateComment("К".repeat(GradeRules.COMMENT_MAX_LENGTH + 1)))
    }
}
