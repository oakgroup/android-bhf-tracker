package com.active.orbit.tracker

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.active.orbit.tracker.database.MyRoomDatabase
import com.active.orbit.tracker.database.daos.*
import com.active.orbit.tracker.retrieval.MobilityResultComputation
import com.active.orbit.tracker.retrieval.data.TripData
import com.active.orbit.tracker.tracker.TrackerService

class Repository(application: Context?) : ViewModel() {

    val dBStepsDao: StepsDataDAO?
    val dBActivityDao: ActivityDataDAO?
    val dBLocationDao: LocationDataDAO?
    val dBHeartRateDao: HeartRateDAO?
    val dBBatteryDao: BatteryDAO?
    val currentHeartRate: MutableLiveData<Int>
    val mobilityChart: MutableLiveData<MobilityResultComputation>?
    val tripsList: MutableLiveData<List<TripData>>?

    companion object {

        private var repositoryInstance: Repository? = null

        @Synchronized
        fun createInstance(application: Context?): Repository? {
            if (repositoryInstance == null) {
                repositoryInstance = Repository(application)
            }
            return repositoryInstance
        }

        val instance: Repository?
            get() {
                if (TrackerService.currentTracker != null) {
                    if (repositoryInstance == null) createInstance(TrackerService.currentTracker)
                }
                return repositoryInstance
            }

        fun getInstance(context: Context?): Repository? {
            if (repositoryInstance == null) createInstance(context)
            return repositoryInstance
        }
    }

    init {
        val db = MyRoomDatabase.getDatabase(application!!)
        repositoryInstance = this
        dBStepsDao = db!!.myStepDataDao()
        dBActivityDao = db.myActivityDataDao()
        dBLocationDao = db.myLocationDataDao()
        dBHeartRateDao = db.myHeartRateDataDao()
        dBBatteryDao = db.myBatteryDAO()

        currentHeartRate = MutableLiveData<Int>()

        mobilityChart = MutableLiveData()
        tripsList = MutableLiveData()
    }
}