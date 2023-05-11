package com.active.orbit.tracker.core.database.queries

import androidx.room.*
import com.active.orbit.tracker.core.database.models.DBTrip

@Dao
interface Trips {

    @Query("SELECT * FROM trips ORDER BY timeInMillis")
    fun getAll(): List<DBTrip>

    @Query("SELECT * FROM trips WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<DBTrip>

    @Query("SELECT count(0) FROM trips WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM trips WHERE idTrip == :idTrip LIMIT 1")
    fun getById(idTrip: Int): DBTrip?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<DBTrip>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<DBTrip>)

    fun upsert(model: DBTrip) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<DBTrip>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM trips WHERE idTrip == :idTrip")
    fun delete(idTrip: Int)

    @Query("DELETE FROM trips")
    fun truncate()
}