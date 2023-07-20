/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.computation

import android.content.Context
import androidx.annotation.WorkerThread
import com.google.android.gms.location.ActivityTransition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import uk.ac.shef.tracker.core.database.models.TrackerDBActivity
import uk.ac.shef.tracker.core.database.models.TrackerDBBattery
import uk.ac.shef.tracker.core.database.models.TrackerDBHeartRate
import uk.ac.shef.tracker.core.database.models.TrackerDBLocation
import uk.ac.shef.tracker.core.database.models.TrackerDBStep
import uk.ac.shef.tracker.core.database.tables.TrackerTableActivities
import uk.ac.shef.tracker.core.database.tables.TrackerTableBatteries
import uk.ac.shef.tracker.core.database.tables.TrackerTableHeartRates
import uk.ac.shef.tracker.core.database.tables.TrackerTableLocations
import uk.ac.shef.tracker.core.database.tables.TrackerTableSteps
import uk.ac.shef.tracker.core.observers.TrackerObserver
import uk.ac.shef.tracker.core.observers.TrackerObserverType
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.tracker.TrackerService
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.LocationUtilities
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.TimeUtils
import uk.ac.shef.tracker.core.utils.background
import uk.ac.shef.tracker.core.utils.main
import kotlin.coroutines.CoroutineContext

/**
 * Class that performs the computation for the daily activities
 */
class DailyComputation(private val context: Context, var startTime: Long, var endTime: Long, private val computeChartResults: Boolean = true) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private var activities = mutableListOf<TrackerDBActivity>()
    private var batteries = mutableListOf<TrackerDBBattery>()
    private var heartRates = mutableListOf<TrackerDBHeartRate>()
    private var locations = mutableListOf<TrackerDBLocation>()
    private var steps = mutableListOf<TrackerDBStep>()
    private var mobilityComputation = MobilityComputation(context)

    private var trackerObserver: TrackerObserver? = null

    /**
     * This computes the results in an asynchronous way and sets the live data
     */
    fun computeResultsAsync() {
        background {
            computeResults()
        }
    }

    @WorkerThread
    fun computeResults(notifyObserver: Boolean = true) {
        Logger.d("Computing day results for ${TimeUtils.formatMillis(startTime, Constants.DATE_FORMAT_UTC)}")
        activities = collectActivitiesFromDatabase(TrackerService.currentTracker)
        batteries = collectBatteriesFromDatabase()
        heartRates = collectHeartRatesFromDatabase(TrackerService.currentTracker)
        locations = collectLocationsFromDatabase(TrackerService.currentTracker)
        steps = collectStepsFromDatabase(TrackerService.currentTracker)
        if (computeChartResults) {
            val cleanLocations = cleanLocationsList(locations)
            mobilityComputation.steps = steps
            mobilityComputation.locations = cleanLocations
            mobilityComputation.activities = activities
            mobilityComputation.computeResults()
        }

        if (notifyObserver) {
            main {
                // update observer
                trackerObserver?.onTrackerUpdate(TrackerObserverType.ACTIVITIES, activities)
                trackerObserver?.onTrackerUpdate(TrackerObserverType.BATTERIES, batteries)
                trackerObserver?.onTrackerUpdate(TrackerObserverType.HEART_RATES, heartRates)
                trackerObserver?.onTrackerUpdate(TrackerObserverType.LOCATIONS, locations)
                trackerObserver?.onTrackerUpdate(TrackerObserverType.STEPS, steps)
                if (computeChartResults) {
                    trackerObserver?.onTrackerUpdate(TrackerObserverType.MOBILITY, mobilityComputation)
                }
            }
        }
    }

    /**
     * This resets all the local data Used by the interface when minimised so to reduce the memory
     * occupation and make the app less conspicuous to Android
     *
     */
    fun resetResults() {
        activities = mutableListOf()
        batteries = mutableListOf()
        heartRates = mutableListOf()
        locations = mutableListOf()
        steps = mutableListOf()
        mobilityComputation = MobilityComputation(context)
    }

    fun registerObserver(observer: TrackerObserver) {
        trackerObserver = observer
    }

    fun unregisterObserver() {
        trackerObserver = null
    }

    /**
     * This gets the activities from the database, it gets the activities from the temp list and it combines them
     * then it adds a copy of the current open activity with endtime - currentTime
     * @param currentTracker the tracker so to get the activity recogniser module
     * @return a list of activities
     */
    private fun collectActivitiesFromDatabase(currentTracker: TrackerService?): MutableList<TrackerDBActivity> {
        val models = arrayListOf<TrackerDBActivity>()
        models.addAll(TrackerTableActivities.getBetween(context, startTime, endTime))
        if (currentTracker?.activityRecognition?.activitiesList != null) {
            models.addAll(currentTracker.activityRecognition!!.activitiesList)
            currentTracker.activityRecognition?.flush(context)
        }
        normaliseActivityFlow(this.activities)
        return models
    }

    /**
     * The closing of the current activity may come by chance immediately after the opening of
     * the next one, e.g. 1 msec afterwards just because that is how the event work
     * if this is the case, we swap the two activities
     * @param activities
     * @return
     */
    private fun normaliseActivityFlow(activities: MutableList<TrackerDBActivity>) {
        var prevActivity: TrackerDBActivity? = null
        for (activity in activities) {
            if (prevActivity?.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER && activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT && activity.timeInMillis - prevActivity.timeInMillis < 2000) {
                val prevTime = prevActivity.timeInMillis
                val time = activity.timeInMillis
                val temp = activity.copy()
                activity.copyFields(prevActivity)
                // but keep the time the same
                activity.timeInMillis = time
                prevActivity.copyFields(temp)
                // keep the time the same
                prevActivity.timeInMillis = prevTime
            }
            prevActivity = activity
        }
    }

    private fun collectBatteriesFromDatabase(): MutableList<TrackerDBBattery> {
        return TrackerTableBatteries.getBetween(context, startTime, endTime).toMutableList()
    }

    private fun collectHeartRatesFromDatabase(currentTracker: TrackerService?): MutableList<TrackerDBHeartRate> {
        val models = arrayListOf<TrackerDBHeartRate>()
        models.addAll(TrackerTableHeartRates.getBetween(context, startTime, endTime))
        if (currentTracker?.heartMonitor?.heartRateReadingStack != null) {
            models.addAll(currentTracker.heartMonitor!!.heartRateReadingStack)
            currentTracker.heartMonitor?.flush()
        }
        return models
    }

    /**
     * This gets the locations from the database, it gets the steps from the temp list and it combines them
     * then it flushes any remaining locations
     * @param currentTracker the tracker so to get the location tracker module
     * @return a list of locations

     */
    private fun collectLocationsFromDatabase(currentTracker: TrackerService?): MutableList<TrackerDBLocation> {
        val models = arrayListOf<TrackerDBLocation>()
        models.addAll(TrackerTableLocations.getBetween(context, startTime, endTime))
        if (currentTracker?.locationTracker?.locationsList != null) {
            models.addAll(currentTracker.locationTracker!!.locationsList)
            currentTracker.locationTracker?.flush(context)
        }
        computeSpeedForLocations(models)
        return models
    }

    private fun cleanLocationsList(originalLocations: MutableList<TrackerDBLocation>): MutableList<TrackerDBLocation> {
        val locUtils = LocationUtilities()
        var locations = locUtils.removeSpikes(originalLocations)
        if (TrackerPreferences.config(context).useStayPoints) locations = locUtils.identifyStayPoint(locations)
        if (TrackerPreferences.config(context).compactLocations) locations = locUtils.simplifyLocationsListUsingSED(locations)
        computeSpeedForLocations(locations)
        return locations
    }

    /**
     * This associates the speed between two locations
     * @param locations
     */
    private fun computeSpeedForLocations(locations: MutableList<TrackerDBLocation>) {
        var prevLocation: TrackerDBLocation? = null
        for (location in locations) {
            if (prevLocation == null)
                prevLocation = location
            else {
                val locationUtils = LocationUtilities()
                location.distance = locationUtils.computeDistance(prevLocation, location)
                location.speed = locationUtils.computeSpeed(prevLocation, location)
                prevLocation = location
            }
        }
    }

    /**
     * This gets the steps from the database, it gets the steps from the temp list and it combines them
     * then it flushes any remaining locations
     * @param currentTracker the tracker so to get the step counter module
     * @return a list of steps
     */
    private fun collectStepsFromDatabase(currentTracker: TrackerService?): MutableList<TrackerDBStep> {
        val models = arrayListOf<TrackerDBStep>()
        models.addAll(TrackerTableSteps.getBetween(context, startTime, endTime))
        if (currentTracker?.stepCounter?.stepsList != null) {
            models.addAll(currentTracker.stepCounter!!.stepsList)
            currentTracker.stepCounter?.flush()
        }
        computeCadenceForSteps(models)
        return models
    }

    /**
     * This assigns the cadence based on the sequence of steps
     * @param steps the list of steps for a day
     */
    private fun computeCadenceForSteps(steps: MutableList<TrackerDBStep>) {
        var prevStepsData: TrackerDBStep? = null
        for (stepData in steps) {
            if (prevStepsData != null) {
                stepData.cadence = stepData.computeCadence(prevStepsData)
            }
            prevStepsData = stepData
        }
    }
}