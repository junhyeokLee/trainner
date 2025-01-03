package com.sports2i.trainer.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.NutritionDirection
import com.sports2i.trainer.data.model.NutritionDirectionResponse
import com.sports2i.trainer.data.model.NutritionDirectionSearchResponse
import com.sports2i.trainer.data.model.NutritionPictureUser
import com.sports2i.trainer.data.model.NutritionPictureUserResponse
import com.sports2i.trainer.data.model.NutritionResponse
import com.sports2i.trainer.repository.NutritionRepository
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel  @Inject constructor(
    private val nutritionRepository: NutritionRepository, @ApplicationContext private val context: Context
) : ViewModel() {

    val searchNutritionState: MutableLiveData<NetworkState<NutritionResponse>> = MutableLiveData()
    val insertNutritionDirectionState: MutableLiveData<NetworkState<NutritionDirection>> = MutableLiveData()
    var searchNutritionDirectionSearchSatate: MutableLiveData<NetworkState<NutritionDirectionSearchResponse>> = MutableLiveData()
    var nutritionPictureUserListSatate: MutableLiveData<NetworkState<NutritionPictureUserResponse>> = MutableLiveData()
    val insertNutritionPictureState: MutableLiveData<NetworkState<NutritionPictureUserResponse>> = MutableLiveData()
    val searchNutritionPictureState: MutableLiveData<NetworkState<NutritionPictureUserResponse>> = MutableLiveData()
    val deleteNutritionPictureIdState: MutableLiveData<NetworkState<String>> = MutableLiveData()

    fun searchNutrition(groupId:String,type:String,date:String) = viewModelScope.launch {
        searchNutritionState.value = NetworkState.Loading(true)
        try {
            val response = nutritionRepository.searchNutrition(groupId,type,date)
            if (response.isSuccessful) {
                response.body()?.let { notice ->
                    searchNutritionState.value = NetworkState.Success(notice)
                } ?: run {
                    searchNutritionState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                searchNutritionState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        } catch (ex: Exception) {
            when (ex) {
                is IOException -> searchNutritionState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> searchNutritionState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun insertNutritionDirection(nutritionDirectionList: NutritionDirection) = viewModelScope.launch {
        insertNutritionDirectionState.value = NetworkState.Loading(true)

        try{
            val response = nutritionRepository.insertNutritionDirection(nutritionDirectionList)
            response.enqueue(object : Callback<NutritionDirectionResponse> {
                override fun onResponse(call: Call<NutritionDirectionResponse>, response: Response<NutritionDirectionResponse>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            insertNutritionDirectionState.value = NetworkState.Success(nutritionDirectionList)
                        } ?: run {
                            insertNutritionDirectionState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        insertNutritionDirectionState.value = NetworkState.Error(context.getString(R.string.network_fail))
                    }
                }
                override fun onFailure(call: Call<NutritionDirectionResponse>, t: Throwable) {
                    insertNutritionDirectionState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            when(ex){
                is IOException -> insertNutritionDirectionState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> insertNutritionDirectionState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun searchNutritionDirection(userId:String,date:String) = viewModelScope.launch {
        searchNutritionDirectionSearchSatate.value = NetworkState.Loading(true)
        try {
            val response = nutritionRepository.searchNutritionDirection(userId,date)
            Log.e("2312312313",response.toString())
            if (response.isSuccessful) {
                response.body()?.let { nutrition ->
                    searchNutritionDirectionSearchSatate.value = NetworkState.Success(nutrition)
                } ?: run {
                    searchNutritionDirectionSearchSatate.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                searchNutritionDirectionSearchSatate.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        } catch (ex: Exception) {
            when (ex) {
                is IOException -> searchNutritionDirectionSearchSatate.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> searchNutritionDirectionSearchSatate.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun updateNutritionEvaluation(nutritionPictureUserList: MutableList<NutritionPictureUser>) = viewModelScope.launch {
        nutritionPictureUserListSatate.value = NetworkState.Loading(true)
        try {
            val response = nutritionRepository.updateNutritionEvaluation(nutritionPictureUserList)

            if (response.isSuccessful) {
                response.body()?.let { nutritionPictureList ->
                    nutritionPictureUserListSatate.value = NetworkState.Success(nutritionPictureList)
                } ?: run {
                    nutritionPictureUserListSatate.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                nutritionPictureUserListSatate.value = NetworkState.Error(context.getString(R.string.network_fail))
            }

        } catch (ex: Exception) {
            when (ex) {
                is IOException -> nutritionPictureUserListSatate.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> nutritionPictureUserListSatate.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun insertNutritionPicture(nutritionPictureUserList: MutableList<NutritionPictureUser>) = viewModelScope.launch {
        insertNutritionPictureState.value = NetworkState.Loading(true)

        try{
            val response = nutritionRepository.insertNutritionPicture(nutritionPictureUserList)
            response.enqueue(object : Callback<NutritionPictureUserResponse> {
                override fun onResponse(call: Call<NutritionPictureUserResponse>, response: Response<NutritionPictureUserResponse>) {
                    if(response.isSuccessful){
                        response.body()?.let { nutritionPictureUserList ->
                            Log.e("insertNutritionPicture","Success")
                            insertNutritionPictureState.value = NetworkState.Success(nutritionPictureUserList)
                        } ?: run {
                            insertNutritionPictureState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        Log.e("insertNutritionPicture","NetworkState.Error")
                        insertNutritionPictureState.value = NetworkState.Error(context.getString(R.string.network_fail))
                    }
                }
                override fun onFailure(call: Call<NutritionPictureUserResponse>, t: Throwable) {
                    Log.e("insertNutritionPicture","onFailure")
                    insertNutritionPictureState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            when(ex){
                is IOException -> {
                    insertNutritionPictureState.value = NetworkState.Error(context.getString(R.string.network_error))
                    Log.e("insertNutritionPicture","NetworkState.Error")

                }
                else -> {
                    insertNutritionPictureState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
                    Log.e("insertNutritionPicture","NetworkState.Error")
                }
            }
        }
    }

    fun searchNutritionPicture(userId:String,date:String) = viewModelScope.launch {
        searchNutritionPictureState.value = NetworkState.Loading(true)
        try {
            val response = nutritionRepository.searchNutritionPicture(userId,date)
            if (response.isSuccessful) {
                response.body()?.let { notice ->
                    searchNutritionPictureState.value = NetworkState.Success(notice)
                } ?: run {
                    searchNutritionPictureState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                searchNutritionPictureState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        } catch (ex: Exception) {
            when (ex) {
                is IOException -> searchNutritionPictureState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> searchNutritionPictureState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun deleteNutritionPicture(nutritionPictureIdList: MutableList<Int>) = viewModelScope.launch {
        deleteNutritionPictureIdState.value = NetworkState.Loading(true)
        try {
            val response = nutritionRepository.deleteNutritionPicture(nutritionPictureIdList)
            val success = response.isSuccessful
            val message = response.message() // Use the response message

            if (success) {
                deleteNutritionPictureIdState.value = NetworkState.Success(message)
            } else {
                deleteNutritionPictureIdState.value = NetworkState.Error(message)
            }
        } catch (ex: Exception) {
            when (ex) {
                is IOException -> deleteNutritionPictureIdState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> deleteNutritionPictureIdState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }
}