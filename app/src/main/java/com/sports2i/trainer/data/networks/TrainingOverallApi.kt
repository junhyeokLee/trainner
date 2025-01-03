package com.sports2i.trainer.data.networks

import com.sports2i.trainer.data.model.SurveyOverallGraphItem
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingOverallGraphItem
import com.sports2i.trainer.data.model.TrainingSubStatisticsGraphItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TrainingOverallApi {

    @GET("/api/mobile/statistics")
    suspend fun apiTrainingOverall(@Query("organizationId") organizationId: String, @Query("groupId") groupId: String, @Query("type") type: String,@Query("date") date:String): Response<TrainingOverallGraphItem.TrainingOverallGraphResponse>

    @GET("/api/mobile/statistics")
    suspend fun apiSurveyOverall(@Query("organizationId") organizationId: String, @Query("groupId") groupId: String, @Query("type") type: String,@Query("date") date:String): Response<SurveyOverallGraphItem.SurveyOverallGraphResponse>

    @GET("/api/mobile/exercise")
    suspend fun apiExerciseOverall(@Query("userId") userId:String , @Query("date") date:String): Response<TrainingInfoResponse.TrainingInfoResponses>

    @GET("/api/TrainingSubDetail/getTssData")
    suspend fun apiTssData(@Query("userId") userId:String , @Query("trainingDate") date:String): Response<TrainingSubStatisticsGraphItem.TrainingSubStatisticsGraphResponse>

}