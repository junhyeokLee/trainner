package com.sports2i.trainer.data.networks

import com.sports2i.trainer.data.model.Survey
import com.sports2i.trainer.data.model.SurveyInsert
import com.sports2i.trainer.data.model.SurveyInsertResponse
import com.sports2i.trainer.data.model.SurveyItemInsertRequest
import com.sports2i.trainer.data.model.SurveyItemResponse
import com.sports2i.trainer.data.model.SurveyPresetResponse
import com.sports2i.trainer.data.model.SurveySearchResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SurveyApi {
    @POST("/api/surveyPreset/insert")
    suspend fun apiSurveyPresetInsert(@Body surveyList: MutableList<Survey>): Response<ResponseBody>

    @GET("/api/surveyItem/by")
    suspend fun apiSurveyItemBy(@Query("organizationId") organizationId: String): Response<SurveyItemResponse>

    @POST("/api/surveyItem/insert")
    suspend fun apiSurveyItemInsert(@Body surveyItemInsertRequest: SurveyItemInsertRequest): Response<SurveyItemResponse>

    @DELETE("/api/surveyItem/delete/{surveyItemId}")
    suspend fun apiSurveyItemDelete(@Path("surveyItemId") surveyItemId: String): Response<SurveyItemResponse>


    @POST("/api/survey/insert")
    suspend fun apiSurveyInsert(@Body surveyItemInsertRequest: MutableList<SurveyInsert>): Response<SurveyInsertResponse>
    @GET("/api/survey/search")
    suspend fun apiSurveySearch(@Query("userId") userId: String,@Query("date") date: String): Response<SurveySearchResponse>

    @GET("/api/surveyPreset/by")
    suspend fun apiSurveyPresetBy(@Query("userId") userId: String): Response<SurveyPresetResponse>

}