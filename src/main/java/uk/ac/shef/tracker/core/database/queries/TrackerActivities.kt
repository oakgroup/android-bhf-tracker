/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.queries

import androidx.room.*
import uk.ac.shef.tracker.core.database.models.TrackerDBActivity

/**
 * Database queries for the activities table
 */
@Dao
interface TrackerActivities {

    @Query("SELECT * FROM activities ORDER BY timeInMillis")
    fun getAll(): List<TrackerDBActivity>

    @Query("SELECT * FROM activities WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis, transitionType DESC")
    fun getBetween(start: Long, end: Long): List<TrackerDBActivity>

    @Query("SELECT * FROM activities WHERE uploaded = 0 ORDER BY timeInMillis LIMIT 300")
    fun getNotUploaded(): List<TrackerDBActivity>

    @Query("SELECT count(0) FROM activities WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM activities WHERE idActivity == :idActivity LIMIT 1")
    fun getById(idActivity: Int): TrackerDBActivity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<TrackerDBActivity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<TrackerDBActivity>)

    @Query("DELETE FROM activities WHERE idActivity == :idActivity")
    fun delete(idActivity: Int)

    @Query("DELETE FROM activities")
    fun truncate()
}