package com.active.orbit.tracker.retrieval

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.active.orbit.tracker.MyViewModel
import com.active.orbit.tracker.tracker.TrackerService
import com.active.orbit.tracker.tracker.sensors.activity_recognition.ActivityData
import com.active.orbit.tracker.tracker.sensors.batteries.BatteryData
import com.active.orbit.tracker.tracker.sensors.heart_rate_monitor.HeartRateData
import com.active.orbit.tracker.tracker.sensors.location_recognition.LocationData
import com.active.orbit.tracker.tracker.sensors.location_recognition.LocationUtilities
import com.active.orbit.tracker.tracker.sensors.step_counting.StepsData
import com.active.orbit.tracker.utils.Globals
import com.active.orbit.tracker.utils.PreferencesStore
import com.active.orbit.tracker.utils.Utils
import com.google.android.gms.location.ActivityTransition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ComputeDayDataAsync(
    private val context: Context,
    private val viewModel: MyViewModel?,
    var startTime: Long,
    var endTime: Long,
    private val computeChartResults: Boolean = true
) {
    companion object {
        private val TAG: String = this::class.simpleName!!
    }

    lateinit var mobilityResultComputation: MobilityResultComputation
    private var locations = mutableListOf<LocationData>()
    private var steps: MutableList<StepsData> = mutableListOf()
    private var activities: MutableList<ActivityData> = mutableListOf()
    private var heartrates: MutableList<HeartRateData>? = mutableListOf()
    private var batteries: MutableList<BatteryData>? = mutableListOf()
    private val repositoryInstance: com.active.orbit.tracker.Repository? =
        com.active.orbit.tracker.Repository.getInstance(context)

    /**
     * it computes the results in an asynchronous way and sets the live data
     */
    fun computeResultsAsync(viewModel: ViewModel) {
        viewModel.viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                computeResults()
                Result.success(true)
            }
            Log.i(TAG, "the call was successful? $success")
            if (success.isSuccess)
                setLiveData()
        }
    }

    fun computeResults() {
        Log.i(TAG, "Starting computing day results")
        Log.i(TAG, "In Context")
        Log.i(TAG, "Computing day results for ${Utils.millisecondsToString(startTime, "dd/MM/yyyy")}")
        steps = collectStepsFromDatabase(TrackerService.currentTracker)
        activities = collectActivitiesFromDatabase(TrackerService.currentTracker)
        locations = collectLocationsFromDatabase(TrackerService.currentTracker)
        heartrates = collectHeartRatesFromDatabase(TrackerService.currentTracker)
        batteries = collectBatteriesFromDatabase(TrackerService.currentTracker)
        if (computeChartResults) {
            val cleanLocations = cleanLocationsList(locations, TrackerService.currentTracker)
            mobilityResultComputation = MobilityResultComputation(context, steps, cleanLocations, activities)
            mobilityResultComputation.computeResults()
        }
        Log.i(TAG, "Finished computing day results")
    }

    /**
     * it resets all the local data Used by the interface when minimised so to reduce the memory
     * occupation and make the app less conspicuous to Android
     *
     */
    fun resetResults() {
        steps = mutableListOf()
        activities = mutableListOf()
        locations = mutableListOf()
        heartrates = mutableListOf()
        batteries = mutableListOf()
        mobilityResultComputation = MobilityResultComputation(context, steps, locations, activities)

    }

    /**
     * it gets the activities from the database, it gets the activities from the temp list and it combines them
     * then it adds a copy of the current open activity with endtime - currentTime
     * @param currentTracker the tracker so to get the activity recogniser module
     * @return a list of activities
     */
    private fun collectActivitiesFromDatabase(currentTracker: TrackerService?): MutableList<ActivityData> {
        val activities: MutableList<ActivityData> =
            repositoryInstance?.dBActivityDao?.getActivitiesBetween(startTime, endTime)!!
        if (currentTracker?.activityRecognition != null) {
            activities.addAll(currentTracker.activityRecognition?.activityDataList!!)
            currentTracker.activityRecognition?.flush(context)
        }
        normaliseActivityFlow(activities)
        return activities
    }

    /**
     * the closing of the current activity may come by chance immediately after the opening of
     * the next one, e.g. 1 msec afterwards just because that is how the event work
     * if this is the case, we swap the two activities
     * @param activities
     * @return
     */
    private fun normaliseActivityFlow(activities: MutableList<ActivityData>) {
        var prevActivity: ActivityData? = null
        for (activity in activities) {
            if (prevActivity?.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER
                && activity.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT
                && activity.timeInMsecs - prevActivity.timeInMsecs < 2000
            ) {
                val prevTime = prevActivity.timeInMsecs
                val time = activity.timeInMsecs
                val temp = activity.copy()
                activity.copyFields(prevActivity)
                // but keep the time the same
                activity.timeInMsecs = time
                prevActivity.copyFields(temp)
                // keep the time the same
                prevActivity.timeInMsecs = prevTime
            }
            prevActivity = activity
        }
    }

    /**
     * it gets the locations from the database, it gets the steps from the temp list and it combines them
     * then it flushes any remaining locations
     * @param currentTracker the tracker so to get the location tracker module
     * @return a list of locations

     */
    private fun collectLocationsFromDatabase(currentTracker: TrackerService?): MutableList<LocationData> {
        val locations: MutableList<LocationData> =
            repositoryInstance?.dBLocationDao?.getLocationsBetween(startTime, endTime)!!
        if (currentTracker?.locationTracker != null) {
            locations.addAll(currentTracker.locationTracker?.locationsDataList!!)
            currentTracker.locationTracker?.flushLocations(context)
        }

        computeSpeedForLocations(locations)
        return locations
    }

    private fun cleanLocationsList(originalLlocations: MutableList<LocationData>, currentTracker: TrackerService?): MutableList<LocationData> {
        val locUtils = LocationUtilities()
        var locations = locUtils.removeSpikes(originalLlocations)
        val userPreferences = PreferencesStore()

        val useStayPoints = userPreferences.getBooleanPreference(
            currentTracker,
            Globals.STAY_POINTS, false
        )
        if (useStayPoints!!)
            locations = locUtils.identifyStayPoint(locations)
        val useGlobalSED = userPreferences.getBooleanPreference(
            currentTracker,
            Globals.COMPACTING_LOCATIONS_GENERAL,
            false
        )
        if (useGlobalSED!!)
            locations = locUtils.simplifyLocationsListUsingSED(locations)
        computeSpeedForLocations(locations)
        return locations
    }

    /**
     * it associates the speed between two locations
     * @param locations
     */
    private fun computeSpeedForLocations(locations: MutableList<LocationData>) {
        var prevLocationData: LocationData? = null
        for (locationData in locations) {
            if (prevLocationData == null)
                prevLocationData = locationData
            else {
                val locationUtils =
                    LocationUtilities()
                locationData.distance =
                    locationUtils.computeDistance(prevLocationData, locationData)
                locationData.speed = locationUtils.computeSpeed(prevLocationData, locationData)
                prevLocationData = locationData
            }
        }
    }


    /**
     * it gets the locations from the database, it gets the steps from the temp list and it combines them
     * then it flushes any remaining locations
     * @param currentTracker the tracker so to get the location tracker module
     * @return a list of locations

     */
    private fun collectHeartRatesFromDatabase(currentTracker: TrackerService?): MutableList<HeartRateData>? {
        val heartRates: MutableList<HeartRateData>? =
            repositoryInstance?.dBHeartRateDao?.getHeartRateBetween(startTime, endTime)!!
        if (currentTracker?.heartMonitor != null) {
            heartRates?.addAll(currentTracker.heartMonitor?.heartRateReadingStack!!)
            currentTracker.heartMonitor?.flush()
        }
        return heartRates
    }

    private fun collectBatteriesFromDatabase(currentTracker: TrackerService?): MutableList<BatteryData>? {
        val batteries: MutableList<BatteryData>? = repositoryInstance?.dBBatteryDao?.getBatteryDataBetween(startTime, endTime)!!
        return batteries
    }

    /**
     * it gets the steps from the database, it gets the steps from the temp list and it combines them
     * then it flushes any remaining locations
     * @param currentTracker the tracker so to get the step counter module
     * @return a list of steps
     */
    private fun collectStepsFromDatabase(currentTracker: TrackerService?): MutableList<StepsData> {
        val steps = repositoryInstance?.dBStepsDao?.getStepsBetween(startTime, endTime)!!
        if (currentTracker?.stepCounter != null) {
            val newStepsList = currentTracker.stepCounter?.stepsDataList
            steps.addAll(newStepsList!!)
            TrackerService.currentTracker?.stepCounter?.flush()
        }
        computeCadenceForSteps(steps)
        return steps
    }

    /**
     * it assigns the cadence based on the sequence of steps
     * @param steps the list of steps for a day
     */
    private fun computeCadenceForSteps(steps: MutableList<StepsData>) {
        var prevStepsData: StepsData? = null
        for (stepData in steps) {
            if (prevStepsData != null) {
                stepData.cadence = stepData.computeCadence(prevStepsData)
            }
            prevStepsData = stepData
        }
    }

    /**
     * it provides the results to the live data for the interface to display if necessary
     */
    private suspend fun setLiveData() {
        Log.i(TAG, "Setting live data")
        withContext(Dispatchers.Main) {
            try {
                if (activities.isNotEmpty()) {
                    viewModel?.setActivitiesDataList(activities)
                }
            } catch (error: Exception) {
                Log.i(TAG, error.message!!)
            }
            try {
                if (steps.isNotEmpty()) {
                    viewModel?.setStepsDataList(steps)
                }
            } catch (error: Exception) {
                Log.i(TAG, error.message!!)
            }
            try {
                if (locations.isNotEmpty()) {
                    viewModel?.setLocationsDataList(locations)
                }
            } catch (error: Exception) {
                Log.i(TAG, error.message!!)
            }
            try {
                if (mobilityResultComputation.chart.isNotEmpty()) {
                    viewModel?.setActivityChart(mobilityResultComputation)
                }
            } catch (error: Exception) {
                Log.i(TAG, error.message!!)
            }

            try {
                if (mobilityResultComputation.trips.isNotEmpty()) {
                    viewModel?.setTripsList(mobilityResultComputation.trips)
                }
            } catch (error: Exception) {
                Log.i(TAG, error.message!!)
            }
            Result.success(true)
        }
    }
}