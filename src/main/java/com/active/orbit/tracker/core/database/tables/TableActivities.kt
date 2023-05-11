package com.active.orbit.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.engine.Database
import com.active.orbit.tracker.core.database.models.DBActivity
import com.active.orbit.tracker.core.utils.Logger

object TableActivities {

    @WorkerThread
    fun getAll(context: Context): List<DBActivity> {
        try {
            return Database.getInstance(context).getActivities().getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all activities from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getBetween(context: Context, start: Long, end: Long): List<DBActivity> {
        try {
            return Database.getInstance(context).getActivities().getBetween(start, end)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all activities between $start and $end from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploaded(context: Context): List<DBActivity> {
        try {
            return Database.getInstance(context).getActivities().getNotUploaded()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded activities from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploadedCountBefore(context: Context, millis: Long): Int {
        try {
            return Database.getInstance(context).getActivities().getNotUploadedCountBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded count before for activities from database ${e.localizedMessage}")
        }
        return 0
    }

    @WorkerThread
    fun getById(context: Context, idActivity: Int): DBActivity? {
        try {
            return Database.getInstance(context).getActivities().getById(idActivity)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting activity by id $idActivity from database ${e.localizedMessage}")
        }
        return null
    }

    @WorkerThread
    fun upsert(context: Context, model: DBActivity) {
        upsert(context, listOf(model))
    }

    @WorkerThread
    fun upsert(context: Context, models: List<DBActivity>) {
        try {
            Database.getInstance(context).getActivities().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert activities to database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun delete(context: Context, idActivity: Int) {
        try {
            Database.getInstance(context).getActivities().delete(idActivity)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete activity with id $idActivity from database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun truncate(context: Context) {
        try {
            Database.getInstance(context).getActivities().truncate()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on truncate activities from database ${e.localizedMessage}")
        }
    }
}