package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.SurveyOverallGraphItem
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingOverallGraphItem
import com.sports2i.trainer.data.model.TrainingSubStatisticsGraphItem
import com.sports2i.trainer.data.networks.TrainingOverallApi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingOverallRepository @Inject constructor(private val trainingOverallApi: TrainingOverallApi) {
    suspend fun requestTrainingStatistics(organizationId: String, groupId: String, type: String,date:String): Response<TrainingOverallGraphItem.TrainingOverallGraphResponse> {
        return trainingOverallApi.apiTrainingOverall(organizationId, groupId, type,date)
    }

    suspend fun requestSurveyStatistics(organizationId: String, groupId: String, type: String,date:String): Response<SurveyOverallGraphItem.SurveyOverallGraphResponse> {
        return trainingOverallApi.apiSurveyOverall(organizationId, groupId, type,date)
    }

    suspend fun requestExerciseStatistics(userId:String , date:String): Response<TrainingInfoResponse.TrainingInfoResponses> {
        return trainingOverallApi.apiExerciseOverall(userId,date)
    }

    suspend fun requestTssData(userId:String , date:String): Response<TrainingSubStatisticsGraphItem.TrainingSubStatisticsGraphResponse> {
        return trainingOverallApi.apiTssData(userId,date)
    }
}