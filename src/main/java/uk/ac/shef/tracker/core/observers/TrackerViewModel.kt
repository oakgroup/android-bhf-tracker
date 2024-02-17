/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */
package uk.ac.shef.tracker.core.observers

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import uk.ac.shef.tracker.core.computation.DailyComputation
import uk.ac.shef.tracker.core.computation.MobilityComputation
import uk.ac.shef.tracker.core.database.queries.TrackerActivities
import uk.ac.shef.tracker.core.database.queries.TrackerBatteries
import uk.ac.shef.tracker.core.database.queries.TrackerHeartRates
import uk.ac.shef.tracker.core.database.queries.TrackerLocations
import uk.ac.shef.tracker.core.database.queries.TrackerSteps
import uk.ac.shef.tracker.core.tracker.TrackerManager
import uk.ac.shef.tracker.core.utils.TimeUtils
import java.util.Calendar

class TrackerViewModel(application: Application) : AndroidViewModel(application), TrackerObserver {
    private val _mobilityChart: MutableLiveData<MobilityComputation> = MutableLiveData()
    val mobilityChart: LiveData<MobilityComputation> = _mobilityChart

    private var _activites: MutableLiveData<List<TrackerActivities>> = MutableLiveData()
    val activites: LiveData<List<TrackerActivities>> = _activites

    private var _locations: MutableLiveData<List<TrackerLocations>> = MutableLiveData()
    val locations: LiveData<List<TrackerLocations>> = _locations

    private var _steps: MutableLiveData<List<TrackerSteps>> = MutableLiveData()
    val steps: LiveData<List<TrackerSteps>> = _steps

    private var _heartRates: MutableLiveData<List<TrackerHeartRates>> = MutableLiveData()
    val heartRates: LiveData<List<TrackerHeartRates>> = _heartRates

    private var _batteries: MutableLiveData<List<TrackerBatteries>> = MutableLiveData()
    val batteries: LiveData<List<TrackerBatteries>> = _batteries

    var dailyComputation: DailyComputation? = null

    var context= getApplication<Application>()
    // this val is pointless - just to be able to call the getInstance of the TrackerManager
    private var trackerManager: TrackerManager?= null

    private fun setMobilityChart(mobility_chart: MobilityComputation?) {
            this._mobilityChart.value = mobility_chart
    }
    fun createTrackerManager(appCompatActivity: AppCompatActivity){
        trackerManager= TrackerManager.getInstance(appCompatActivity)
    }

    fun computeResults() {
        trackerManager.let {
            val currentDateTime = trackerManager!!.currentDateTime
            val midnight = TimeUtils.midnightInMsecs(currentDateTime)
            val endOfDay = midnight + TimeUtils.ONE_DAY_MILLIS
            dailyComputation = DailyComputation(context, midnight, endOfDay)
            dailyComputation?.registerObserver(this)
            dailyComputation?.computeResultsAsync()
        }
    }

    override fun onTrackerUpdate(type: TrackerObserverType, data: Any) {
        when (type) {
            TrackerObserverType.MOBILITY ->
                setMobilityChart(data as MobilityComputation)
            TrackerObserverType.ACTIVITIES ->
                _activites.value=data as List<TrackerActivities>
            TrackerObserverType.LOCATIONS ->
                _locations.value=data as List<TrackerLocations>
            TrackerObserverType.HEART_RATES ->
                _heartRates.value=data as List<TrackerHeartRates>
            TrackerObserverType.STEPS ->
                _steps.value=data as List<TrackerSteps>
            TrackerObserverType.BATTERIES ->
                _batteries.value=data as List<TrackerBatteries>
        }
    }

    fun changeResultsDate(selectedDateTime: Calendar){
        trackerManager.let {
            trackerManager!!.currentDateTime = selectedDateTime.timeInMillis
            computeResults()
        }
    }

    fun unregisterObservers(){
        dailyComputation?.unregisterObserver()
    }

}