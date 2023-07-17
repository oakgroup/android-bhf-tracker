package uk.ac.shef.tracker.core.database.queries

import androidx.room.*
import uk.ac.shef.tracker.core.database.models.TrackerDBBattery

@Dao
interface TrackerBatteries {

    @Query("SELECT * FROM batteries ORDER BY timeInMillis")
    fun getAll(): List<TrackerDBBattery>

    @Query("SELECT * FROM batteries WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis")
    fun getBetween(start: Long, end: Long): List<TrackerDBBattery>

    @Query("SELECT * FROM batteries WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<TrackerDBBattery>

    @Query("SELECT count(0) FROM batteries WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM batteries WHERE idBattery == :idBattery LIMIT 1")
    fun getById(idBattery: Int): TrackerDBBattery?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<TrackerDBBattery>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<TrackerDBBattery>)

    fun upsert(model: TrackerDBBattery) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<TrackerDBBattery>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM batteries WHERE idBattery == :idBattery")
    fun delete(idBattery: Int)

    @Query("DELETE FROM batteries")
    fun truncate()
}