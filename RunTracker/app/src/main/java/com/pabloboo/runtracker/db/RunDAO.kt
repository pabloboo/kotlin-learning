package com.pabloboo.runtracker.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RunDAO {

    @Query("SELECT * FROM running_table WHERE id = :id")
    fun getRunById(id: Int): Run?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuns(runs: List<Run>)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("DELETE FROM running_table WHERE id = :id")
    suspend fun deleteRunById(id: Int)

    // Query to get all runs sorted
    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timestamp ASC")
    fun getAllRunsSortedByDateAscending(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    // Query to get total statistics
    @Query("SELECT AVG(avgSpeedInKMH) FROM running_table")
    fun getTotalAvgSpeed(): LiveData<Float>

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT COUNT(*) FROM running_table")
    fun getTotalNumberOfRuns(): LiveData<Int>

    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM running_table")
    fun getTotalDistance(): LiveData<Int>

    // get records
    @Query("SELECT * FROM running_table WHERE distanceInMeters >= 5000 ORDER BY timeInMillis ASC LIMIT 1")
    fun get5000MetersRecord(): LiveData<Run>

    @Query("SELECT * FROM running_table WHERE distanceInMeters >= 10000 ORDER BY timeInMillis ASC LIMIT 1")
    fun get10000MetersRecord(): LiveData<Run>

    @Query("SELECT * FROM running_table WHERE distanceInMeters >= 21000 ORDER BY timeInMillis ASC LIMIT 1")
    fun get21000MetersRecord(): LiveData<Run>

}