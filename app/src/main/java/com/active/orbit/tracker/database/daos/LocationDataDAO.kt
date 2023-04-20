package com.active.orbit.tracker.database.daos

import androidx.room.*
import com.active.orbit.tracker.tracker.sensors.location_recognition.LocationData

@Dao
interface LocationDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(locationData: LocationData?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg locationData: LocationData?)

    @Query("SELECT * FROM LocationData where timeInMsecs> :timeInMsecs ORDER BY timeInMsecs ASC LIMIT 1")
    fun getLocationAfter(timeInMsecs: Long): LocationData?

    @Query("SELECT * FROM LocationData where timeInMsecs <= :timeInMsecs ORDER BY timeInMsecs DESC LIMIT 1")
    fun getLocationAt(timeInMsecs: Long): LocationData?

    @Query("SELECT * FROM LocationData where timeInMsecs >= :starttimeInMsecs and timeInMsecs <= :endtimeInMsecs ORDER BY timeInMsecs ASC")
    fun getLocationsBetween(starttimeInMsecs: Long, endtimeInMsecs: Long): MutableList<LocationData>?

    @Query("SELECT * FROM LocationData where timeInMsecs >= :midnight and timeInMsecs <= :nextMidnight ORDER BY timeInMsecs DESC LIMIT 1")
    fun getLastLocation(midnight: Long, nextMidnight: Long): LocationData?

    @Query("SELECT * FROM LocationData where uploaded <1 ORDER BY timeInMsecs ASC LIMIT :limit")
    fun getUnsentLocations(limit: Int): List<LocationData?>?

    @Query("SELECT count(0) FROM LocationData WHERE uploaded < 1 ORDER BY timeInMsecs ASC")
    fun getUnsentCount(): Int

    @Query("SELECT count(0) FROM LocationData WHERE uploaded < 1 AND timeInMsecs < :millis ORDER BY timeInMsecs ASC")
    fun getUnsentCountBefore(millis: Long): Int

    @Query("Update LocationData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @get:Query("SELECT * FROM LocationData where uploaded >= 1 ORDER BY timeInMsecs DESC LIMIT 1")
    val lastSentLocation: LocationData?

    @Delete
    fun delete(locationData: LocationData?)

    @Delete
    fun deleteAll(vararg locationData: LocationData?)

    ///////////////////// others ///////////////////// 
    @Query("SELECT COUNT(*) FROM LocationData")
    fun howManyLocationsIDB(): Int
}