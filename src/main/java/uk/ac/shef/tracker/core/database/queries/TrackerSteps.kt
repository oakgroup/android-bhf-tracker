/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.queries

import androidx.room.*
import uk.ac.shef.tracker.core.database.models.TrackerDBStep

/**
 * Database queries for the steps table
 */
@Dao
interface TrackerSteps {

    @Query("SELECT * FROM steps ORDER BY timeInMillis")
    fun getAll(): List<TrackerDBStep>

    @Query("SELECT * FROM steps WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis")
    fun getBetween(start: Long, end: Long): List<TrackerDBStep>

    @Query("SELECT * FROM steps WHERE uploaded = 0 ORDER BY timeInMillis LIMIT 300")
    fun getNotUploaded(): List<TrackerDBStep>

    @Query("SELECT count(0) FROM steps WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM steps WHERE idStep == :idStep LIMIT 1")
    fun getById(idStep: Int): TrackerDBStep?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<TrackerDBStep>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<TrackerDBStep>)

    @Query("DELETE FROM steps WHERE idStep == :idStep")
    fun delete(idStep: Int)

    @Query("DELETE FROM steps")
    fun truncate()
}