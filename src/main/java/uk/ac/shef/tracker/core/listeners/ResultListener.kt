/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.listeners

/**
 * Utility interface to have a boolean result callback
 */
interface ResultListener {

    fun onResult(success: Boolean)
}