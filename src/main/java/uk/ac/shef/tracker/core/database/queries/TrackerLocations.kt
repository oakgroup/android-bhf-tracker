/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.queries

import androidx.room.*
import uk.ac.shef.tracker.core.database.models.TrackerDBLocation

/**
 * Database queries for the locations table
 */
@Dao
interface TrackerLocations {

    @Query("SELECT * FROM locations ORDER BY timeInMillis")
    fun getAll(): List<TrackerDBLocation>

    @Query("SELECT * FROM locations WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis")
    fun getBetween(start: Long, end: Long): List<TrackerDBLocation>

    @Query("SELECT * FROM locations WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<TrackerDBLocation>

    @Query("SELECT count(0) FROM locations WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM locations WHERE idLocation == :idLocation LIMIT 1")
    fun getById(idLocation: Int): TrackerDBLocation?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<TrackerDBLocation>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<TrackerDBLocation>)

    fun upsert(model: TrackerDBLocation) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<TrackerDBLocation>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM locations WHERE idLocation == :idLocation")
    fun delete(idLocation: Int)

    @Query("DELETE FROM locations")
    fun truncate()
}