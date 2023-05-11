package com.active.orbit.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.engine.Database
import com.active.orbit.tracker.core.database.models.DBStep
import com.active.orbit.tracker.core.utils.Logger

object TableSteps {

    @WorkerThread
    fun getAll(context: Context): List<DBStep> {
        try {
            return Database.getInstance(context).getSteps().getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all steps from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getBetween(context: Context, start: Long, end: Long): List<DBStep> {
        try {
            return Database.getInstance(context).getSteps().getBetween(start, end)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting all steps between $start and $end from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploaded(context: Context): List<DBStep> {
        try {
            return Database.getInstance(context).getSteps().getNotUploaded()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded steps from database ${e.localizedMessage}")
        }
        return arrayListOf()
    }

    @WorkerThread
    fun getNotUploadedCountBefore(context: Context, millis: Long): Int {
        try {
            return Database.getInstance(context).getSteps().getNotUploadedCountBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded count before for steps from database ${e.localizedMessage}")
        }
        return 0
    }

    @WorkerThread
    fun getById(context: Context, idStep: Int): DBStep? {
        try {
            return Database.getInstance(context).getSteps().getById(idStep)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting step by id $idStep from database ${e.localizedMessage}")
        }
        return null
    }

    @WorkerThread
    fun upsert(context: Context, model: DBStep) {
        upsert(context, listOf(model))
    }

    @WorkerThread
    fun upsert(context: Context, models: List<DBStep>) {
        try {
            Database.getInstance(context).getSteps().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert steps to database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun delete(context: Context, idStep: Int) {
        try {
            Database.getInstance(context).getSteps().delete(idStep)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete step with id $idStep from database ${e.localizedMessage}")
        }
    }

    @WorkerThread
    fun truncate(context: Context) {
        try {
            Database.getInstance(context).getSteps().truncate()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on truncate steps from database ${e.localizedMessage}")
        }
    }
}