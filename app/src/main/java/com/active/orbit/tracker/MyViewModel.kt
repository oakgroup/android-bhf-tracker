package com.active.orbit.tracker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.active.orbit.tracker.retrieval.MobilityResultComputation
import com.active.orbit.tracker.retrieval.data.TripData
import com.active.orbit.tracker.tracker.TrackerService
import com.active.orbit.tracker.tracker.sensors.activity_recognition.ActivityData
import com.active.orbit.tracker.tracker.sensors.heart_rate_monitor.HeartRateData
import com.active.orbit.tracker.tracker.sensors.location_recognition.LocationData
import com.active.orbit.tracker.tracker.sensors.step_counting.StepsData

class MyViewModel(application: Application) : AndroidViewModel(application) {

    var stepsDataList: MutableLiveData<List<StepsData>> = MutableLiveData()
    var heartRates: MutableLiveData<List<HeartRateData>> = MutableLiveData()
    var activitiesDataList: MutableLiveData<List<ActivityData>> = MutableLiveData()
    var locationsDataList: MutableLiveData<List<LocationData>> = MutableLiveData()

    var currentHeartRate: MutableLiveData<Int>?

    var mobilityChart: MutableLiveData<MobilityResultComputation>?
    var tripsList: MutableLiveData<List<TripData>>?

    companion object {
        var repository: Repository? = null
    }

    init {
        // creation and connection to the Repository
        repository =
            if (Repository.getInstance(application) == null)
                Repository(application)
            else Repository.getInstance(application)

        currentHeartRate = repository?.currentHeartRate
        mobilityChart = repository?.mobilityChart
        tripsList = repository?.tripsList
    }

    fun setActivitiesDataList(currentActivities: List<ActivityData>?) {
        try {
            this.activitiesDataList.value = currentActivities
        } catch (error: Exception) {
            Log.i("XXX", error.message!!)
        }
    }

    fun setLocationsDataList(currentLocations: List<LocationData>?) {
        this.locationsDataList.value = currentLocations
    }

    fun setActivityChart(chart: MobilityResultComputation?) {
        this.mobilityChart?.value = chart
    }

    fun setTripsList(tripsList: List<TripData>?) {
        this.tripsList?.value = tripsList
    }

    fun setStepsDataList(currentSteps: List<StepsData>?) {
        this.stepsDataList.value = currentSteps
    }

    fun setHeartRates(heartRatesList: List<HeartRateData>?) {
        this.heartRates.value = heartRatesList
    }

    fun keepFlushingToDB(flush: Boolean) {
        TrackerService.currentTracker?.keepFlushingToDB(flush)
    }
}