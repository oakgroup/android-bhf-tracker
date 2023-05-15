package com.active.orbit.tracker.core.broadcast

import android.content.Context

interface TrackerBroadcastListener {

    fun onBroadcast(context: Context, @TrackerBroadcastType type: String, sentByMyself: Boolean, identifier: String, subIdentifier: String)
}