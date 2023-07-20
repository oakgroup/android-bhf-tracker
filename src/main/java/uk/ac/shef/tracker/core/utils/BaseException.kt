/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

/**
 * Utility custom exception class
 */
@Suppress("unused")
class BaseException : RuntimeException {

    companion object {

        private const val LOG_MESSAGE = "BaseException"
    }

    constructor() : super() {
        Logger.e(LOG_MESSAGE)
    }

    constructor(message: String) : super(message) {
        Logger.e("$LOG_MESSAGE $message")
    }

    constructor(cause: Throwable) : super(cause) {
        Logger.e("$LOG_MESSAGE $cause")
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        Logger.e("$LOG_MESSAGE $message")
    }
}