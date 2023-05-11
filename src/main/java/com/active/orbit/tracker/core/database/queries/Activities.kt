package com.active.orbit.tracker.core.database.queries

import androidx.room.*
import com.active.orbit.tracker.core.database.models.DBActivity

@Dao
interface Activities {

    @Query("SELECT * FROM activities ORDER BY timeInMillis")
    fun getAll(): List<DBActivity>

    @Query("SELECT * FROM activities WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis, transitionType DESC")
    fun getBetween(start: Long, end: Long): List<DBActivity>

    @Query("SELECT * FROM activities WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<DBActivity>

    @Query("SELECT count(0) FROM activities WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM activities WHERE idActivity == :idActivity LIMIT 1")
    fun getById(idActivity: Int): DBActivity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<DBActivity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<DBActivity>)

    fun upsert(model: DBActivity) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<DBActivity>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM activities WHERE idActivity == :idActivity")
    fun delete(idActivity: Int)

    @Query("DELETE FROM activities")
    fun truncate()
}