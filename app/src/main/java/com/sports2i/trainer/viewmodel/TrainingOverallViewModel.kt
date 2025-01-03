package com.sports2i.trainer.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.SurveyOverallGraphItem
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingOverallGraphItem
import com.sports2i.trainer.data.model.TrainingSubStatisticsGraphItem
import com.sports2i.trainer.repository.TrainingOverallRepository
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingOverallViewModel @Inject constructor(private val trainingOverallRepository: TrainingOverallRepository, @ApplicationContext private val context: Context): ViewModel() {
    val trainingOverallState: MutableLiveData<NetworkState<TrainingOverallGraphItem.TrainingOverallGraphResponse>> = MutableLiveData()
    val trainingOverallStateError: MutableLiveData<Boolean> = MutableLiveData()
    val surveyOverallState: MutableLiveData<NetworkState<SurveyOverallGraphItem.SurveyOverallGraphResponse>> = MutableLiveData()
    val exerciseOverallState: MutableLiveData<NetworkState<TrainingInfoResponse.TrainingInfoResponses>> = MutableLiveData()
    val exerciseOverallStateError: MutableLiveData<Boolean> = MutableLiveData()
    val tssDataState: MutableLiveData<NetworkState<TrainingSubStatisticsGraphItem.TrainingSubStatisticsGraphResponse>> = MutableLiveData()

    fun requestTrainingStatistics(organizationId: String, groupId: String, type: String,date:String) = viewModelScope.launch {
        trainingOverallState.value = NetworkState.Loading(true)
        val response = trainingOverallRepository.requestTrainingStatistics(organizationId, groupId, type, date)
        if (response.isSuccessful) {
            response.body()?.let {
                trainingOverallState.value = NetworkState.Success(it)
                trainingOverallStateError.value = false
            }?: run {
                trainingOverallState.value = NetworkState.Error(context.getString(R.string.network_empty_sucess_date))
                trainingOverallStateError.value = true
            }
        }else {
            trainingOverallState.value = NetworkState.Error(context.getString(R.string.network_error))
            trainingOverallStateError.value = true
        }
    }

    fun requestSurveyStatistics(organizationId: String, groupId: String, type: String,date:String) = viewModelScope.launch {
        surveyOverallState.value = NetworkState.Loading(true)
        val response = trainingOverallRepository.requestSurveyStatistics(organizationId, groupId, type, date)
        if (response.isSuccessful) {
            response.body()?.let {
                surveyOverallState.value = NetworkState.Success(it)
            }?: run {
                surveyOverallState.value = NetworkState.Error(context.getString(R.string.network_empty_sucess_date))
            }
        }else {
            surveyOverallState.value = NetworkState.Error(context.getString(R.string.network_error))
        }
    }

    fun requestExerciseStatistics(userId:String , date:String) = viewModelScope.launch {
        exerciseOverallState.value = NetworkState.Loading(true)
        val response = trainingOverallRepository.requestExerciseStatistics(userId,date)
        if (response.isSuccessful) {
            response.body()?.let {
                exerciseOverallState.value = NetworkState.Success(it)
                exerciseOverallStateError.value = false
            }?: run {
                exerciseOverallState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                exerciseOverallStateError.value = true
            }
        }else {
            exerciseOverallState.value = NetworkState.Error(context.getString(R.string.network_error))
            exerciseOverallStateError.value = true
        }
    }

    fun requestTssData(userId:String , date:String) = viewModelScope.launch {
        tssDataState.value = NetworkState.Loading(true)
        val response = trainingOverallRepository.requestTssData(userId,date)
        if (response.isSuccessful) {
            response.body()?.let {
                tssDataState.value = NetworkState.Success(it)
            }?: run {
                tssDataState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            tssDataState.value = NetworkState.Error(context.getString(R.string.network_error))
        }
    }
}