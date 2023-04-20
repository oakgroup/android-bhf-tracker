package com.active.orbit.tracker.tracker.sensors.activity_recognition

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.active.orbit.tracker.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InsertActivityDataAsync(context: Context, private val activityDataList: List<ActivityData>) {

    companion object {
        val TAG: String? = this::class.simpleName
    }

    private val repositoryInstance: Repository? = Repository.getInstance(context)

    init {
        if (activityDataList.isNotEmpty()) {
            repositoryInstance?.viewModelScope?.launch(Dispatchers.IO) {
                insertActivities(activityDataList)
            }
        }
    }

    private suspend fun insertActivities(activityDataList: List<ActivityData>) {
        withContext(Dispatchers.IO) {
            repositoryInstance?.dBActivityDao?.insertAll(*activityDataList.toTypedArray())
            Result.success(true)
        }
    }
}