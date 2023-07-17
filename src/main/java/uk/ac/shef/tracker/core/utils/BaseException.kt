package uk.ac.shef.tracker.core.utils

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