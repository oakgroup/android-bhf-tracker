package com.active.orbit.tracker.core.broadcast

import android.content.Context

interface TrackerBroadcastHost {

    fun broadcastRegister(broadcast: TrackerBroadcast) {}

    fun broadcastUnregister() {}

    fun broadcastIdentifier(): Int

    fun getContext(): Context?
}