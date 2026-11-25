package com.example.edu_prac_3.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO для оценок (таблица `grades`): CRUD, выборка по дисциплине и периоду,
 * расчёт среднего балла.
 *
 * Период задаётся двумя строками ISO (`yyyy-MM-dd`) включительно: формат
 * сравнивается корректно и не требует преобразований на стороне SQLite.
 */
@Dao
interface GradeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(grade: Grade): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(grades: List<Grade>): List<Long>

    @Update
    suspend fun update(grade: Grade): Int

    @Delete
    suspend fun delete(grade: Grade): Int

    @Query("DELETE FROM grades WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    /** Удаление всех оценок дисциплины (используется при каскадном удалении и в тестах). */
    @Query("DELETE FROM grades WHERE discipline_id = :disciplineId")
    suspend fun deleteByDiscipline(disciplineId: Long): Int

    @Query("SELECT * FROM grades WHERE id = :id")
    suspend fun getById(id: Long): Grade?

    @Query("SELECT * FROM grades ORDER BY date DESC, id DESC")
    suspend fun getAll(): List<Grade>

    @Query("SELECT * FROM grades ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE discipline_id = :disciplineId ORDER BY date DESC, id DESC")
    suspend fun getByDiscipline(disciplineId: Long): List<Grade>

    @Query("SELECT * FROM grades WHERE discipline_id = :disciplineId ORDER BY date DESC, id DESC")
    fun observeByDiscipline(disciplineId: Long): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE date BETWEEN :from AND :to ORDER BY date DESC, id DESC")
    suspend fun getByPeriod(from: String, to: String): List<Grade>

    @Query("SELECT COUNT(*) FROM grades")
    suspend fun count(): Int

    /** Средний балл по дисциплине. 0.0, если оценок ещё нет (деления на ноль не происходит). */
    @Query("SELECT COALESCE(AVG(value), 0.0) FROM grades WHERE discipline_id = :disciplineId")
    suspend fun averageForDiscipline(disciplineId: Long): Double

    /** Общий средний балл по всем оценкам. 0.0, если база пуста. */
    @Query("SELECT COALESCE(AVG(value), 0.0) FROM grades")
    suspend fun overallAverage(): Double
}
