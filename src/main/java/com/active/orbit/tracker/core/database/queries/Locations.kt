package com.active.orbit.tracker.core.database.queries

import androidx.room.*
import com.active.orbit.tracker.core.database.models.DBLocation

@Dao
interface Locations {

    @Query("SELECT * FROM locations ORDER BY timeInMillis")
    fun getAll(): List<DBLocation>

    @Query("SELECT * FROM locations WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis")
    fun getBetween(start: Long, end: Long): List<DBLocation>

    @Query("SELECT * FROM locations WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<DBLocation>

    @Query("SELECT count(0) FROM locations WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM locations WHERE idLocation == :idLocation LIMIT 1")
    fun getById(idLocation: Int): DBLocation?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<DBLocation>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<DBLocation>)

    fun upsert(model: DBLocation) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<DBLocation>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM locations WHERE idLocation == :idLocation")
    fun delete(idLocation: Int)

    @Query("DELETE FROM locations")
    fun truncate()
}