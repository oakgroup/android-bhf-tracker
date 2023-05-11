package com.active.orbit.tracker.core.database.engine

import com.active.orbit.tracker.core.generics.BaseModel
import com.active.orbit.tracker.core.utils.BaseException
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.Logger

/**
 * Base database model that should be extended from other database models
 */
open class DBModel : BaseModel {

    override fun isValid(): Boolean {
        throw BaseException("Is valid method must never be called on the base class")
    }

    override fun identifier(): String {
        Logger.e("Called base identifier method, this should never happen")
        return Constants.EMPTY
    }
}