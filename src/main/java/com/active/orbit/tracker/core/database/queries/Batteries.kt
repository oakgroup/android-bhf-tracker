package com.active.orbit.tracker.core.database.queries

import androidx.room.*
import com.active.orbit.tracker.core.database.models.DBBattery

@Dao
interface Batteries {

    @Query("SELECT * FROM batteries ORDER BY timeInMillis")
    fun getAll(): List<DBBattery>

    @Query("SELECT * FROM batteries WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis")
    fun getBetween(start: Long, end: Long): List<DBBattery>

    @Query("SELECT * FROM batteries WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<DBBattery>

    @Query("SELECT count(0) FROM batteries WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM batteries WHERE idBattery == :idBattery LIMIT 1")
    fun getById(idBattery: Int): DBBattery?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<DBBattery>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<DBBattery>)

    fun upsert(model: DBBattery) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<DBBattery>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM batteries WHERE idBattery == :idBattery")
    fun delete(idBattery: Int)

    @Query("DELETE FROM batteries")
    fun truncate()
}