package uk.ac.shef.tracker.core.database.queries

import androidx.room.*
import uk.ac.shef.tracker.core.database.models.TrackerDBStep

@Dao
interface TrackerSteps {

    @Query("SELECT * FROM steps ORDER BY timeInMillis")
    fun getAll(): List<TrackerDBStep>

    @Query("SELECT * FROM steps WHERE timeInMillis >= :start AND timeInMillis <= :end ORDER BY timeInMillis")
    fun getBetween(start: Long, end: Long): List<TrackerDBStep>

    @Query("SELECT * FROM steps WHERE uploaded = 0 ORDER BY timeInMillis")
    fun getNotUploaded(): List<TrackerDBStep>

    @Query("SELECT count(0) FROM steps WHERE uploaded = 0 AND timeInMillis < :millis ORDER BY timeInMillis")
    fun getNotUploadedCountBefore(millis: Long): Int

    @Query("SELECT * FROM steps WHERE idStep == :idStep LIMIT 1")
    fun getById(idStep: Int): TrackerDBStep?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(models: List<TrackerDBStep>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(models: List<TrackerDBStep>)

    fun upsert(model: TrackerDBStep) {
        upsert(listOf(model))
    }

    @Transaction
    fun upsert(models: List<TrackerDBStep>) {
        insert(models)
        update(models)
    }

    @Query("DELETE FROM steps WHERE idStep == :idStep")
    fun delete(idStep: Int)

    @Query("DELETE FROM steps")
    fun truncate()
}