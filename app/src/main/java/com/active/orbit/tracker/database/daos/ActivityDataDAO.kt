package com.active.orbit.tracker.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.active.orbit.tracker.tracker.sensors.activity_recognition.ActivityData

@Dao
interface ActivityDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(activityData: ActivityData?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg activityData: ActivityData?)

    ///////////////////// searching /////////////////////
    @Query("SELECT * FROM ActivityData where timeInMsecs >= :time ORDER BY timeInMsecs LIMIT 1")
    fun getActivityAfter(time: Long): LiveData<ActivityData?>?

    @Query(
        "SELECT * FROM ActivityData where timeInMsecs >= :startTime and timeInMsecs <= :endTime  " +
                " ORDER BY timeInMsecs ASC,  transitionType DESC"
    )
    fun getActivitiesBetween(startTime: Long, endTime: Long): MutableList<ActivityData>

    ///////////////////// data sending queries /////////////////////
    @Query("Update ActivityData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @Query("SELECT * FROM ActivityData WHERE uploaded < 1 ORDER BY timeInMsecs ASC limit :limit")
    fun getUnsentActivities(limit: Int): List<ActivityData?>?

    @Query("SELECT count(0) FROM ActivityData WHERE uploaded < 1 ORDER BY timeInMsecs ASC")
    fun getUnsentCount(): Int

    @Query("SELECT count(0) FROM ActivityData WHERE uploaded < 1 AND timeInMsecs < :millis ORDER BY timeInMsecs ASC")
    fun getUnsentCountBefore(millis: Long): Int

    @get:Query("SELECT * FROM ActivityData WHERE uploaded >= 1 ORDER BY timeInMsecs DESC limit 1")
    val lastSentActivity: ActivityData?

    ///////////////////// others /////////////////////
    @Query("SELECT COUNT(*) FROM ActivityData")
    fun howManyActivitiesInDB(): Int

    ///////////////////// deletion queries /////////////////////
    @Delete
    fun deleteAll(vararg activityData: ActivityData?)

    @Delete
    fun delete(activityData: ActivityData?)
}