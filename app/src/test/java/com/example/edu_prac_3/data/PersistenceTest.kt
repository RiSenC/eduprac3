package com.example.edu_prac_3.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.edu_prac_3.data.local.AppDatabase
import com.example.edu_prac_3.data.local.Discipline
import com.example.edu_prac_3.data.local.Grade
import com.example.edu_prac_3.data.local.WorkType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Проверка требования «данные сохраняются после закрытия и повторного запуска»
 * (контрольная точка спринта 2).
 *
 * База открывается как обычный файл, закрывается и открывается заново —
 * записи должны остаться на месте. Это и отличает Room/SQLite от хранения
 * в памяти. Запуск: `./gradlew testDebugUnitTest` (эмулятор не нужен).
 */
@RunWith(RobolectricTestRunner::class)
class PersistenceTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun dataSurvivesDatabaseReopen(): Unit = runBlocking {
        context.deleteDatabase(TEST_DATABASE)

        // Первый запуск: пишем дисциплину и оценку.
        var database = openDatabase()
        val disciplineId = database.disciplineDao().insert(
            Discipline(name = "Проверка сохранности", teacher = "Тестовый преподаватель")
        )
        val gradeId = database.gradeDao().insert(
            Grade(
                disciplineId = disciplineId,
                value = 5,
                date = "2026-11-20",
                workType = WorkType.CONTROL,
                comment = "Запись до перезапуска"
            )
        )
        database.close()

        // Второй запуск: читаем заново открытую базу.
        database = openDatabase()
        val loadedDiscipline = database.disciplineDao().getWithGrades(disciplineId)
        val loadedGrade = database.gradeDao().getById(gradeId)

        assertNotNull("Дисциплина должна сохраниться между запусками", loadedDiscipline)
        assertEquals("Проверка сохранности", loadedDiscipline!!.discipline.name)
        assertNotNull("Оценка должна сохраниться между запусками", loadedGrade)
        assertEquals(5, loadedGrade!!.value)
        assertEquals(WorkType.CONTROL, loadedGrade.workType)
        assertEquals("Запись до перезапуска", loadedGrade.comment)

        database.close()
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    fun deletedData_doesNotReappearAfterReopen(): Unit = runBlocking {
        context.deleteDatabase(TEST_DATABASE)

        var database = openDatabase()
        val disciplineId = database.disciplineDao().insert(Discipline(name = "Удаляемая дисциплина"))
        database.gradeDao().insert(Grade(disciplineId = disciplineId, value = 3, date = "2026-10-01"))
        database.disciplineDao().deleteById(disciplineId)
        database.close()

        database = openDatabase()
        assertEquals(0, database.disciplineDao().count())
        assertEquals(0, database.gradeDao().count())
        database.close()
        context.deleteDatabase(TEST_DATABASE)
    }

    private fun openDatabase(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, TEST_DATABASE)
            .allowMainThreadQueries()
            .build()

    private companion object {
        const val TEST_DATABASE = "gradebook-persistence-test.db"
    }
}
