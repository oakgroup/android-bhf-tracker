package com.active.orbit.tracker.core.database.queries

import androidx.room.*
import com.active.orbit.tracker.core.database.models.DBStep

@Dao
interface Steps {

    @Query("SELECT * FROM steps ORDER BY timeInMillis")
    fun getAll(): List<DBStep>

    @Query("SELECT * FROM steps WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis")
    fun getBetween(start: Long, end: Long): List<DBStep>

    @Query("SELECT * FROM steps WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<DBStep>

    @Query("SELECT count(0) FROM steps WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM steps WHERE idStep == :idStep LIMIT 1")
    fun getById(idStep: Int): DBStep?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<DBStep>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<DBStep>)

    fun upsert(model: DBStep) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<DBStep>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM steps WHERE idStep == :idStep")
    fun delete(idStep: Int)

    @Query("DELETE FROM steps")
    fun truncate()
}