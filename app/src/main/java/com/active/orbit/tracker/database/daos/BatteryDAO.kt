package com.active.orbit.tracker.database.daos

import androidx.room.*
import com.active.orbit.tracker.tracker.sensors.batteries.BatteryData

@Dao
interface BatteryDAO {

    @Delete
    fun delete(BatteryData: BatteryData?)

    @Query("SELECT * FROM BatteryData where timeInMsecs >= :startTime AND timeInMsecs <= :endTime ORDER BY timeInMsecs DESC LIMIT 500")
    fun deleteAll(startTime: Long, endTime: Long): Int

    @Delete
    fun deleteAll(vararg BatteryData: BatteryData?)

    ///////////////////// insertion queries /////////////////////
    @Insert
    fun insertAll(vararg BatteryData: BatteryData?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(BatteryData: BatteryData?)

    ///////////////////// search queries /////////////////////
    @Query("SELECT * FROM BatteryData where timeInMsecs >= :startTime and timeInMsecs <= :endTime ORDER BY timeInMsecs DESC LIMIT 1000")
    fun getBatteryDataBetween(startTime: Long, endTime: Long): MutableList<BatteryData>?

    @Query("SELECT * FROM BatteryData ORDER BY timeInMsecs DESC LIMIT 1")
    fun getLastBatteryData(): BatteryData?

    @Query("Update BatteryData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @Query("SELECT * FROM BatteryData where uploaded <1 ORDER BY timeInMsecs ASC LIMIT :limit")
    fun getUnsentBatteryData(limit: Int): MutableList<BatteryData?>?

    @Query("SELECT count(0) FROM BatteryData WHERE uploaded < 1 ORDER BY timeInMsecs ASC")
    fun getUnsentCount(): Int

    @Query("SELECT count(0) FROM BatteryData WHERE uploaded < 1 AND timeInMsecs < :millis ORDER BY timeInMsecs ASC")
    fun getUnsentCountBefore(millis: Long): Int
}