/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.observers

import uk.ac.shef.tracker.core.tracker.TrackerManager

/**
 * Utility interface to observer the tracker data
 */
interface TrackerObserver {

    /**
     * This method will be called by the tracker whenever there is an update of the data
     * If the client app is observing, it will be notified
     */
    fun onTrackerUpdate(type: TrackerObserverType, data: Any)

}