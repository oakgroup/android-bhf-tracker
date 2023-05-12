package com.active.orbit.tracker.core.computation

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.models.*
import com.active.orbit.tracker.core.database.tables.*
import com.active.orbit.tracker.core.preferences.engine.Preferences
import com.active.orbit.tracker.core.tracker.TrackerService
import com.active.orbit.tracker.core.utils.LocationUtilities
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.TimeUtils
import com.google.android.gms.location.ActivityTransition

class DailyComputation(private val context: Context, var startTime: Long, var endTime: Long, private val computeChartResults: Boolean = true) {

    private var activities = mutableListOf<DBActivity>()
    private var batteries = mutableListOf<DBBattery>()
    private var heartRates = mutableListOf<DBHeartRate>()
    private var locations = mutableListOf<DBLocation>()
    private var steps = mutableListOf<DBStep>()

    lateinit var mobilityResultComputation: MobilityResultComputation

    /**
     * This computes the results in an asynchronous way and sets the live data
     */
    fun computeResultsAsync() {
        backgroundThread {
            computeResults()
        }
    }

    @WorkerThread
    private fun computeResults() {
        Logger.d("Computing day results for ${TimeUtils.formatMillis(startTime, "dd/MM/yyyy")}")
        activities = collectActivitiesFromDatabase(TrackerService.currentTracker)
        batteries = collectBatteriesFromDatabase()
        heartRates = collectHeartRatesFromDatabase(TrackerService.currentTracker)
        locations = collectLocationsFromDatabase(TrackerService.currentTracker)
        steps = collectStepsFromDatabase(TrackerService.currentTracker)
        if (computeChartResults) {
            val cleanLocations = cleanLocationsList(locations)
            mobilityResultComputation = MobilityResultComputation(context, steps, cleanLocations, activities)
            mobilityResultComputation.computeResults()
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
        mobilityResultComputation = MobilityResultComputation(context, steps, locations, activities)
    }

    /**
     * This gets the activities from the database, it gets the activities from the temp list and it combines them
     * then it adds a copy of the current open activity with endtime - currentTime
     * @param currentTracker the tracker so to get the activity recogniser module
     * @return a list of activities
     */
    private fun collectActivitiesFromDatabase(currentTracker: TrackerService?): MutableList<DBActivity> {
        val models = arrayListOf<DBActivity>()
        models.addAll(TableActivities.getBetween(context, startTime, endTime))
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
    private fun normaliseActivityFlow(activities: MutableList<DBActivity>) {
        var prevActivity: DBActivity? = null
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

    private fun collectBatteriesFromDatabase(): MutableList<DBBattery> {
        return TableBatteries.getBetween(context, startTime, endTime).toMutableList()
    }

    private fun collectHeartRatesFromDatabase(currentTracker: TrackerService?): MutableList<DBHeartRate> {
        val models = arrayListOf<DBHeartRate>()
        models.addAll(TableHeartRates.getBetween(context, startTime, endTime))
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
    private fun collectLocationsFromDatabase(currentTracker: TrackerService?): MutableList<DBLocation> {
        val models = arrayListOf<DBLocation>()
        models.addAll(TableLocations.getBetween(context, startTime, endTime))
        if (currentTracker?.locationTracker?.locationsList != null) {
            models.addAll(currentTracker.locationTracker!!.locationsList)
            currentTracker.locationTracker?.flush(context)
        }
        computeSpeedForLocations(models)
        return models
    }

    private fun cleanLocationsList(originalLocations: MutableList<DBLocation>): MutableList<DBLocation> {
        val locUtils = LocationUtilities()
        var locations = locUtils.removeSpikes(originalLocations)
        if (Preferences.tracker(context).useStayPoints) locations = locUtils.identifyStayPoint(locations)
        if (Preferences.tracker(context).compactLocations) locations = locUtils.simplifyLocationsListUsingSED(locations)
        computeSpeedForLocations(locations)
        return locations
    }

    /**
     * This associates the speed between two locations
     * @param locations
     */
    private fun computeSpeedForLocations(locations: MutableList<DBLocation>) {
        var prevLocation: DBLocation? = null
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
    private fun collectStepsFromDatabase(currentTracker: TrackerService?): MutableList<DBStep> {
        val models = arrayListOf<DBStep>()
        models.addAll(TableSteps.getBetween(context, startTime, endTime))
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
    private fun computeCadenceForSteps(steps: MutableList<DBStep>) {
        var prevStepsData: DBStep? = null
        for (stepData in steps) {
            if (prevStepsData != null) {
                stepData.cadence = stepData.computeCadence(prevStepsData)
            }
            prevStepsData = stepData
        }
    }
}