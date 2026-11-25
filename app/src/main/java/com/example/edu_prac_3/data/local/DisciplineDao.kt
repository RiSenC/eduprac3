package com.example.edu_prac_3.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO для дисциплин (таблица `disciplines`): операции CRUD и запрос связи 1 – N.
 *
 * Все методы записи — suspend: Room выполняет их вне главного потока.
 * Методы наблюдения возвращают [Flow] и обновляются сами при изменении таблицы.
 */
@Dao
interface DisciplineDao {

    /** Возвращает id созданной записи. При дубликате названия бросает SQLiteConstraintException. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(discipline: Discipline): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(disciplines: List<Discipline>): List<Long>

    @Update
    suspend fun update(discipline: Discipline): Int

    @Delete
    suspend fun delete(discipline: Discipline): Int

    @Query("DELETE FROM disciplines WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM disciplines WHERE id = :id")
    suspend fun getById(id: Long): Discipline?

    @Query("SELECT * FROM disciplines WHERE name = :name COLLATE NOCASE")
    suspend fun findByName(name: String): Discipline?

    /** Список дисциплин по алфавиту — так выглядит главный экран по требованиям. */
    @Query("SELECT * FROM disciplines ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<Discipline>

    @Query("SELECT * FROM disciplines ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Discipline>>

    @Query("SELECT COUNT(*) FROM disciplines")
    suspend fun count(): Int

    /** Количество оценок по дисциплине: нужно, чтобы предупредить перед удалением. */
    @Query("SELECT COUNT(*) FROM grades WHERE discipline_id = :disciplineId")
    suspend fun gradeCount(disciplineId: Long): Int

    /** Дисциплины вместе с их оценками одним запросом (@Relation). */
    @Transaction
    @Query("SELECT * FROM disciplines ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllWithGrades(): List<DisciplineWithGrades>

    @Transaction
    @Query("SELECT * FROM disciplines WHERE id = :id")
    suspend fun getWithGrades(id: Long): DisciplineWithGrades?
}
