package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.GroupSearchResponse
import com.sports2i.trainer.data.model.GroupSelectedSearchResponse
import com.sports2i.trainer.data.model.GroupUserResponse
import com.sports2i.trainer.data.networks.GroupApi
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
class GroupRepository @Inject constructor(private val groupApi: GroupApi) {

    suspend fun getGroupSearch(organizationId:String) : Response<GroupSearchResponse> {
        return groupApi.getGroupSearch(organizationId)
    }
    suspend fun getGroupUser(groupId:String) : Response<GroupUserResponse> {
        return groupApi.getGroupUser(groupId)
    }
    suspend fun getSelectedGroup(groupId:String) : Response<GroupSelectedSearchResponse> {
        return groupApi.getSelectedGroup(groupId)
    }
}