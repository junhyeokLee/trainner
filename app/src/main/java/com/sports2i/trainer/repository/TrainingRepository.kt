package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.DeleteTrainingGroupStatus
import com.sports2i.trainer.data.model.ExerciseItemResponse
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.data.model.ExerciseTimeItemResponse
import com.sports2i.trainer.data.model.ExerciseUnitResponse
import com.sports2i.trainer.data.model.TrainingComment
import com.sports2i.trainer.data.model.TrainingCommentRequest
import com.sports2i.trainer.data.model.TrainingConfirmResponse
import com.sports2i.trainer.data.model.TrainingInfo
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingOverall
import com.sports2i.trainer.data.model.TrainingOverallSearchResponse
import com.sports2i.trainer.data.model.TrainingSub
import com.sports2i.trainer.data.model.TrainingSubDetailHealthRequest
import com.sports2i.trainer.data.model.TrainingSubDetailInsert
import com.sports2i.trainer.data.model.TrainingSubDetailRpeRequest
import com.sports2i.trainer.data.model.TrainingSubDetailUrlRequest
import com.sports2i.trainer.data.model.TrainingSubResponse
import com.sports2i.trainer.data.model.TrainingTssDataTime
import com.sports2i.trainer.data.networks.TrainingApi
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepository @Inject constructor(private val trainingApi: TrainingApi) {

    suspend fun getExerciseItemSearch() : Response<ExerciseItemResponse> {
        return trainingApi.getExerciseSearch()
    }

    suspend fun getExerciseUnitSearch() : Response<ExerciseUnitResponse> {
        return trainingApi.getExerciseUnitSearch()
    }

    suspend fun getExerciseTimeItemSearch() : Response<ExerciseTimeItemResponse> {
        return trainingApi.getExerciseTimeItemSearch()
    }

    suspend fun requestTrainingSave(trainingInfos: MutableList<TrainingInfo>) : Call<TrainingInfoResponse.TrainingInfoResponses> {
        return trainingApi.requestTrainingSave(trainingInfos)
    }

    suspend fun getTrainingStatus(strUserId: String,strTrainingDate: String) : Response<TrainingInfoResponse.TrainingInfoResponses> {
        return trainingApi.getTrainingStatus(strUserId,strTrainingDate)
    }
    suspend fun requestExercisePresetSave(exercisePresets: MutableList<ExercisePreset>) : Call<ExercisePreset.ExercisePresetResponse> {
        return trainingApi.requestExercisePresetSave(exercisePresets)
    }
    suspend fun getExercisePreset(strOrganizationId: String) : Response<ExercisePreset.ExercisePresetResponse> {
        return trainingApi.getExercisePreset(strOrganizationId)
    }
    suspend fun deleteExercisePreset(strExercisePresetId: String) : Call<ExercisePreset.ExercisePresetResponse> {
        return trainingApi.deleteExercisePreset(strExercisePresetId)
    }
    suspend fun getTrainingOverallUser(strUserId: String,strTrainingDate: String) : Response<TrainingOverall.TrainingOverallResponse> {
        return trainingApi.getTrainingOverallUser(strUserId,strTrainingDate)
    }
    suspend fun getTrainingOverallGroup(strOrganizationId: String,strGroupId: String, strTrainingDate:String) : Response<TrainingOverall.TrainingOverallResponse> {
        return trainingApi.getTrainingOverallGroup(strOrganizationId,strGroupId,strTrainingDate)
    }
    suspend fun getNutritionOverallGroup(strOrganizationId: String,strGroupId: String, strTrainingDate:String) : Response<TrainingOverall.TrainingOverallResponse> {
        return trainingApi.getNutritionOverallGroup(strOrganizationId,strGroupId,strTrainingDate)
    }
    suspend fun getTrainingGroupStatus(organizationId: String,groupId: String, type:String, date:String) : Response<TrainingOverallSearchResponse> {
        return trainingApi.getTrainingGroupStatus(organizationId,groupId,type,date)
    }
    suspend fun deleteTrainingStatus(trainingGroupStatus: MutableList<DeleteTrainingGroupStatus>) : Call<TrainingInfoResponse.TrainingInfoResponses> {
        return trainingApi.deleteTrainingStatus(trainingGroupStatus)
    }
     fun requestTrainingComment(trainingCommentRequest: TrainingCommentRequest) : Call<TrainingComment.TrainingCommentResponse> {
        return trainingApi.requestTrainingComment(trainingCommentRequest)
    }

    suspend fun getTrainingComment(userId: String,date: String) : Response<TrainingComment.TrainingCommentResponse> {
        return trainingApi.getTrainingComment(userId,date)
    }

    suspend fun getTrainingSubDetail(userId: String,exerciseId :String,trainingDate: String,trainingTime:String) : Response<TrainingSubResponse.TrainingSubDetailResponse> {
        return trainingApi.getTrainingSubDetail(userId,exerciseId,trainingDate,trainingTime)
    }

    fun deleteTrainingComment(userId: String) : Call<TrainingComment.TrainingCommentResponse> {
        return trainingApi.deleteTrainingComment(userId)
    }

    fun requestTrainingSubInsert(trainingSubDetailInsert:TrainingSubDetailInsert) :  Call<TrainingSubResponse.TrainingSubDetailResponse> {
        return trainingApi.requestTrainingSubInsert(trainingSubDetailInsert)
    }

    suspend fun requestTrainingSubRpeUpdate(trainingSubRpe:TrainingSubDetailRpeRequest) : Response<TrainingSubResponse.TrainingSubDetailResponse> {
        return trainingApi.requestTrainingSubRpeUpdate(trainingSubRpe)
    }
    suspend fun requestTrainingSubHealthUpdate(trainingSubHealth: TrainingSubDetailHealthRequest) : Response<TrainingSubResponse.TrainingSubDetailResponse> {
        return trainingApi.requestTrainingSubHealthUpdate(trainingSubHealth)
    }
    suspend fun requestTrainingSubUrlUpdate(trainingSubUrl: TrainingSubDetailUrlRequest) : Response<TrainingSubResponse.TrainingSubDetailResponse> {
        return trainingApi.requestTrainingSubUrlUpdate(trainingSubUrl)
    }
    suspend fun requestTrainingSubRpeUpdateUrl(trainingSub:TrainingSub) : Call<TrainingSub.TrainingSubObejctResponse> {
        return trainingApi.requestTrainingSubRpeUpdateUrl(trainingSub)
    }

    suspend fun getExerciseDetail(userId: String, date: String, exerciseId:String) : Response<TrainingInfoResponse.TrainingInfoResponses> {
        return trainingApi.getExerciseDetail(userId,date,exerciseId)
    }

    suspend fun requestExerciseUpdate(trainingInfoResponseList:List<TrainingInfoResponse>) : Response<ResponseBody>  {
        return trainingApi.requestExerciseUpdate(trainingInfoResponseList)
    }

    suspend fun getTssDataTime(userId: String, trainingDate: String) : Response<TrainingTssDataTime.TrainingTssDataTimeResponse> {
        return trainingApi.getTssDataTime(userId,trainingDate)
    }

    suspend fun getTrainingConfirm(userId: String, date: String) : Response<TrainingConfirmResponse> {
        return trainingApi.getTrainingConfirm(userId,date)
    }

}