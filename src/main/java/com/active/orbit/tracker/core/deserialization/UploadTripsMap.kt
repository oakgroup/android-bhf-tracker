package com.active.orbit.tracker.core.deserialization

import com.active.orbit.tracker.core.generics.BaseModel
import com.active.orbit.tracker.core.utils.Constants
import com.google.gson.annotations.SerializedName

class UploadTripsMap : BaseModel {

    @SerializedName("inserted")
    var inserted: Int? = null

    override fun identifier(): String {
        return Constants.EMPTY
    }

    override fun isValid(): Boolean {
        return inserted != null
    }
}