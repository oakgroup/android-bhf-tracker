package com.active.orbit.tracker.core.deserialization

import android.text.TextUtils
import com.active.orbit.tracker.core.generics.BaseModel
import com.active.orbit.tracker.core.utils.Constants
import com.google.gson.annotations.SerializedName

class UserRegistrationMap : BaseModel {

    @SerializedName("_id")
    var id = Constants.EMPTY

    @SerializedName("phone_model")
    var phoneModel = Constants.EMPTY

    @SerializedName("android_version")
    var androidVersion = Constants.EMPTY

    @SerializedName("app_version")
    var appVersion = Constants.EMPTY

    @SerializedName("participantId")
    var participantId = Constants.EMPTY

    @SerializedName("participantIdCounter")
    var participantIdCounter = Constants.INVALID

    override fun identifier(): String {
        return Constants.EMPTY
    }

    override fun isValid(): Boolean {
        return !TextUtils.isEmpty(id) &&
                !TextUtils.isEmpty(phoneModel) &&
                !TextUtils.isEmpty(androidVersion) &&
                !TextUtils.isEmpty(appVersion) &&
                !TextUtils.isEmpty(participantId)
    }
}