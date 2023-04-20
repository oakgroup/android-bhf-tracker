package com.active.orbit.tracker.tracker.sensors.batteries

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.active.orbit.tracker.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InsertBatteryDataAsync(context: Context, batteryList: List<BatteryData>) {

    private val repositoryInstance: Repository? = Repository.getInstance(context)

    companion object {
        val TAG: String? = this::class.simpleName
    }

    init {
        if (batteryList.isNotEmpty()) {
            repositoryInstance?.viewModelScope?.launch(Dispatchers.IO) {
                insertBatteryList(batteryList)
            }
        }
    }

    private suspend fun insertBatteryList(batteryList: List<BatteryData?>) {
        withContext(Dispatchers.IO) {
            repositoryInstance?.dBBatteryDao?.insertAll(*batteryList.toTypedArray())
            Result.success(true)
        }
    }
}