package com.summerdewyes.mvvm_walks_with_my_dog.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByAvgDistance(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillis) From running_table")
    fun getTotalTimeInMillis() : LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) From running_table")
    fun getTotalCaloriesBurned() : LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) From running_table")
    fun getTotalDistanceInMeters() : LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKMH) From running_table")
    fun getTotalAvgSpeed() : LiveData<Float>
}