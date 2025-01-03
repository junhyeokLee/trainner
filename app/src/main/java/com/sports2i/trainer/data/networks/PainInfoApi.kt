package com.sports2i.trainer.data.networks

import com.sports2i.trainer.data.model.PainInfo
import com.sports2i.trainer.data.model.PainInfoResponse
import com.sports2i.trainer.data.model.PainInfoResponse2
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface PainInfoApi {
    @GET("/api/painInfo/list")
    suspend fun apiPainInfoList(@Query("userId") userId: String, @Query("date") date: String): Response<PainInfoResponse>

    @POST("/api/painInfo/insert")
    suspend fun apiPainInfoInsert(@Body painInfoList: MutableList<PainInfo>): Response<PainInfoResponse>

    @POST("/api/painInfo/delete")
    suspend fun apiPainInfoDelete(@Body painInfoList: MutableList<PainInfo>): Response<PainInfoResponse>

    @PUT("/api/painInfo/update")
    suspend fun apiPainInfoUpdate(@Body painInfoList: MutableList<PainInfo>): Response<PainInfoResponse>

    @GET("/api/painInfo/search")
    suspend fun apiPainInfoSearch(@Query("userId") userId: String, @Query("startDate") startDate: String, @Query("endDate") endDate: String): Response<PainInfoResponse2>
}