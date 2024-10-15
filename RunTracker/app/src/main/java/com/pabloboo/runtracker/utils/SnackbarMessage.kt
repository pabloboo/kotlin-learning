package com.pabloboo.runtracker.utils

data class SnackbarMessage (
    var message: String,
    var type: String // Constants.SUCCESS_MESSAGE or Constants.ERROR_MESSAGE
)