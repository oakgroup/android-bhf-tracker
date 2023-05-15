package com.active.orbit.tracker.core.network

/**
 * Utility interface to get a connection result
 */
interface TrackerConnectionListener {

    fun onConnectionStarted(tag: Int) {}

    fun onConnectionSuccess(tag: Int, response: String) {}

    fun onConnectionError(tag: Int) {}

    fun onConnectionCompleted(tag: Int) {}
}