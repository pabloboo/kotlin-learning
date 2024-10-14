package com.pabloboo.runtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.pabloboo.runtracker.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalNumberOfRuns = mainRepository.getTotalNumberOfRuns()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    // To show in the graph
    val runsSortedByDateAscending = mainRepository.getAllRunsSortedByDateAscending()

    // Records
    val get5000MetersRecord = mainRepository.get5000MetersRecord()
    val get10000MetersRecord = mainRepository.get10000MetersRecord()
    val get21000MetersRecord = mainRepository.get21000MetersRecord()
}