package com.example.edu_prac_3.util

/**
 * Правила корректности оценок и полей (раздел 4 ТЗ: допустимый диапазон 2–5).
 *
 * Логика вынесена в отдельный объект без зависимостей от Android, поэтому
 * проверяется обычными JUnit-тестами на JVM, без эмулятора.
 */
object GradeRules {

    /** Минимальная допустимая оценка пятибалльной шкалы. */
    const val MIN_VALUE: Int = 2

    /** Максимальная допустимая оценка пятибалльной шкалы. */
    const val MAX_VALUE: Int = 5

    val allowedValues: IntRange = MIN_VALUE..MAX_VALUE

    /** Допустимая шкала целиком: 2, 3, 4, 5. */
    fun values(): List<Int> = allowedValues.toList()

    fun isValidValue(value: Int): Boolean = value in allowedValues

    /** Возвращает текст ошибки или null, если значение корректно. */
    fun validateValue(value: Int): String? =
        if (isValidValue(value)) null else "Оценка должна быть целым числом от $MIN_VALUE до $MAX_VALUE"

    /** Разбор ввода из текстового поля: null, если введено не целое число. */
    fun parseValue(raw: String): Int? = raw.trim().toIntOrNull()

    fun parseAndValidate(raw: String): Result<Int> {
        val value = parseValue(raw) ?: return Result.failure(
            IllegalArgumentException("Введите оценку целым числом от $MIN_VALUE до $MAX_VALUE")
        )
        val error = validateValue(value)
        return if (error == null) Result.success(value) else Result.failure(IllegalArgumentException(error))
    }

    fun validateName(raw: String): String? {
        val name = raw.trim()
        return when {
            name.isEmpty() -> "Название дисциплины не может быть пустым"
            name.length < 2 -> "Название дисциплины слишком короткое"
            name.length > NAME_MAX_LENGTH ->
                "Название дисциплины длиннее $NAME_MAX_LENGTH символов"
            else -> null
        }
    }

    fun validateTeacher(raw: String?): String? {
        val teacher = raw?.trim().orEmpty()
        return when {
            teacher.isEmpty() -> null // поле необязательное
            teacher.length > TEACHER_MAX_LENGTH ->
                "Имя преподавателя длиннее $TEACHER_MAX_LENGTH символов"
            else -> null
        }
    }

    fun validateComment(raw: String?): String? {
        val comment = raw?.trim().orEmpty()
        return if (comment.length > COMMENT_MAX_LENGTH) {
            "Комментарий длиннее $COMMENT_MAX_LENGTH символов"
        } else {
            null
        }
    }

    const val NAME_MAX_LENGTH: Int = 60
    const val TEACHER_MAX_LENGTH: Int = 60
    const val COMMENT_MAX_LENGTH: Int = 200
}
