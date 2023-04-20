package com.active.orbit.tracker.utils

class Globals {
    companion object {
        const val APP_VERSION_NO: String = "version code"
        const val APP_VERSION_INSTALLATION_DATE: String = "installation date time"
        const val PREFERENCES_FILE: String = "it.torino.preferences"

        const val MSECS_IN_A_MINUTE = (60 * 1000).toLong()
        const val MSECS_IN_AN_HOUR = MSECS_IN_A_MINUTE * 60
        const val MSECS_IN_A_DAY: Long = MSECS_IN_AN_HOUR * 24

        // constants for user preferences
        const val COMPACTING_LOCATIONS_GENERAL: String = "compacting_locations_general"
        const val COMPACTING_LOCATIONS_TRIPS: String = "compacting_locations_trips"
        const val STAY_POINTS: String = "stay_points"

        // constants for app preferences
        const val SEND_DATA_TO_SERVER: String = "send_data_to_server"

        // user constants
        const val PHONE_MODEL: String = "phone_model"
        const val ANDROID_VERSION: String = "android_version"
        const val APP_VERSION: String = "app_version"
        const val USER_ID: String = "_id"
        const val USER_ID_FOR_BATTERIES: String = "user_id"

        // server constants to run either locally on a local server or on the official server
        const val SERVER_REGISTRATION_URL: String = "/user_registration"
        const val SERVER_PORT: String = "" //"":8092"
        const val ACTIVITIES_ON_SERVER: String = "activities"
        const val LOCATIONS_ON_SERVER: String = "locations"
        const val STEPS_ON_SERVER: String = "steps"
        const val TRIPS_ON_SERVER: String = "trips"
        const val HEART_RATES_ON_SERVER: String = "heart_rates"
        const val BATTERIES_ON_SERVER: String = "batteries"

        const val SERVER_ERROR: String = "error"
        const val SERVER_INSERT_ACTIVITIES_URL: String = "/insert_activities"
        const val SERVER_INSERT_LOCATIONS_URL: String = "/insert_locations"
        const val SERVER_INSERT_STEPS_URL: String = "/insert_steps"
        const val SERVER_INSERT_HEART_RATES_URL: String = "/insert_heart_rates"
        const val SERVER_INSERT_TRIPS_URL: String = "/insert_trips"
        const val SERVER_INSERT_BATTERIES_URL: String = "/insert_batteries"

        const val LAST_TRIPS_DAY_SENT: String = "last_tri_sent_date_1"

        // architecture constants
        const val USE_STEP_COUNTER: String = "use_step_counter"
        const val USE_LOCATION_TRACKING: String = "use_location_tracking"
        const val USE_ACTIVITY_RECOGNITION: String = "use_activity_recognition"
        const val USE_HEART_RATE_MONITOR: String = "use_hr_monitor"
        const val USE_BATTERY_MONITOR: String = "use_battery_monitor"
        const val USE_MOBILITY_MODELLING: String = "use_mobility_modelling"

        // others
        const val POWER_PREFERENCES_GRANTED: String = "power_preferences_granted"
        const val POWER_PREFERENCES_2_GRANTED: String = "power_preferences_2_granted_3"
        const val WEAR_ID_SUFFIX: String = "_wear"

    }

}