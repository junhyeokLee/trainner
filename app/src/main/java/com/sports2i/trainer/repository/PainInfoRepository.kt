package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.PainInfo
import com.sports2i.trainer.data.model.PainInfoResponse
import com.sports2i.trainer.data.model.PainInfoResponse2
import com.sports2i.trainer.data.networks.PainInfoApi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PainInfoRepository @Inject constructor(private val painInfoApi: PainInfoApi) {
    suspend fun painInfoList(userId: String, date: String): Response<PainInfoResponse> {
        return painInfoApi.apiPainInfoList(userId, date)
    }

    suspend fun painInfoInsert(painInfoList: MutableList<PainInfo>): Response<PainInfoResponse> {
        return painInfoApi.apiPainInfoInsert(painInfoList)
    }

    suspend fun painInfoDelete(painInfoList: MutableList<PainInfo>): Response<PainInfoResponse> {
        return painInfoApi.apiPainInfoDelete(painInfoList)
    }

    suspend fun painInfoUpdate(painInfoList: MutableList<PainInfo>): Response<PainInfoResponse> {
        return painInfoApi.apiPainInfoUpdate(painInfoList)
    }

    suspend fun painInfoSearch(userId: String, startDate: String, endDate: String): Response<PainInfoResponse2> {
        return painInfoApi.apiPainInfoSearch(userId, startDate, endDate)
    }
}