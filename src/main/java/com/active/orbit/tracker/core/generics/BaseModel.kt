package com.active.orbit.tracker.core.generics

import android.text.TextUtils
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.Logger

/**
 * Base protocol that should be implemented by all the models
 *
 * @author omar.brugna
 */
interface BaseModel : Comparable<BaseModel?> {

    fun isValid(): Boolean

    fun identifier(): String {
        Logger.e("Called base identifier method, this should never happen")
        return Constants.EMPTY
    }

    fun sameOf(other: BaseModel?): Boolean {
        if (!TextUtils.isEmpty(identifier()) && !TextUtils.isEmpty(other?.identifier()))
            return identifier() == other?.identifier()
        return false
    }

    fun sameContentOf(other: BaseModel?): Boolean {
        // override to customize
        return false
    }

    override fun compareTo(other: BaseModel?): Int {
        return Constants.PRIORITY_ZERO
    }

    fun priority(): Long {
        Logger.e("Called base priority method, this should never happen")
        return 0
    }
}