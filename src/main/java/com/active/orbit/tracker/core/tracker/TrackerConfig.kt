package com.active.orbit.tracker.core.tracker

import com.active.orbit.tracker.core.utils.Constants

class TrackerConfig {

    var baseUrl = Constants.EMPTY
    var useStepCounter = true
    var useActivityRecognition = true
    var useLocationTracking = true
    var useHeartRateMonitor = true
    var useMobilityModelling = true
    var useBatteryMonitor = true
    var useStayPoints = true
    var compactLocations = true
    var uploadData = true
}