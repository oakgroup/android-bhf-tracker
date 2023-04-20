package com.active.orbit.tracker.database.daos

import androidx.room.*
import com.active.orbit.tracker.tracker.sensors.heart_rate_monitor.HeartRateData

@Dao
interface HeartRateDAO {

    @Delete
    fun delete(heartRateData: HeartRateData?)

    @Query("SELECT * FROM HeartRateData where timeInMsecs >= :startTime AND timeInMsecs <= :endTime ORDER BY timeInMsecs DESC LIMIT 500")
    fun deleteAll(startTime: Long, endTime: Long): Int

    @Delete
    fun deleteAll(vararg heartRateData: HeartRateData?)

    @Insert
    fun insertAll(vararg heartRateData: HeartRateData?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(heartRateData: HeartRateData?)

    @Query("SELECT * FROM HeartRateData where timeInMsecs >= :startTime and timeInMsecs <= :endTime ORDER BY timeInMsecs DESC LIMIT 1000")
    fun getHeartRateBetween(startTime: Long, endTime: Long): MutableList<HeartRateData>?

    @Query("SELECT * FROM HeartRateData where timeInMsecs <= :time ORDER BY timeInMsecs DESC LIMIT 1")
    fun getLastHeartRate(time: Long): MutableList<HeartRateData?>?

    @Query("Update HeartRateData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @Query("SELECT * FROM HeartRateData where uploaded <1 ORDER BY timeInMsecs ASC LIMIT :limit")
    fun getUnsentHeartRateData(limit: Int): MutableList<HeartRateData?>?

    @Query("SELECT count(0) FROM HeartRateData WHERE uploaded < 1 ORDER BY timeInMsecs ASC")
    fun getUnsentCount(): Int

    @Query("SELECT count(0) FROM HeartRateData WHERE uploaded < 1 AND timeInMsecs < :millis ORDER BY timeInMsecs ASC")
    fun getUnsentCountBefore(millis: Long): Int
}