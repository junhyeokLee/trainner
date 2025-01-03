package com.sports2i.trainer.data.networks

import com.sports2i.trainer.data.model.GroupSearchResponse
import com.sports2i.trainer.data.model.GroupSelectedSearchResponse
import com.sports2i.trainer.data.model.GroupUserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupApi {

//    @GET("/api/group/search")
//    suspend fun getGroupSearch(): Response<GroupSearchResponse>
    @GET("/api/group/by")
    suspend fun getGroupSearch(@Query("organizationId") organizationId:String): Response<GroupSearchResponse>

    @GET("/api/account/search")
    suspend fun getGroupUser(@Query("groupId") groupId: String): Response<GroupUserResponse>

    @GET("/api/group/select/{groupId}")
    suspend fun getSelectedGroup(@Path("groupId") groupId: String): Response<GroupSelectedSearchResponse>
}