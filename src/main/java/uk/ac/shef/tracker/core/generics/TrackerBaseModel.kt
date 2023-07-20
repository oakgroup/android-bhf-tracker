/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.generics

import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger

/**
 * Base interface that should be implemented by all the models
 */
interface TrackerBaseModel : Comparable<TrackerBaseModel?> {

    /**
     * Check the validity of this model according to the required data
     *
     * @return true if the model is valid
     */
    fun isValid(): Boolean

    /**
     * Get the model unique identifier
     * This method should never be called on the base class. Each model should override it and return the model identifier
     *
     * @return the model identifier
     */
    fun identifier(): String {
        Logger.e("Called base identifier method, this should never happen")
        return Constants.EMPTY
    }

    /**
     * Compare this model with another model
     *
     * @param other the input [TrackerBaseModel] to compare
     * @return [Constants.PRIORITY_ZERO] if this object is equal to the specified other object, [Constants.PRIORITY_CURRENT] if it's less than other, or [Constants.PRIORITY_CURRENT] if it's greater than other.
     */
    override fun compareTo(other: TrackerBaseModel?): Int {
        return Constants.PRIORITY_ZERO
    }

    /**
     * Get the priority of this model
     *
     * @return the [Long] priority
     */
    fun priority(): Long {
        Logger.e("Called base priority method, this should never happen")
        return 0
    }
}