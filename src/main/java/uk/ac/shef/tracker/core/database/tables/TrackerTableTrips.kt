/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.tables

import android.content.Context
import androidx.annotation.WorkerThread
import uk.ac.shef.tracker.core.database.engine.TrackerDatabase
import uk.ac.shef.tracker.core.database.models.TrackerDBTrip
import uk.ac.shef.tracker.core.database.queries.TrackerTrips
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.TimeUtils

/**
 * Utility class to execute the queries for the trips table handling the exceptions with a specific [Logger] message
 * This class should be used for all the operations, do not access directly the [TrackerTrips] dao
 */
object TrackerTableTrips {

    /**
     * @param context an instance of [Context]
     * @return all the models in the database table
     */
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

    /**
     * Get all the models in the database that have not been uploaded
     *
     * @param context an instance of [Context]
     * @return all the models found
     */
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

    /**
     * Get the models in the database that have not been uploaded
     * Searching only for models before a specific timestamp
     *
     * @param context an instance of [Context]
     * @param millis the models that have a greater timestamp than this won't be considered
     * @return all the models found
     */
    @WorkerThread
    fun getNotUploadedBefore(context: Context, millis: Long): List<TrackerDBTrip> {
        try {
            return TrackerDatabase.getInstance(context).getTrips().getNotUploadedBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded trips before $millis from database ${e.localizedMessage}")
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
            return TrackerDatabase.getInstance(context).getTrips().getNotUploadedCountBefore(millis)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on getting not uploaded count before for trips from database ${e.localizedMessage}")
        }
        return 0
    }

    /**
     * Get the model by the identifier
     *
     * @param context an instance of [Context]
     * @param idTrip the model identifier
     * @return the model if found
     */
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

    /**
     * Insert a model into the database
     * If the model already exists, according to its primary key and indices, the model will be updated
     *
     * @param context an instance of [Context]
     * @param model the model to insert/update
     */
    @WorkerThread
    fun upsert(context: Context, model: TrackerDBTrip) {
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
    fun upsert(context: Context, models: List<TrackerDBTrip>) {
        try {
            TrackerDatabase.getInstance(context).getTrips().upsert(models)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on upsert trips to database ${e.localizedMessage}")
        }
    }

    /**
     * Delete all the today trips from the database
     *
     * @param context an instance of [Context]
     */
    @WorkerThread
    fun deleteTodayTrips(context: Context) {
        try {
            val currentMidnight = TimeUtils.midnightInMsecs(System.currentTimeMillis())
            TrackerDatabase.getInstance(context).getTrips().deleteTodayTrips(currentMidnight)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete today trips from database ${e.localizedMessage}")
        }
    }

    /**
     * Delete a model from the database
     *
     * @param context an instance of [Context]
     * @param idTrip the model identifier
     */
    @WorkerThread
    fun delete(context: Context, idTrip: Int) {
        try {
            TrackerDatabase.getInstance(context).getTrips().delete(idTrip)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on delete trip with id $idTrip from database ${e.localizedMessage}")
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
            TrackerDatabase.getInstance(context).getTrips().truncate()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("Error on truncate trips from database ${e.localizedMessage}")
        }
    }
}