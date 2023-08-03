/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.network

/**
 * Utility interface to get a connection result
 */
interface TrackerConnectionListener {

    fun onConnectionStarted(tag: Int) {}

    fun onConnectionSuccess(tag: Int, response: String) {}

    fun onConnectionError(tag: Int) {}

    fun onConnectionCompleted(tag: Int) {}
}