/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import uk.ac.shef.tracker.core.database.engine.TrackerDatabase
import uk.ac.shef.tracker.core.database.models.TrackerDBHeartRate
import uk.ac.shef.tracker.core.database.queries.TrackerHeartRates
import uk.ac.shef.tracker.core.utils.Logger

/**
 * Utility class to execute the queries for the heart_rates table handling the exceptions with a specific [Logger] message
 * This class should be used for all the operations, do not access directly the [TrackerHeartRates] dao
 */
object TrackerTableHeartRates {

    /**
     * @param context an instance of [Context]
     * @return all the models in the database table
     */
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

    /**
     * Get all the models in the database table between a start and an end time
     *
     * @param context an instance of [Context]
     * @param start the start timestamp
     * @param end the end timestamp
     * @return all the models found
     */
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

    /**
     * Get all the models in the database that have not been uploaded
     *
     * @param context an instance of [Context]
     * @return all the models found
     */
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

    /**
     * Get the count of the models in the database that have not been uploaded
     * Searching only for models before a specific timestamp
     *
     * @param context an instance of [Context]
     * @param millis the models that have a greater timestamp than this won't be considered
     * @return the count of all the models found
     */
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

    /**
     * Get the model by the identifier
     *
     * @param context an instance of [Context]
     * @param idHeartRate the model identifier
     * @return the model if found
     */
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

    /**
     * Insert a model into the database
     * If the model already exists, according to its primary key and indices, the model will be updated
     *
     * @param context an instance of [Context]
     * @param model the model to insert/update
     */
    @WorkerThread
    fun upsert(context: Context, model: TrackerDBHeartRate) {
        upsert(context, listOf(model))
    }

    /**
     * Insert a list of models into the database
     * If the model already exists, according to its primary key and indices, the model will be updated
     *
     * @param context an instance of [Context]
     * @param models the list of models to insert/update
     */
    @WorkerThread
    fun upsert(context: Context, models: List<TrackerDBHeartRate>) {
        try {
            TrackerDatabase.getInstance(context).getHeartRates().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert heart rates to database ${e.localizedMessage}")
        }
    }

    /**
     * Delete a model from the database
     *
     * @param context an instance of [Context]
     * @param idHeartRate the model identifier
     */
    @WorkerThread
    fun delete(context: Context, idHeartRate: Int) {
        try {
            TrackerDatabase.getInstance(context).getHeartRates().delete(idHeartRate)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete heart rate with id $idHeartRate from database ${e.localizedMessage}")
        }
    }

    /**
     * Delete all the models from this database table
     *
     * @param context an instance of [Context]
     */
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