/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

import android.os.Handler
import android.os.Looper

/**
 * Utility class to run methods on main or background threads
 *
 * @author omar.brugna
 */
object ThreadHandler {

    private var looper = Handler(Looper.getMainLooper())

    fun mainThread(runnable: Runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            // we are on a background thread, run on main thread
            looper.post(runnable)
        } else runnable.run()
    }

    fun backgroundThread(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // we are on the main thread, start a new thread
            Thread(runnable).start()
        } else runnable.run()
    }
}