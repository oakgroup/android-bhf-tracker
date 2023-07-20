/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.tracker

import uk.ac.shef.tracker.core.utils.Constants

/**
 * This model will be used by the client app to customise the tracker configurations
 */
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