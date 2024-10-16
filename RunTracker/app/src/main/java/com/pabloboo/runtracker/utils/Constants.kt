package com.pabloboo.runtracker.utils

object Constants {

    const val RUNNING_DATABASE_NAME = "running_db"
    const val REQUEST_CODE_LOCATION_PERMISSION = 0
    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_SHOW_TRACKING_SCREEN = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 18f
    const val TIMER_UPDATE_INTERVAL = 50L
    const val SUCCESS_MESSAGE = "Success"
    const val ERROR_MESSAGE = "Error"

    const val SHARED_PREF_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
}