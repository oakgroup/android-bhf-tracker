package uk.ac.shef.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import uk.ac.shef.tracker.core.database.engine.TrackerDatabase
import uk.ac.shef.tracker.core.database.models.TrackerDBActivity
import uk.ac.shef.tracker.core.utils.Logger

object TrackerTableActivities {

    @WorkerThread
    fun getAll(context: Context): List<TrackerDBActivity> {
        try {
            return TrackerDatabase.getInstance(context).getActivities().getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all activities from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getBetween(context: Context, start: Long, end: Long): List<TrackerDBActivity> {
        try {
            return TrackerDatabase.getInstance(context).getActivities().getBetween(start, end)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all activities between $start and $end from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploaded(context: Context): List<TrackerDBActivity> {
        try {
            return TrackerDatabase.getInstance(context).getActivities().getNotUploaded()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded activities from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploadedCountBefore(context: Context, millis: Long): Int {
        try {
            return TrackerDatabase.getInstance(context).getActivities().getNotUploadedCountBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded count before for activities from database ${e.localizedMessage}")
        }
        return 0
    }

    @WorkerThread
    fun getById(context: Context, idActivity: Int): TrackerDBActivity? {
        try {
            return TrackerDatabase.getInstance(context).getActivities().getById(idActivity)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting activity by id $idActivity from database ${e.localizedMessage}")
        }
        return null
    }

    @WorkerThread
    fun upsert(context: Context, model: TrackerDBActivity) {
        upsert(context, listOf(model))
    }

    @WorkerThread
    fun upsert(context: Context, models: List<TrackerDBActivity>) {
        try {
            TrackerDatabase.getInstance(context).getActivities().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert activities to database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun delete(context: Context, idActivity: Int) {
        try {
            TrackerDatabase.getInstance(context).getActivities().delete(idActivity)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete activity with id $idActivity from database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun truncate(context: Context) {
        try {
            TrackerDatabase.getInstance(context).getActivities().truncate()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on truncate activities from database ${e.localizedMessage}")
        }
    }
}