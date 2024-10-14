package com.pabloboo.runtracker.utils

import com.pabloboo.runtracker.db.Run
import java.util.Calendar

object DataFunctions {

    fun kmhToMinPerKm(speedKmh: Float): Float {
        if (speedKmh == 0f) return 0f
        return 60 / speedKmh
    }

    fun getTotalKmOfCurrentWeek(runs: List<Run>): Float {
        var totalKm = 0f
        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        runs.forEach {
            val runWeek = Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }.get(Calendar.WEEK_OF_YEAR)
            if (runWeek == currentWeek) {
                totalKm += it.distanceInMeters / 1000f
            }
        }
        return totalKm
    }

    fun getTotalKmOfPastWeek(runs: List<Run>): Float {
        var totalKm = 0f
        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        runs.forEach {
            val runWeek = Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }.get(Calendar.WEEK_OF_YEAR)
            if (runWeek == currentWeek - 1) {
                totalKm += it.distanceInMeters / 1000f
            }
        }
        return totalKm
    }

}