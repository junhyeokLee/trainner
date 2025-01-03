package com.sports2i.trainer.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Survey
import com.sports2i.trainer.data.model.SurveyInsert
import com.sports2i.trainer.data.model.SurveyInsertResponse
import com.sports2i.trainer.data.model.SurveyItemInsertRequest
import com.sports2i.trainer.data.model.SurveyItemResponse
import com.sports2i.trainer.data.model.SurveyPresetResponse
import com.sports2i.trainer.data.model.SurveySearchResponse
import com.sports2i.trainer.repository.SurveyRepository
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyViewModel @Inject constructor(private val surveyRepository: SurveyRepository, @ApplicationContext private val context: Context): ViewModel() {
    val surveyItemState: MutableLiveData<NetworkState<SurveyItemResponse>> = MutableLiveData()
    val surveyPresetState: MutableLiveData<NetworkState<Int>> = MutableLiveData()
    val surveyInsertState: MutableLiveData<NetworkState<SurveyInsertResponse>> = MutableLiveData()
    val surveySearchState: MutableLiveData<NetworkState<SurveySearchResponse>> = MutableLiveData()

    val surveyPresetByState: MutableLiveData<NetworkState<SurveyPresetResponse>> = MutableLiveData()

    fun requestSurveyPresetInsert(surveyList: MutableList<Survey>) = viewModelScope.launch {
        surveyPresetState.value = NetworkState.Loading(true)
        val response = surveyRepository.surveyPresetInsert(surveyList)
        if (response.isSuccessful) {
            response.body()?.let {
                surveyPresetState.value = NetworkState.Success(response.code())
            }?: run {
                surveyPresetState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            surveyPresetState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestSurveyItemBy(organizationId: String) = viewModelScope.launch {
        surveyItemState.value = NetworkState.Loading(true)
        val response = surveyRepository.surveyItemBy(organizationId)
        if (response.isSuccessful) {
            response.body()?.let {
                surveyItemState.value = NetworkState.Success(it)
            }?: run {
                surveyItemState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            surveyItemState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestSurveyItemInsert(organizationId: String, surveyItemName: String) = viewModelScope.launch {
        surveyItemState.value = NetworkState.Loading(true)
        val response = surveyRepository.surveyItemInsert(SurveyItemInsertRequest(organizationId = organizationId, surveyItemName = surveyItemName))
        if (response.isSuccessful) {
            response.body()?.let {
                surveyItemState.value = NetworkState.Success(it)
            }?: run {
                surveyItemState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            surveyItemState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestSurveyItemDelete(surveyItemId: String) = viewModelScope.launch {
        surveyItemState.value = NetworkState.Loading(true)
        val response = surveyRepository.surveyItemDelete(surveyItemId)
        if (response.isSuccessful) {
            response.body()?.let {
                surveyItemState.value = NetworkState.Success(it)
            }?: run {
                surveyItemState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            surveyItemState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestSurveyInsert(surveyInsert: MutableList<SurveyInsert>) = viewModelScope.launch {
        surveyInsertState.value = NetworkState.Loading(true)
        val response = surveyRepository.surveyInsert(surveyInsert)
        if (response.isSuccessful) {
            response.body()?.let {
                surveyInsertState.value = NetworkState.Success(it)
            }?: run {
                surveyInsertState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            surveyInsertState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestSurveySearch(userId: String,date:String) = viewModelScope.launch {
        surveySearchState.value = NetworkState.Loading(true)
        val response = surveyRepository.surveySearch(userId,date)
        if (response.isSuccessful) {
            response.body()?.let {
                surveySearchState.value = NetworkState.Success(it)
            }?: run {
                surveySearchState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            surveySearchState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestSurveyPresetBy(userId: String) = viewModelScope.launch {
        surveyPresetByState.value = NetworkState.Loading(true)
        val response = surveyRepository.surveyPresetBy(userId)
        if (response.isSuccessful) {
            response.body()?.let {
                surveyPresetByState.value = NetworkState.Success(it)
            }?: run {
                surveyPresetByState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            surveyPresetByState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }
}