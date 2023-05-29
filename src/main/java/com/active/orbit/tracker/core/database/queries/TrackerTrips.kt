package com.active.orbit.tracker.core.database.queries

import androidx.room.*
import com.active.orbit.tracker.core.database.models.TrackerDBTrip

@Dao
interface TrackerTrips {

    @Query("SELECT * FROM trips ORDER BY timeInMillis")
    fun getAll(): List<TrackerDBTrip>

    @Query("SELECT * FROM trips WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<TrackerDBTrip>

    @Query("SELECT * FROM trips WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedBefore(millis: Long): List<TrackerDBTrip>

    @Query("SELECT count(0) FROM trips WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM trips WHERE idTrip == :idTrip LIMIT 1")
    fun getById(idTrip: Int): TrackerDBTrip?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<TrackerDBTrip>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<TrackerDBTrip>)

    fun upsert(model: TrackerDBTrip) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<TrackerDBTrip>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM trips WHERE startTime > :startOfTheDay")
    fun deleteTodayTrips(startOfTheDay: Long)

    @Query("DELETE FROM trips WHERE idTrip == :idTrip")
    fun delete(idTrip: Int)

    @Query("DELETE FROM trips")
    fun truncate()
}