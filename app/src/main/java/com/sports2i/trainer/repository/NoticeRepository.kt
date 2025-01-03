package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.NoticeInertResponse
import com.sports2i.trainer.data.model.NoticeInsert
import com.sports2i.trainer.data.model.NoticeListResponse
import com.sports2i.trainer.data.model.NoticeResponse
import com.sports2i.trainer.data.networks.NoticeApi
import okhttp3.ResponseBody
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
class NoticeRepository @Inject constructor(private val noticeApi: NoticeApi) {

    suspend fun getNotice(page:Int) : Response<NoticeResponse> {
        return noticeApi.getNotice(page)
    }

    suspend fun getNoticeList(lastId:Int) : Response<NoticeListResponse> {
        return noticeApi.getNoticeList(lastId)
    }

    suspend fun requestNoticeEnroll(noticeInsert: NoticeInsert) : Call<NoticeInertResponse> {
        return noticeApi.requestNoticeEnroll(noticeInsert)
    }

    suspend fun getNoticeDetail(id: Int): Response<NoticeInertResponse> {
        return noticeApi.getNoticeDetail(id)
    }

    fun deleteNotice(id: Int): Call<ResponseBody> {
        return noticeApi.deleteNotice(id)
    }

    suspend fun updateNotice(id: Int, noticeInsert: NoticeInsert): Response<NoticeInertResponse> {
        return noticeApi.updateNotice(id, noticeInsert)
    }

}