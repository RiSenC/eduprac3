package com.example.edu_prac_3.data

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.edu_prac_3.data.local.AppDatabase
import com.example.edu_prac_3.data.local.DemoData
import com.example.edu_prac_3.data.local.Discipline
import com.example.edu_prac_3.data.local.Grade
import com.example.edu_prac_3.data.local.WorkType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Тесты уровня данных (Спринт 2). Запускаются на JVM через Robolectric —
 * эмулятор для них не нужен, поэтому проверки доступны на любом компьютере
 * команды: `./gradlew testDebugUnitTest`.
 *
 * Проверяются требования контрольной точки спринта: создание, чтение, изменение
 * и удаление дисциплин и оценок, связь 1 – N, каскадное удаление, внешние ключи,
 * ограничение диапазона оценок, расчёт среднего балла и демонстрационные данные.
 */
@RunWith(RobolectricTestRunner::class)
class GradeBookDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    // ------------------------------------------------------------- дисциплины

    @Test
    fun insertDiscipline_returnsGeneratedIdAndIsReadable() = runBlocking {
        val id = database.disciplineDao()
            .insert(Discipline(name = "Математический анализ", teacher = "Смирнова О. В."))

        assertTrue("Room должен сгенерировать id > 0", id > 0)

        val loaded = database.disciplineDao().getById(id)
        assertNotNull(loaded)
        assertEquals("Математический анализ", loaded!!.name)
        assertEquals("Смирнова О. В.", loaded.teacher)
    }

    @Test
    fun teacherIsOptional() = runBlocking {
        val id = database.disciplineDao().insert(Discipline(name = "Физика"))
        assertNull(database.disciplineDao().getById(id)!!.teacher)
    }

    @Test
    fun duplicateDisciplineName_isRejected() = runBlocking {
        database.disciplineDao().insert(Discipline(name = "История"))
        try {
            database.disciplineDao().insert(Discipline(name = "История"))
            fail("Ожидалось нарушение уникальности названия дисциплины")
        } catch (_: SQLiteConstraintException) {
            // ожидаемо: уникальный индекс disciplines.name
        }
    }

    @Test
    fun disciplines_areSortedByName() = runBlocking {
        val dao = database.disciplineDao()
        dao.insert(Discipline(name = "Физика"))
        dao.insert(Discipline(name = "Алгебра"))
        dao.insert(Discipline(name = "История"))

        assertEquals(listOf("Алгебра", "История", "Физика"), dao.getAll().map { it.name })
    }

    @Test
    fun updateDiscipline_changesNameAndTeacher() = runBlocking {
        val dao = database.disciplineDao()
        val id = dao.insert(Discipline(name = "Программирование"))
        val stored = dao.getById(id)!!

        val rows = dao.update(stored.copy(name = "Программирование на Kotlin", teacher = "Кузнецов А. П."))

        assertEquals(1, rows)
        assertEquals("Программирование на Kotlin", dao.getById(id)!!.name)
        assertEquals("Кузнецов А. П.", dao.getById(id)!!.teacher)
    }

    // ------------------------------------------------------------------ оценки

    @Test
    fun insertAndReadGrades_byDisciplineDescendingByDate() = runBlocking {
        val disciplineId = database.disciplineDao().insert(Discipline(name = "История"))
        val grades = database.gradeDao()

        grades.insert(
            Grade(disciplineId = disciplineId, value = 3, date = "2026-09-18", workType = WorkType.ANSWER)
        )
        grades.insert(
            Grade(
                disciplineId = disciplineId,
                value = 5,
                date = "2026-10-23",
                workType = WorkType.CONTROL,
                comment = "Итоговая"
            )
        )
        grades.insert(
            Grade(disciplineId = disciplineId, value = 4, date = "2026-10-02", workType = WorkType.PRACTICE)
        )

        val loaded = grades.getByDiscipline(disciplineId)

        assertEquals(3, loaded.size)
        assertEquals(listOf("2026-10-23", "2026-10-02", "2026-09-18"), loaded.map { it.date })
        assertEquals(WorkType.CONTROL, loaded.first().workType)
        assertEquals("Итоговая", loaded.first().comment)
    }

    @Test
    fun updateAndDeleteGrade() = runBlocking {
        val disciplineId = database.disciplineDao().insert(Discipline(name = "Физика"))
        val grades = database.gradeDao()
        val gradeId = grades.insert(Grade(disciplineId = disciplineId, value = 3, date = "2026-10-05"))

        val stored = grades.getById(gradeId)!!
        assertEquals(1, grades.update(stored.copy(value = 5, comment = "Пересдача")))
        assertEquals(5, grades.getById(gradeId)!!.value)
        assertEquals("Пересдача", grades.getById(gradeId)!!.comment)

        assertEquals(1, grades.deleteById(gradeId))
        assertNull(grades.getById(gradeId))
    }

    @Test
    fun gradeWithUnknownDiscipline_isRejectedByForeignKey() = runBlocking {
        try {
            database.gradeDao().insert(Grade(disciplineId = 999L, value = 4, date = "2026-10-05"))
            fail("Ожидалось нарушение внешнего ключа discipline_id")
        } catch (_: SQLiteConstraintException) {
            // ожидаемо: оценка не может ссылаться на несуществующую дисциплину
        }
    }

    @Test
    fun deletingDiscipline_deletesItsGradesCascade() = runBlocking {
        val disciplineId = database.disciplineDao().insert(Discipline(name = "История"))
        val grades = database.gradeDao()
        grades.insert(Grade(disciplineId = disciplineId, value = 3, date = "2026-09-18"))
        grades.insert(Grade(disciplineId = disciplineId, value = 4, date = "2026-10-02"))
        assertEquals(2, grades.count())

        database.disciplineDao().deleteById(disciplineId)

        assertEquals(0, grades.count())
    }

    @Test
    fun relation_loadsDisciplineWithItsGrades() = runBlocking {
        val disciplineId = database.disciplineDao().insert(Discipline(name = "Программирование"))
        database.gradeDao().insert(Grade(disciplineId = disciplineId, value = 5, date = "2026-09-21"))

        val loaded = database.disciplineDao().getWithGrades(disciplineId)

        assertNotNull(loaded)
        assertEquals("Программирование", loaded!!.discipline.name)
        assertEquals(1, loaded.grades.size)
    }

    @Test
    fun periodFilter_returnsGradesInsideRange() = runBlocking {
        val disciplineId = database.disciplineDao().insert(Discipline(name = "История"))
        val grades = database.gradeDao()
        grades.insert(Grade(disciplineId = disciplineId, value = 2, date = "2026-09-01"))
        grades.insert(Grade(disciplineId = disciplineId, value = 4, date = "2026-10-15"))
        grades.insert(Grade(disciplineId = disciplineId, value = 5, date = "2026-11-20"))

        val october = grades.getByPeriod("2026-10-01", "2026-10-31")

        assertEquals(1, october.size)
        assertEquals(4, october.first().value)
    }

    // ---------------------------------------------------------------- расчёты

    @Test
    fun averageForDiscipline_matchesManualCalculation() = runBlocking {
        val disciplineId = database.disciplineDao().insert(Discipline(name = "Математический анализ"))
        val grades = database.gradeDao()
        listOf(5, 4, 3, 5).forEachIndexed { index, value ->
            grades.insert(
                Grade(disciplineId = disciplineId, value = value, date = "2026-10-0${index + 1}")
            )
        }

        // (5 + 4 + 3 + 5) / 4 = 4.25 — пример из раздела 9 ТЗ
        assertEquals(4.25, grades.averageForDiscipline(disciplineId), 0.0001)
    }

    @Test
    fun averagesAreZero_whenThereAreNoGrades() = runBlocking {
        val disciplineId = database.disciplineDao().insert(Discipline(name = "Физика"))

        assertEquals(0.0, database.gradeDao().averageForDiscipline(disciplineId), 0.0001)
        assertEquals(0.0, database.gradeDao().overallAverage(), 0.0001)
    }

    @Test
    fun overallAverage_countsAllGrades() = runBlocking {
        val math = database.disciplineDao().insert(Discipline(name = "Математический анализ"))
        val programming = database.disciplineDao().insert(Discipline(name = "Программирование"))
        val grades = database.gradeDao()
        grades.insert(Grade(disciplineId = math, value = 4, date = "2026-09-01"))
        grades.insert(Grade(disciplineId = programming, value = 5, date = "2026-09-02"))

        assertEquals(4.5, grades.overallAverage(), 0.0001)
    }

    // -------------------------------------------------------- демонстрационные данные

    @Test
    fun demoData_isInsertedOnlyOnce() = runBlocking {
        val dao = database.disciplineDao()
        val grades = database.gradeDao()

        DemoData.seedIfEmpty(database)
        assertEquals(4, dao.count())
        assertEquals(10, grades.count())

        DemoData.seedIfEmpty(database) // повторный запуск не должен дублировать записи
        assertEquals(4, dao.count())
        assertEquals(10, grades.count())
    }

    @Test
    fun demoData_controlSetGivesAverage425() = runBlocking {
        DemoData.seedIfEmpty(database)
        val math = database.disciplineDao().findByName("Математический анализ")

        assertNotNull(math)
        assertEquals(4.25, database.gradeDao().averageForDiscipline(math!!.id), 0.0001)
    }

    @Test
    fun demoData_hasDisciplineWithoutGradesForEmptyState() = runBlocking {
        DemoData.seedIfEmpty(database)
        val physics = database.disciplineDao().findByName("Физика")

        assertNotNull(physics)
        assertEquals(0, database.disciplineDao().gradeCount(physics!!.id))
    }

    // ------------------------------------------------------------ слой операций

    @Test
    fun repository_rejectsGradeOutsideRange() = runBlocking {
        val repository = GradeBookRepository(database)
        val disciplineId = repository.addDiscipline("История")

        try {
            repository.addGrade(disciplineId, value = 6, date = "2026-10-05")
            fail("Оценка 6 должна быть отклонена")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message!!.contains("2"))
        }
    }

    @Test
    fun repository_rejectsInvalidDate() = runBlocking {
        val repository = GradeBookRepository(database)
        val disciplineId = repository.addDiscipline("История")

        try {
            repository.addGrade(disciplineId, value = 4, date = "05.10.2026")
            fail("Дата не в формате ISO должна быть отклонена")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message!!.contains("ГГГГ-ММ-ДД"))
        }
    }

    @Test
    fun repository_trimsInputAndObservesChanges() = runBlocking {
        val repository = GradeBookRepository(database)
        val disciplineId = repository.addDiscipline("  История  ", "   ")

        val stored = repository.getDiscipline(disciplineId)!!
        assertEquals("История", stored.name)
        assertNull(stored.teacher)

        repository.addGrade(disciplineId, value = 4, date = "2026-10-05", comment = "  Семинар  ")

        val observed = repository.observeGrades(disciplineId).first()
        assertEquals(1, observed.size)
        assertEquals("Семинар", observed.first().comment)
    }

    @Test
    fun repository_hasGradesHelpsToWarnBeforeDeleting() = runBlocking {
        val repository = GradeBookRepository(database)
        val disciplineId = repository.addDiscipline("История")

        assertTrue(!repository.hasGrades(disciplineId))
        repository.addGrade(disciplineId, value = 3, date = "2026-09-18")
        assertTrue(repository.hasGrades(disciplineId))
    }
}
