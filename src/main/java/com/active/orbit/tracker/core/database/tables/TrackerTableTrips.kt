package com.active.orbit.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.engine.TrackerDatabase
import com.active.orbit.tracker.core.database.models.TrackerDBTrip
import com.active.orbit.tracker.core.utils.Logger

object TrackerTableTrips {

    @WorkerThread
    fun getAll(context: Context): List<TrackerDBTrip> {
        try {
            return TrackerDatabase.getInstance(context).getTrips().getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all trips from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploaded(context: Context): List<TrackerDBTrip> {
        try {
            return TrackerDatabase.getInstance(context).getTrips().getNotUploaded()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded trips from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploadedCountBefore(context: Context, millis: Long): Int {
        try {
            return TrackerDatabase.getInstance(context).getTrips().getNotUploadedCountBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded count before for trips from database ${e.localizedMessage}")
        }
        return 0
    }

    @WorkerThread
    fun getById(context: Context, idTrip: Int): TrackerDBTrip? {
        try {
            return TrackerDatabase.getInstance(context).getTrips().getById(idTrip)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting trip by id $idTrip from database ${e.localizedMessage}")
        }
        return null
    }

    @WorkerThread
    fun upsert(context: Context, model: TrackerDBTrip) {
        upsert(context, listOf(model))
    }

    @WorkerThread
    fun upsert(context: Context, models: List<TrackerDBTrip>) {
        try {
            TrackerDatabase.getInstance(context).getTrips().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert trips to database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun delete(context: Context, idTrip: Int) {
        try {
            TrackerDatabase.getInstance(context).getTrips().delete(idTrip)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete trip with id $idTrip from database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun truncate(context: Context) {
        try {
            TrackerDatabase.getInstance(context).getTrips().truncate()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on truncate trips from database ${e.localizedMessage}")
        }
    }
}