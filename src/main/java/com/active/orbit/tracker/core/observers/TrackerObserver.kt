package com.active.orbit.tracker.core.observers

interface TrackerObserver {

    fun onTrackerUpdate(type: TrackerObserverType, data: Any)
}