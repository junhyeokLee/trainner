// TrackingViewModel.kt

package com.sports2i.trainer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.data.model.TrackingData
import com.sports2i.trainer.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repository: TrackingRepository
) : ViewModel() {

    // LiveData를 사용하여 데이터를 관리
    val allTrackingData: LiveData<List<TrackingData>> = repository.getAllTrackingData()

    // 특정 조건에 맞는 TrackingData를 반환하는 LiveData
    var trackingData: LiveData<TrackingData> = MutableLiveData()

    // 조건에 맞는 TrackingData를 가져오는 함수
    fun getTrackingData(exerciseId: String, userId: String, trainingTime: String, trainingDate: String) {
        viewModelScope.launch {
//            val data = repository.getTrackingData(exerciseId, userId, trainingTime, trainingDate)
            trackingData = repository.getTrackingData(exerciseId, userId, trainingTime, trainingDate)
//            trackingData.postValue(data.value) // 데이터가 null이 아닌 경우에만 postValue를 호출
        }
    }

    fun saveTrackingData(trackingData: TrackingData) {
        viewModelScope.launch {
            repository.insertTracking(trackingData)
        }
    }
}
