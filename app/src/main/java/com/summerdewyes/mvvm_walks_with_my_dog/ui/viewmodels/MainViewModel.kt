package com.summerdewyes.mvvm_walks_with_my_dog.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summerdewyes.mvvm_walks_with_my_dog.db.Run
import com.summerdewyes.mvvm_walks_with_my_dog.other.SortType
import com.summerdewyes.mvvm_walks_with_my_dog.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * MainRepository 에서 데이터를 수집하고
 * MainViewModel에서 데이터가 필요한 모든 fragment에 데이터를 제공합니다.
 */

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository // 수집한 데이터를 사용하기 위해서는 MainViewModel 내부에 MainRepository 인스턴스가 필요하므로 주생성자에 주입합니다.
) : ViewModel() {

    private val runSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    private val runSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE // 기본

    init {
        runs.addSource(runSortedByDate) { result ->
            if (sortType == SortType.DATE) {
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortedByTimeInMillis) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortedByDistance) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortedByAvgSpeed) { result ->
            if (sortType == SortType.AVG_SPEED) {
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortedByCaloriesBurned) { result ->
            if (sortType == SortType.CALORIES_BURNED) {
                result?.let {
                    runs.value = it
                }
            }
        }

    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> runSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runSortedByTimeInMillis.value?.let { runs.value = it }
        SortType.DISTANCE -> runSortedByDistance.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

    fun deleteRun(run: Run) = viewModelScope.launch {
        mainRepository.deleteRun(run)
    }

}