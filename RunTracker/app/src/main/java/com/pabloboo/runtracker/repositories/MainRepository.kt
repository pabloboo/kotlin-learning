package com.pabloboo.runtracker.repositories

import com.pabloboo.runtracker.db.Run
import com.pabloboo.runtracker.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDAO: RunDAO
) {

    fun getRunById(id: Int) = runDAO.getRunById(id)

    suspend fun insertRun(run: Run) = runDAO.insertRun(run)

    suspend fun insertRuns(runs: List<Run>) = runDAO.insertRuns(runs)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    suspend fun deleteRunById(id: Int) = runDAO.deleteRunById(id)

    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByDateAscending() = runDAO.getAllRunsSortedByDateAscending()

    fun getAllRunsSortedByDistance() = runDAO.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()

    fun getTotalDistance() = runDAO.getTotalDistance()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()

    fun getTotalNumberOfRuns() = runDAO.getTotalNumberOfRuns()

    fun get5000MetersRecord() = runDAO.get5000MetersRecord()

    fun get10000MetersRecord() = runDAO.get10000MetersRecord()

    fun get21000MetersRecord() = runDAO.get21000MetersRecord()

}