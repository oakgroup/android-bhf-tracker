/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.serialization

import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import uk.ac.shef.tracker.core.generics.TrackerBaseModel
import uk.ac.shef.tracker.core.utils.Constants

class UserRegistrationRequest : TrackerBaseModel {

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