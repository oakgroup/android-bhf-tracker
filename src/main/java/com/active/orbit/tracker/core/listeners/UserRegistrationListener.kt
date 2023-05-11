package com.active.orbit.tracker.core.listeners

import com.active.orbit.tracker.core.deserialization.UserRegistrationMap

interface UserRegistrationListener {

    fun onSuccess(map: UserRegistrationMap)

    fun onError()
}