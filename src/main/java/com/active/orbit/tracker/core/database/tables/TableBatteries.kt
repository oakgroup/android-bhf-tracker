package com.active.orbit.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.engine.Database
import com.active.orbit.tracker.core.database.models.DBBattery
import com.active.orbit.tracker.core.utils.Logger

object TableBatteries {

    @WorkerThread
    fun getAll(context: Context): List<DBBattery> {
        try {
            return Database.getInstance(context).getBatteries().getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all batteries from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getBetween(context: Context, start: Long, end: Long): List<DBBattery> {
        try {
            return Database.getInstance(context).getBatteries().getBetween(start, end)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all batteries between $start and $end from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploaded(context: Context): List<DBBattery> {
        try {
            return Database.getInstance(context).getBatteries().getNotUploaded()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded batteries from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploadedCountBefore(context: Context, millis: Long): Int {
        try {
            return Database.getInstance(context).getBatteries().getNotUploadedCountBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded count before for batteries from database ${e.localizedMessage}")
        }
        return 0
    }

    @WorkerThread
    fun getById(context: Context, idBattery: Int): DBBattery? {
        try {
            return Database.getInstance(context).getBatteries().getById(idBattery)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting battery by id $idBattery from database ${e.localizedMessage}")
        }
        return null
    }

    @WorkerThread
    fun upsert(context: Context, model: DBBattery) {
        upsert(context, listOf(model))
    }

    @WorkerThread
    fun upsert(context: Context, models: List<DBBattery>) {
        try {
            Database.getInstance(context).getBatteries().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert batteries to database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun delete(context: Context, idBattery: Int) {
        try {
            Database.getInstance(context).getBatteries().delete(idBattery)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete battery with id $idBattery from database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun truncate(context: Context) {
        try {
            Database.getInstance(context).getBatteries().truncate()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on truncate batteries from database ${e.localizedMessage}")
        }
    }
}