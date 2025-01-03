package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.Survey
import com.sports2i.trainer.data.model.SurveyInsert
import com.sports2i.trainer.data.model.SurveyInsertResponse
import com.sports2i.trainer.data.model.SurveyItemInsertRequest
import com.sports2i.trainer.data.model.SurveyItemResponse
import com.sports2i.trainer.data.model.SurveyPresetResponse
import com.sports2i.trainer.data.model.SurveySearchResponse
import com.sports2i.trainer.data.networks.SurveyApi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveyRepository @Inject constructor(private val surveyApi: SurveyApi) {
    suspend fun surveyPresetInsert(surveyList: MutableList<Survey>): Response<ResponseBody> {
        return surveyApi.apiSurveyPresetInsert(surveyList)
    }
    suspend fun surveyItemBy(organizationId: String): Response<SurveyItemResponse> {
        return surveyApi.apiSurveyItemBy(organizationId)
    }

    suspend fun surveyItemInsert(surveyItemInsertRequest: SurveyItemInsertRequest): Response<SurveyItemResponse> {
        return surveyApi.apiSurveyItemInsert(surveyItemInsertRequest)
    }

    suspend fun surveyItemDelete(surveyItemId: String): Response<SurveyItemResponse> {
        return surveyApi.apiSurveyItemDelete(surveyItemId)
    }

    suspend fun surveyInsert(surveyInsertRequest: MutableList<SurveyInsert>): Response<SurveyInsertResponse> {
        return surveyApi.apiSurveyInsert(surveyInsertRequest)
    }

    suspend fun surveySearch(userId: String, date: String): Response<SurveySearchResponse> {
        return surveyApi.apiSurveySearch(userId, date)
    }

    suspend fun surveyPresetBy(userId: String): Response<SurveyPresetResponse> {
        return surveyApi.apiSurveyPresetBy(userId)
    }
}