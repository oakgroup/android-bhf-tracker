package com.active.orbit.tracker.core.broadcast

import android.content.Context

interface BroadcastListener {

    fun onBroadcast(context: Context, @BroadcastType type: String, sentByMyself: Boolean, identifier: String, subIdentifier: String)
}