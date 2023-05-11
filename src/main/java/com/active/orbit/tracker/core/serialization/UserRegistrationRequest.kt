package com.active.orbit.tracker.core.serialization

import android.text.TextUtils
import com.active.orbit.tracker.core.generics.BaseModel
import com.active.orbit.tracker.core.utils.Constants
import com.google.gson.annotations.SerializedName

class UserRegistrationRequest : BaseModel {

    @SerializedName("phone_model")
    var phoneModel: String? = null

    @SerializedName("app_version")
    var appVersion: String? = null

    @SerializedName("android_version")
    var androidVersion: String? = null

    @SerializedName("id_program")
    var idProgram: String? = null

    @SerializedName("participantId")
    var idPatient: String? = null

    @SerializedName("userSex")
    var userSex: String? = null

    @SerializedName("userAge")
    var userAge: String? = null

    @SerializedName("userWeight")
    var userWeight: String? = null

    @SerializedName("userHeight")
    var userHeight: String? = null

    @SerializedName("batteryPercent")
    var batteryLevel: Int? = null

    @SerializedName("isCharging")
    var isCharging = false

    @SerializedName("timeInMsecs")
    var registrationTimestamp: Long? = null

    override fun identifier(): String {
        return Constants.EMPTY
    }

    override fun isValid(): Boolean {
        return !TextUtils.isEmpty(phoneModel) &&
                !TextUtils.isEmpty(appVersion) &&
                !TextUtils.isEmpty(androidVersion) &&
                !TextUtils.isEmpty(idProgram) &&
                !TextUtils.isEmpty(idPatient) &&
                !TextUtils.isEmpty(userSex) &&
                !TextUtils.isEmpty(userAge) &&
                !TextUtils.isEmpty(userWeight) &&
                !TextUtils.isEmpty(userHeight) &&
                batteryLevel != null &&
                registrationTimestamp != null
    }
}