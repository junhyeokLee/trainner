package com.sports2i.trainer.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupSearchResponse
import com.sports2i.trainer.data.model.GroupSelectedSearchResponse
import com.sports2i.trainer.data.model.GroupUserResponse
import com.sports2i.trainer.repository.GroupRepository
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

    @HiltViewModel
    class GroupViewModel @Inject constructor(
        private val groupRepository: GroupRepository,
        @ApplicationContext private val context: Context
    ) : ViewModel() {

    val groupInfoState: MutableLiveData<NetworkState<GroupSearchResponse>> = MutableLiveData()
    val groupUserState: MutableLiveData<NetworkState<GroupUserResponse>> = MutableLiveData()
    val selectedGroupInfoState: MutableLiveData<NetworkState<GroupSelectedSearchResponse>> = MutableLiveData()

    val selectedGroupInfoLiveData: MutableLiveData<GroupInfo?> = MutableLiveData()

//        var selectedGroupIndex: LiveData<Int> = 0
//        var selectedUserIndex: LiveData<Int> = 0

        private val _selectedGroupIndex = MutableLiveData(0)
        val selectedGroupIndex: LiveData<Int> = _selectedGroupIndex

        private val _selectedUserIndex = MutableLiveData(0)
        val selectedUserIndex: LiveData<Int> = _selectedUserIndex

        fun requestGroupInfo(organizationId:String) = viewModelScope.launch {
            groupInfoState.value = NetworkState.Loading(true)
                try {
                    val response = groupRepository.getGroupSearch(organizationId)
                    if (response.isSuccessful) {
                        response.body()?.let { groupInfo ->
                            groupInfoState.value = NetworkState.Success(groupInfo)
                        } ?: run {
                            groupInfoState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        groupInfoState.value = NetworkState.Error(context.getString(R.string.network_fail))
                    }
                } catch (ex: Exception) {
                    when (ex) {
                        is IOException -> groupInfoState.value =
                            NetworkState.Error(context.getString(R.string.network_error))

                        else -> groupInfoState.value =
                            NetworkState.Error(context.getString(R.string.network_data_change_error))
                    }
                }
            }

        fun requestGroupUser(groupId:String) = viewModelScope.launch {
            groupUserState.value = NetworkState.Loading(true)
            try {
                val response = groupRepository.getGroupUser(groupId)
                if (response.isSuccessful) {
                    response.body()?.let { groupUsers ->
                        groupUserState.value = NetworkState.Success(groupUsers)
                    } ?: run {
                        groupUserState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                    }
                } else {
                    groupUserState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> groupUserState.value = NetworkState.Error(context.getString(R.string.network_error))
                    else -> groupUserState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
                }
            }
        }

        fun getSelectedGroup(groupId:String) = viewModelScope.launch {
            selectedGroupInfoState.value = NetworkState.Loading(true)
            try {
                val response = groupRepository.getSelectedGroup(groupId)
                if (response.isSuccessful) {
                    response.body()?.let { groupInfo ->
                        selectedGroupInfoState.value = NetworkState.Success(groupInfo)
                    } ?: run {
                        selectedGroupInfoState.value =
                            NetworkState.Error(context.getString(R.string.network_empty_date))
                    }
                } else {
                    selectedGroupInfoState.value =
                        NetworkState.Error(context.getString(R.string.network_fail))
                }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> selectedGroupInfoState.value =
                        NetworkState.Error(context.getString(R.string.network_error))

                    else -> selectedGroupInfoState.value =
                        NetworkState.Error(context.getString(R.string.network_data_change_error))
                }
            }
        }



        fun setSelectedGroupInfo(groupInfo: GroupInfo) {
            selectedGroupInfoLiveData.value = groupInfo
        }
        fun getSelectedGroupInfoLiveData(): LiveData<GroupInfo?> {
            return selectedGroupInfoLiveData
        }


    }
