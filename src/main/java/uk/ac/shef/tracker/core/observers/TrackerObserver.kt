package uk.ac.shef.tracker.core.observers

interface TrackerObserver {

    fun onTrackerUpdate(type: TrackerObserverType, data: Any)
}