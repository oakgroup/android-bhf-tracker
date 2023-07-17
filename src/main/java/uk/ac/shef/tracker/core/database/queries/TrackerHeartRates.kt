package uk.ac.shef.tracker.core.database.queries

import androidx.room.*
import uk.ac.shef.tracker.core.database.models.TrackerDBHeartRate

@Dao
interface TrackerHeartRates {

    @Query("SELECT * FROM heart_rates ORDER BY timeInMillis")
    fun getAll(): List<TrackerDBHeartRate>

    @Query("SELECT * FROM heart_rates WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis")
    fun getBetween(start: Long, end: Long): List<TrackerDBHeartRate>

    @Query("SELECT * FROM heart_rates WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<TrackerDBHeartRate>

    @Query("SELECT count(0) FROM heart_rates WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM heart_rates WHERE idHeartRate == :idHeartRate LIMIT 1")
    fun getById(idHeartRate: Int): TrackerDBHeartRate?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<TrackerDBHeartRate>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<TrackerDBHeartRate>)

    fun upsert(model: TrackerDBHeartRate) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<TrackerDBHeartRate>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM heart_rates WHERE idHeartRate == :idHeartRate")
    fun delete(idHeartRate: Int)

    @Query("DELETE FROM heart_rates")
    fun truncate()
}