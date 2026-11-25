package com.example.edu_prac_3.data

import com.example.edu_prac_3.data.local.AppDatabase
import com.example.edu_prac_3.data.local.Discipline
import com.example.edu_prac_3.data.local.DisciplineDao
import com.example.edu_prac_3.data.local.DisciplineWithGrades
import com.example.edu_prac_3.data.local.Grade
import com.example.edu_prac_3.data.local.GradeDao
import com.example.edu_prac_3.data.local.WorkType
import com.example.edu_prac_3.util.GradeDate
import com.example.edu_prac_3.util.GradeRules
import kotlinx.coroutines.flow.Flow

/**
 * Слой операций над данными: экраны (спринт 3) работают через репозиторий,
 * а не с DAO напрямую. Здесь же собраны проверки корректности ввода, чтобы
 * правила были в одном месте для интерфейса и тестов.
 */
class GradeBookRepository(
    private val disciplineDao: DisciplineDao,
    private val gradeDao: GradeDao
) {

    constructor(database: AppDatabase) : this(database.disciplineDao(), database.gradeDao())

    // ---------------------------------------------------------------- дисциплины

    fun observeDisciplines(): Flow<List<Discipline>> = disciplineDao.observeAll()

    suspend fun getDisciplines(): List<Discipline> = disciplineDao.getAll()

    suspend fun getDiscipline(id: Long): Discipline? = disciplineDao.getById(id)

    suspend fun getDisciplinesWithGrades(): List<DisciplineWithGrades> =
        disciplineDao.getAllWithGrades()

    suspend fun hasGrades(disciplineId: Long): Boolean = disciplineDao.gradeCount(disciplineId) > 0

    /**
     * Добавляет дисциплину после проверки названия.
     * @throws IllegalArgumentException если название пустое или слишком длинное.
     * @throws android.database.sqlite.SQLiteConstraintException если такое название уже есть.
     */
    suspend fun addDiscipline(name: String, teacher: String? = null): Long {
        GradeRules.validateName(name)?.let { throw IllegalArgumentException(it) }
        GradeRules.validateTeacher(teacher)?.let { throw IllegalArgumentException(it) }

        return disciplineDao.insert(
            Discipline(
                name = name.trim(),
                teacher = teacher?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
    }

    suspend fun updateDiscipline(discipline: Discipline): Int {
        GradeRules.validateName(discipline.name)?.let { throw IllegalArgumentException(it) }
        GradeRules.validateTeacher(discipline.teacher)?.let { throw IllegalArgumentException(it) }
        return disciplineDao.update(
            discipline.copy(
                name = discipline.name.trim(),
                teacher = discipline.teacher?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
    }

    /**
     * Удаляет дисциплину. Её оценки удаляются каскадно на уровне ForeignKey
     * (см. Grade), поэтому интерфейс обязан спросить подтверждение заранее.
     */
    suspend fun deleteDiscipline(id: Long): Int = disciplineDao.deleteById(id)

    // --------------------------------------------------------------------- оценки

    fun observeGrades(disciplineId: Long): Flow<List<Grade>> =
        gradeDao.observeByDiscipline(disciplineId)

    suspend fun getGrades(disciplineId: Long): List<Grade> = gradeDao.getByDiscipline(disciplineId)

    suspend fun getGradesByPeriod(from: String, to: String): List<Grade> {
        require(GradeDate.isValid(from)) { "Начало периода указано неверно" }
        require(GradeDate.isValid(to)) { "Конец периода указан неверно" }
        require(from <= to) { "Начало периода позже его конца" }
        return gradeDao.getByPeriod(from, to)
    }

    /**
     * Добавляет оценку с проверкой диапазона 2–5 и формата даты.
     * @throws IllegalArgumentException при некорректном значении, дате или комментарии.
     */
    suspend fun addGrade(
        disciplineId: Long,
        value: Int,
        date: String,
        workType: WorkType = WorkType.OTHER,
        comment: String? = null
    ): Long {
        GradeRules.validateValue(value)?.let { throw IllegalArgumentException(it) }
        require(GradeDate.isValid(date)) { "Дата должна быть указана в формате ГГГГ-ММ-ДД" }
        GradeRules.validateComment(comment)?.let { throw IllegalArgumentException(it) }

        return gradeDao.insert(
            Grade(
                disciplineId = disciplineId,
                value = value,
                date = date,
                workType = workType,
                comment = comment?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
    }

    suspend fun updateGrade(grade: Grade): Int {
        GradeRules.validateValue(grade.value)?.let { throw IllegalArgumentException(it) }
        require(GradeDate.isValid(grade.date)) { "Дата должна быть указана в формате ГГГГ-ММ-ДД" }
        GradeRules.validateComment(grade.comment)?.let { throw IllegalArgumentException(it) }
        return gradeDao.update(grade)
    }

    suspend fun deleteGrade(id: Long): Int = gradeDao.deleteById(id)

    // ------------------------------------------------------------------ расчёты

    /** Средний балл по дисциплине; 0.0, если оценок нет. */
    suspend fun averageForDiscipline(disciplineId: Long): Double =
        gradeDao.averageForDiscipline(disciplineId)

    /** Общий средний балл по всем оценкам; 0.0, если оценок нет. */
    suspend fun overallAverage(): Double = gradeDao.overallAverage()

    suspend fun gradesCount(): Int = gradeDao.count()
}
