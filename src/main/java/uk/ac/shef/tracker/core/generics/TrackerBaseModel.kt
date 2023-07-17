/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.generics

import android.text.TextUtils
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger

/**
 * Base protocol that should be implemented by all the models
 *
 * @author omar.brugna
 */
interface TrackerBaseModel : Comparable<TrackerBaseModel?> {

    fun isValid(): Boolean

    fun identifier(): String {
        Logger.e("Called base identifier method, this should never happen")
        return Constants.EMPTY
    }

    fun sameOf(other: TrackerBaseModel?): Boolean {
        if (!TextUtils.isEmpty(identifier()) && !TextUtils.isEmpty(other?.identifier()))
            return identifier() == other?.identifier()
        return false
    }

    fun sameContentOf(other: TrackerBaseModel?): Boolean {
        // override to customize
        return false
    }

    override fun compareTo(other: TrackerBaseModel?): Int {
        return Constants.PRIORITY_ZERO
    }

    fun priority(): Long {
        Logger.e("Called base priority method, this should never happen")
        return 0
    }
}