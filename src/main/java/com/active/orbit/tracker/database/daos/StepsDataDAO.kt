package com.active.orbit.tracker.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.active.orbit.tracker.tracker.sensors.step_counting.StepsData

@Dao
interface StepsDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stepsData: StepsData?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg stepsData: StepsData?)

    @Query("SELECT * FROM StepsData WHERE timeInMsecs >= 0 limit 1")
    fun getFirstSteps(): StepsData?

    @Query("SELECT * FROM StepsData WHERE timeInMsecs >= :startTime and timeInMsecs <= :endTime ORDER BY timeInMsecs ASC")
    fun getStepsBetween(startTime: Long, endTime: Long): MutableList<StepsData>?

    @Query("SELECT * FROM StepsData WHERE timeInMsecs >= :time ORDER BY timeInMsecs ASC LIMIT 1")
    fun getStepsAfter(time: Long): LiveData<StepsData?>?

    @Query("SELECT COUNT(*) FROM StepsData")
    fun howManyElementsInDB(): Int

    @Query("SELECT * FROM StepsData WHERE uploaded <1 ORDER BY timeInMsecs ASC limit :limit")
    fun getUnsentSteps(limit: Int): List<StepsData?>?

    @Query("SELECT count(0) FROM StepsData WHERE uploaded < 1 ORDER BY timeInMsecs ASC")
    fun getUnsentCount(): Int

    @Query("SELECT count(0) FROM StepsData WHERE uploaded < 1 AND timeInMsecs < :millis ORDER BY timeInMsecs ASC")
    fun getUnsentCountBefore(millis: Long): Int

    @Query("Update StepsData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @get:Query("SELECT * FROM StepsData WHERE uploaded >= 1 ORDER BY timeInMsecs DESC limit 1")
    val lastSentSteps: StepsData?

    @Delete
    fun delete(stepsData: StepsData?)

    @Delete
    fun deleteAll(vararg stepsData: StepsData?)
}