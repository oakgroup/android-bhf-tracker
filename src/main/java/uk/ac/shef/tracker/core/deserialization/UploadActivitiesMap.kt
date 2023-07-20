/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.deserialization

import com.google.gson.annotations.SerializedName
import uk.ac.shef.tracker.core.generics.TrackerBaseModel
import uk.ac.shef.tracker.core.utils.Constants

/**
 * This class is used to automatically parse a response from the server api
 */
class UploadActivitiesMap : TrackerBaseModel {

    @SerializedName("inserted")
    var inserted: Int? = null

    override fun identifier(): String {
        return Constants.EMPTY
    }

    override fun isValid(): Boolean {
        return inserted != null
    }
}