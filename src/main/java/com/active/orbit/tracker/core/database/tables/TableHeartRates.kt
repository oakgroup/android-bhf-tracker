package com.active.orbit.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.engine.Database
import com.active.orbit.tracker.core.database.models.DBHeartRate
import com.active.orbit.tracker.core.utils.Logger

object TableHeartRates {

    @WorkerThread
    fun getAll(context: Context): List<DBHeartRate> {
        try {
            return Database.getInstance(context).getHeartRates().getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all heart rates from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getBetween(context: Context, start: Long, end: Long): List<DBHeartRate> {
        try {
            return Database.getInstance(context).getHeartRates().getBetween(start, end)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all heart rates between $start and $end from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploaded(context: Context): List<DBHeartRate> {
        try {
            return Database.getInstance(context).getHeartRates().getNotUploaded()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded heart rates from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploadedCountBefore(context: Context, millis: Long): Int {
        try {
            return Database.getInstance(context).getHeartRates().getNotUploadedCountBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded count before for heart rates from database ${e.localizedMessage}")
        }
        return 0
    }

    @WorkerThread
    fun getById(context: Context, idHeartRate: Int): DBHeartRate? {
        try {
            return Database.getInstance(context).getHeartRates().getById(idHeartRate)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting heart rate by id $idHeartRate from database ${e.localizedMessage}")
        }
        return null
    }

    @WorkerThread
    fun upsert(context: Context, model: DBHeartRate) {
        upsert(context, listOf(model))
    }

    @WorkerThread
    fun upsert(context: Context, models: List<DBHeartRate>) {
        try {
            Database.getInstance(context).getHeartRates().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert heart rates to database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun delete(context: Context, idHeartRate: Int) {
        try {
            Database.getInstance(context).getHeartRates().delete(idHeartRate)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete heart rate with id $idHeartRate from database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun truncate(context: Context) {
        try {
            Database.getInstance(context).getHeartRates().truncate()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on truncate heart rates from database ${e.localizedMessage}")
        }
    }
}