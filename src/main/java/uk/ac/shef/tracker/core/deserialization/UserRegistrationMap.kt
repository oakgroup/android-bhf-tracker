/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.deserialization

import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import uk.ac.shef.tracker.core.generics.TrackerBaseModel
import uk.ac.shef.tracker.core.utils.Constants

/**
 * This class is used to automatically parse a response from the server api
 */
class UserRegistrationMap : TrackerBaseModel {

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