package com.sports2i.trainer.data.networks

import com.sports2i.trainer.data.model.NoticeInertResponse
import com.sports2i.trainer.data.model.NoticeInsert
import com.sports2i.trainer.data.model.NoticeListResponse
import com.sports2i.trainer.data.model.NoticeResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NoticeApi {

    @GET("/api/notice/search")
    suspend fun getNotice(@Query("page") page: Int): Response<NoticeResponse>

    // 무한스크롤
    @GET("/api/notice/list")
    suspend fun getNoticeList(@Query("lastId") lastId: Int): Response<NoticeListResponse>


    @POST("/api/notice/insert")
    suspend fun requestNoticeEnroll(@Body noticeInsert: NoticeInsert) : Call<NoticeInertResponse>


    @GET("/api/notice/{id}")
    suspend fun getNoticeDetail(@Path("id") id: Int): Response<NoticeInertResponse>

    @DELETE("/api/notice/delete/{id}")
    fun deleteNotice(@Path("id") id: Int): Call<ResponseBody>

    @PUT("/api/notice/update/{id}")
    suspend fun updateNotice(@Path("id") id: Int, @Body noticeInsert: NoticeInsert): Response<NoticeInertResponse>


}