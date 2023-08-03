/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.observers

/**
 * This enum lists the different types of the tracker observer
 * The tracker will notify the client app with one of these types for each notification
 */
enum class TrackerObserverType {
    ACTIVITIES,
    BATTERIES,
    HEART_RATES,
    LOCATIONS,
    STEPS,
    MOBILITY
}