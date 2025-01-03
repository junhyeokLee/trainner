package com.sports2i.trainer.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.NoticeInertResponse
import com.sports2i.trainer.data.model.NoticeInsert
import com.sports2i.trainer.data.model.NoticeListResponse
import com.sports2i.trainer.data.model.NoticeResponse
import com.sports2i.trainer.repository.NoticeRepository
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel  @Inject constructor(
    private val noticeRepository: NoticeRepository, @ApplicationContext private val context: Context
) : ViewModel() {

    val noticeState: MutableLiveData<NetworkState<NoticeResponse>> = MutableLiveData()
    val noticeListState: MutableLiveData<NetworkState<NoticeListResponse>> = MutableLiveData()
    val noticeInsertState : MutableLiveData<NetworkState<NoticeInertResponse>> = MutableLiveData()
    val noticeDeleteState : MutableLiveData<NetworkState<ResponseBody>> = MutableLiveData()
    val noticeUpdateState : MutableLiveData<NetworkState<NoticeInertResponse>> = MutableLiveData()

    fun getNotice(page:Int) = viewModelScope.launch {
        noticeState.value = NetworkState.Loading(true)
        try {
            val response = noticeRepository.getNotice(page)
            if (response.isSuccessful) {
                response.body()?.let { notice ->
                    noticeState.value = NetworkState.Success(notice)
                } ?: run {
                    noticeState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                noticeState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        } catch (ex: Exception) {
            when (ex) {
                is IOException -> noticeState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> noticeState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun getNoticeList(lastId:Int) = viewModelScope.launch {
        noticeListState.value = NetworkState.Loading(true)
        try {
            val response = noticeRepository.getNoticeList(lastId)
            if (response.isSuccessful) {
                response.body()?.let { notice ->
                    noticeListState.value = NetworkState.Success(notice)
                } ?: run {
                    noticeListState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                noticeListState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        } catch (ex: Exception) {
            when (ex) {
                is IOException -> noticeListState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> noticeListState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun requestNoticeEnroll(noticeInsert: NoticeInsert) = viewModelScope.launch {
        noticeInsertState.value = NetworkState.Loading(true)
        try{
            val response = noticeRepository.requestNoticeEnroll(noticeInsert)
            response.enqueue(object : Callback<NoticeInertResponse> {
                override fun onResponse(call: Call<NoticeInertResponse>, response: Response<NoticeInertResponse>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            noticeInsertState.value = NetworkState.Success(data)
                        } ?: run {
                            noticeInsertState.value = NetworkState.Error(context.getString(R.string.network_empty_date))

                        }
                    } else {
                        noticeInsertState.value = NetworkState.Error(context.getString(R.string.network_fail))

                    }
                }
                override fun onFailure(call: Call<NoticeInertResponse>, t: Throwable) {
                    noticeInsertState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            when(ex){
                is IOException -> noticeInsertState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> noticeInsertState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))

            }
        }
    }

    fun deleteNotice(id: Int) = viewModelScope.launch {
        noticeDeleteState.value = NetworkState.Loading(true)
        try{
            val response = noticeRepository.deleteNotice(id)
            response.enqueue(object : Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            noticeDeleteState.value = NetworkState.Success(data)
                        } ?: run {
                            noticeDeleteState.value = NetworkState.Error(context.getString(R.string.network_empty_date))

                        }
                    } else {
                        noticeDeleteState.value = NetworkState.Error(context.getString(R.string.network_fail))
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    noticeDeleteState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            when(ex){
                is IOException -> noticeDeleteState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> noticeDeleteState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))

            }
        }
    }


    fun getNoticeDetail(id:Int) = viewModelScope.launch {
        noticeInsertState.value = NetworkState.Loading(true)
        try {
            val response = noticeRepository.getNoticeDetail(id)
            if (response.isSuccessful) {
                response.body()?.let { notice ->
                    noticeInsertState.value = NetworkState.Success(notice)
                } ?: run {
                    noticeInsertState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                noticeInsertState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        } catch (ex: Exception) {
            when (ex) {
                is IOException -> noticeInsertState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> noticeInsertState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun updateNotice(id: Int, noticeInsert: NoticeInsert) = viewModelScope.launch {
        noticeUpdateState.value = NetworkState.Loading(true)
        try {
            val response = noticeRepository.updateNotice(id, noticeInsert)

            if (response.isSuccessful) {
                response.body()?.let { notice ->
                    noticeUpdateState.value = NetworkState.Success(notice)
                } ?: run {
                    noticeUpdateState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                noticeUpdateState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }

        } catch (ex: Exception) {
            when (ex) {
                is IOException -> noticeUpdateState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> noticeUpdateState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

}