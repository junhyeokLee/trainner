package com.sports2i.trainer.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.PainInfo
import com.sports2i.trainer.data.model.PainInfoResponse
import com.sports2i.trainer.data.model.PainInfoResponse2
import com.sports2i.trainer.repository.PainInfoRepository
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PainInfoViewModel @Inject constructor(private val painInfoRepository: PainInfoRepository, @ApplicationContext private val context: Context): ViewModel() {
    val painInfoListState: MutableLiveData<NetworkState<PainInfoResponse>> = MutableLiveData()
    val painInfoInsertState: MutableLiveData<NetworkState<PainInfoResponse>> = MutableLiveData()
    val painInfoDeleteState: MutableLiveData<NetworkState<PainInfoResponse>> = MutableLiveData()
    val painInfoUpdateState: MutableLiveData<NetworkState<PainInfoResponse>> = MutableLiveData()
    val painInfoSearcheState: MutableLiveData<NetworkState<PainInfoResponse2>> = MutableLiveData()

    fun requestPainInfoList(userId: String, date: String) = viewModelScope.launch {
        painInfoListState.value = NetworkState.Loading(true)
        val response = painInfoRepository.painInfoList(userId, date)
        if (response.isSuccessful) {
            response.body()?.let {
                painInfoListState.value = NetworkState.Success(it)
            }?: run {
                painInfoListState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            painInfoListState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestPainInfoInsert(painInfoList: MutableList<PainInfo>) = viewModelScope.launch {
        painInfoInsertState.value = NetworkState.Loading(true)
        val response = painInfoRepository.painInfoInsert(painInfoList)
        if (response.isSuccessful) {
            response.body()?.let {
                painInfoInsertState.value = NetworkState.Success(it)
            }?: run {
                painInfoInsertState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            painInfoInsertState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestPainInfoDelete(painInfoList: MutableList<PainInfo>) = viewModelScope.launch {
        painInfoDeleteState.value = NetworkState.Loading(true)
        val response = painInfoRepository.painInfoDelete(painInfoList)
        if (response.isSuccessful) {
            response.body()?.let {
                painInfoDeleteState.value = NetworkState.Success(it)
            }?: run {
                painInfoDeleteState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            painInfoDeleteState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestPainInfoUpdate(painInfoList: MutableList<PainInfo>) = viewModelScope.launch {
        painInfoUpdateState.value = NetworkState.Loading(true)
        val response = painInfoRepository.painInfoUpdate(painInfoList)
        if (response.isSuccessful) {
            response.body()?.let {
                painInfoUpdateState.value = NetworkState.Success(it)
            }?: run {
                painInfoUpdateState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            painInfoUpdateState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun requestPainInfoSearch(userId: String, startDate: String, endDate: String) = viewModelScope.launch {
        painInfoSearcheState.value = NetworkState.Loading(true)
        val response = painInfoRepository.painInfoSearch(userId, startDate, endDate)
        if (response.isSuccessful) {
            response.body()?.let {
                painInfoSearcheState.value = NetworkState.Success(it)
            }?: run {
                painInfoSearcheState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            painInfoSearcheState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }
}