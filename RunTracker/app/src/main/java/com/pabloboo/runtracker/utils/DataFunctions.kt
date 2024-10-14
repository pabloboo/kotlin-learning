package com.pabloboo.runtracker.utils

object DataFunctions {

    fun kmhToMinPerKm(speedKmh: Float): Float {
        if (speedKmh == 0f) return 0f
        return 60 / speedKmh
    }

}