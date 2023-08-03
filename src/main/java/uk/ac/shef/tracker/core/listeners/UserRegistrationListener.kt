/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.listeners

import uk.ac.shef.tracker.core.deserialization.UserRegistrationMap

/**
 * Utility interface to have a the callback of the user registration api request
 */
interface UserRegistrationListener {

    fun onSuccess(map: UserRegistrationMap)

    fun onError()
}