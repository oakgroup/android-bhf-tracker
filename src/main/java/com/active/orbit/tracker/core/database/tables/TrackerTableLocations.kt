package com.active.orbit.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.engine.TrackerDatabase
import com.active.orbit.tracker.core.database.models.TrackerDBLocation
import com.active.orbit.tracker.core.utils.Logger

object TrackerTableLocations {

    @WorkerThread
    fun getAll(context: Context): List<TrackerDBLocation> {
        try {
            return TrackerDatabase.getInstance(context).getLocations().getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all locations from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getBetween(context: Context, start: Long, end: Long): List<TrackerDBLocation> {
        try {
            return TrackerDatabase.getInstance(context).getLocations().getBetween(start, end)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all locations between $start and $end from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploaded(context: Context): List<TrackerDBLocation> {
        try {
            return TrackerDatabase.getInstance(context).getLocations().getNotUploaded()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded locations from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploadedCountBefore(context: Context, millis: Long): Int {
        try {
            return TrackerDatabase.getInstance(context).getLocations().getNotUploadedCountBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded count before for locations from database ${e.localizedMessage}")
        }
        return 0
    }

    @WorkerThread
    fun getById(context: Context, idLocation: Int): TrackerDBLocation? {
        try {
            return TrackerDatabase.getInstance(context).getLocations().getById(idLocation)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting location by id $idLocation from database ${e.localizedMessage}")
        }
        return null
    }

    @WorkerThread
    fun upsert(context: Context, model: TrackerDBLocation) {
        upsert(context, listOf(model))
    }

    @WorkerThread
    fun upsert(context: Context, models: List<TrackerDBLocation>) {
        try {
            TrackerDatabase.getInstance(context).getLocations().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert locations to database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun delete(context: Context, idLocation: Int) {
        try {
            TrackerDatabase.getInstance(context).getLocations().delete(idLocation)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete location with id $idLocation from database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun truncate(context: Context) {
        try {
            TrackerDatabase.getInstance(context).getLocations().truncate()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on truncate locations from database ${e.localizedMessage}")
        }
    }
}