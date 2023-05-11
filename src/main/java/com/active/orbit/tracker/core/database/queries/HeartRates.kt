package com.active.orbit.tracker.core.database.queries

import androidx.room.*
import com.active.orbit.tracker.core.database.models.DBHeartRate

@Dao
interface HeartRates {

    @Query("SELECT * FROM heart_rates ORDER BY timeInMillis")
    fun getAll(): List<DBHeartRate>

    @Query("SELECT * FROM heart_rates WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis")
    fun getBetween(start: Long, end: Long): List<DBHeartRate>

    @Query("SELECT * FROM heart_rates WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<DBHeartRate>

    @Query("SELECT count(0) FROM heart_rates WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM heart_rates WHERE idHeartRate == :idHeartRate LIMIT 1")
    fun getById(idHeartRate: Int): DBHeartRate?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<DBHeartRate>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<DBHeartRate>)

    fun upsert(model: DBHeartRate) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<DBHeartRate>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM heart_rates WHERE idHeartRate == :idHeartRate")
    fun delete(idHeartRate: Int)

    @Query("DELETE FROM heart_rates")
    fun truncate()
}