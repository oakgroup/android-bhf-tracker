package com.active.orbit.tracker.core.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.active.orbit.tracker.core.utils.Constants

/**
 * Utility class used to notify events between activities and fragments
 *
 * @author omar.brugna
 */
class BaseBroadcast(private val mHost: BroadcastHost) : BroadcastReceiver() {

    companion object {

        const val DATA_UPDATED = "data_updated"

        private const val SENDER_ID_KEY = "sender_id"
        private const val IDENTIFIER_KEY = "identifier"
        private const val IDENTIFIER_SUB_KEY = "identifier_sub"

        fun notifyDataUpdated(host: BroadcastHost) {
            notify(host, DATA_UPDATED)
        }

        private fun notify(host: BroadcastHost, broadcastKey: String, identifier: String = Constants.EMPTY, subIdentifier: String = Constants.EMPTY) {
            if (host.getContext() != null) {
                val intent = Intent(broadcastKey)
                intent.putExtra(SENDER_ID_KEY, host.broadcastIdentifier())
                intent.putExtra(IDENTIFIER_KEY, identifier)
                intent.putExtra(IDENTIFIER_SUB_KEY, subIdentifier)
                LocalBroadcastManager.getInstance(host.getContext()!!).sendBroadcast(intent)
            }
        }
    }

    private val mReceiverId: Int
    private var mListener: BroadcastListener? = null

    init {
        mHost.broadcastRegister(this)
        mReceiverId = mHost.broadcastIdentifier()
    }

    fun registerForType(@BroadcastType type: String) {
        if (mHost.getContext() != null)
            LocalBroadcastManager.getInstance(mHost.getContext()!!).registerReceiver(this, IntentFilter(type))
    }

    fun unregister() {
        if (mHost.getContext() != null)
            LocalBroadcastManager.getInstance(mHost.getContext()!!).unregisterReceiver(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        @BroadcastType val type = intent.action!!
        val senderId = intent.getIntExtra(SENDER_ID_KEY, Constants.INVALID)
        val identifier = intent.getStringExtra(IDENTIFIER_KEY) ?: Constants.EMPTY
        val subIdentifier = intent.getStringExtra(IDENTIFIER_SUB_KEY) ?: Constants.EMPTY

        if (mListener != null) {
            val sentByMyself = mReceiverId == senderId
            mListener!!.onBroadcast(context, @BroadcastType type, sentByMyself, identifier, subIdentifier)
        }
    }

    fun setListener(listener: BroadcastListener) {
        mListener = listener
    }
}