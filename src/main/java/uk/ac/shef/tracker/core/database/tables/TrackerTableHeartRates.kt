package uk.ac.shef.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import uk.ac.shef.tracker.core.database.engine.TrackerDatabase
import uk.ac.shef.tracker.core.database.models.TrackerDBHeartRate
import uk.ac.shef.tracker.core.utils.Logger

object TrackerTableHeartRates {

    @WorkerThread
    fun getAll(context: Context): List<TrackerDBHeartRate> {
        try {
            return TrackerDatabase.getInstance(context).getHeartRates().getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all heart rates from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getBetween(context: Context, start: Long, end: Long): List<TrackerDBHeartRate> {
        try {
            return TrackerDatabase.getInstance(context).getHeartRates().getBetween(start, end)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all heart rates between $start and $end from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploaded(context: Context): List<TrackerDBHeartRate> {
        try {
            return TrackerDatabase.getInstance(context).getHeartRates().getNotUploaded()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded heart rates from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploadedCountBefore(context: Context, millis: Long): Int {
        try {
            return TrackerDatabase.getInstance(context).getHeartRates().getNotUploadedCountBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded count before for heart rates from database ${e.localizedMessage}")
        }
        return 0
    }

    @WorkerThread
    fun getById(context: Context, idHeartRate: Int): TrackerDBHeartRate? {
        try {
            return TrackerDatabase.getInstance(context).getHeartRates().getById(idHeartRate)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting heart rate by id $idHeartRate from database ${e.localizedMessage}")
        }
        return null
    }

    @WorkerThread
    fun upsert(context: Context, model: TrackerDBHeartRate) {
        upsert(context, listOf(model))
    }

    @WorkerThread
    fun upsert(context: Context, models: List<TrackerDBHeartRate>) {
        try {
            TrackerDatabase.getInstance(context).getHeartRates().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert heart rates to database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun delete(context: Context, idHeartRate: Int) {
        try {
            TrackerDatabase.getInstance(context).getHeartRates().delete(idHeartRate)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete heart rate with id $idHeartRate from database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun truncate(context: Context) {
        try {
            TrackerDatabase.getInstance(context).getHeartRates().truncate()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on truncate heart rates from database ${e.localizedMessage}")
        }
    }
}