package uk.ac.shef.tracker.core.listeners

import uk.ac.shef.tracker.core.deserialization.UserRegistrationMap

interface UserRegistrationListener {

    fun onSuccess(map: UserRegistrationMap)

    fun onError()
}