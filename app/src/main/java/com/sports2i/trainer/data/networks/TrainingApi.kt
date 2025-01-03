package com.sports2i.trainer.data.networks

import com.sports2i.trainer.data.model.DeleteTrainingGroupStatus
import com.sports2i.trainer.data.model.ExerciseItemResponse
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.data.model.ExerciseTimeItemResponse
import com.sports2i.trainer.data.model.ExerciseUnitResponse
import com.sports2i.trainer.data.model.TrainingComment
import com.sports2i.trainer.data.model.TrainingCommentRequest
import com.sports2i.trainer.data.model.TrainingConfirmResponse
import com.sports2i.trainer.data.model.TrainingExercise
import com.sports2i.trainer.data.model.TrainingGroupStatus
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

interface TrainingApi {
    @GET("/api/exerciseitem/searchall")
    suspend fun getExerciseSearch(): Response<ExerciseItemResponse>

    @GET("/api/exerciseunit/searchall")
    suspend fun getExerciseUnitSearch(): Response<ExerciseUnitResponse>

    @GET("/api/timeitem/searchall")
    suspend fun getExerciseTimeItemSearch(): Response<ExerciseTimeItemResponse>


    @POST("/api/training/insert")
    suspend fun requestTrainingSave(@Body trainingInfos: MutableList<TrainingInfo>) : Call<TrainingInfoResponse.TrainingInfoResponses>

    @GET("/api/training/searchlist")
    suspend fun getTrainingStatus(@Query("strUserId") strUserId: String,@Query("strTrainingDate") strTrainingDate: String) : Response<TrainingInfoResponse.TrainingInfoResponses>

    @POST("/api/exercisepreset/save")
    suspend fun requestExercisePresetSave(@Body exercisePresets: MutableList<ExercisePreset>) : Call<ExercisePreset.ExercisePresetResponse>

    @GET("/api/exercisepreset/search")
    suspend fun getExercisePreset(@Query("strOrganizationId") strOrganizationId: String) : Response<ExercisePreset.ExercisePresetResponse>

    @POST("/api/exercisepreset/delete")
    suspend fun deleteExercisePreset(@Query("strExercisePresetId") strExercisePresetId: String) : Call<ExercisePreset.ExercisePresetResponse>

    @GET("/api/trainingoverall/search")
    suspend fun getTrainingOverallUser(@Query("strUserId") strUserId: String,@Query("strTrainingDate") strTrainingDate: String) : Response<TrainingOverall.TrainingOverallResponse>

    @GET("/api/trainingoverall/groupsearch")
    suspend fun getTrainingOverallGroup(@Query("strOrganizationId") strOrganizationId: String,@Query("strGroupId") strGroupId: String, @Query("strTrainingDate") strTrainingDate:String) : Response<TrainingOverall.TrainingOverallResponse>

    @GET("/api/trainingoverall/groupsearch")
    suspend fun getNutritionOverallGroup(@Query("strOrganizationId") strOrganizationId: String,@Query("strGroupId") strGroupId: String, @Query("strTrainingDate") strTrainingDate:String) : Response<TrainingOverall.TrainingOverallResponse>

//    @GET("/api/training/groupsearch")
//    suspend fun getTrainingGroupStatus(@Query("strOrganizationId") strOrganizationId: String,@Query("strGroupId") strGroupId: String, @Query("strTrainingDate") strTrainingDate:String) : Response<TrainingGroupStatus.TrainingGroupStatusResponse>

    @GET("/api/mobile/search")
    suspend fun getTrainingGroupStatus(@Query("organizationId") organizationId:String ,@Query("groupId") groupId: String,@Query("type") type:String,@Query("date") date:String): Response<TrainingOverallSearchResponse>


    @POST("/api/training/delete")
    fun deleteTrainingStatus(@Body trainingGroupStatus: MutableList<DeleteTrainingGroupStatus>) : Call<TrainingInfoResponse.TrainingInfoResponses>

    @POST("/api/training/comment/insert")
    fun requestTrainingComment(@Body trainingCommentRequest: TrainingCommentRequest) : Call<TrainingComment.TrainingCommentResponse>

//    @DELETE("/api/training/comment/delete")
//    suspend fun deleteTrainingComment(@Query("id") id: String) : Call<TrainingComment.TrainingCommentResponse>
    @GET("/api/training/comment/search")
    suspend fun getTrainingComment(@Query("userId") userId: String,@Query("date") date: String) : Response<TrainingComment.TrainingCommentResponse>

    @DELETE("/api/training/comment/delete/{id}")
     fun deleteTrainingComment(@Path("id") id: String): Call<TrainingComment.TrainingCommentResponse>

//     @POST("/api/TrainingSubDetail/insert")
//     fun requestTrainingSubInsert(@Body trainingSub: TrainingSub) : Call<TrainingSubResponse.TrainingSubDetailResponse>

    @POST("/api/TrainingSubDetail/insertDetail")
    fun requestTrainingSubInsert(@Body trainingSubDetailInsert: TrainingSubDetailInsert) : Call<TrainingSubResponse.TrainingSubDetailResponse>


    @GET("/api/TrainingSubDetail/get")
    suspend fun getTrainingSubDetail(
        @Query("userId") userId: String,
        @Query("exerciseId") exerciseId: String,
        @Query("trainingDate") trainingDate: String,
        @Query("trainingTime") trainingTime: String
    ) : Response<TrainingSubResponse.TrainingSubDetailResponse>

    @PUT("/api/TrainingSubDetail/update")
    suspend fun requestTrainingSubUpdate(@Body request: TrainingSubResponse): Response<TrainingSubResponse.TrainingSubDetailResponse>

    @PUT("/api/TrainingSubDetail/update")
    suspend fun requestTrainingSubRpeUpdate(@Body request: TrainingSubDetailRpeRequest): Response<TrainingSubResponse.TrainingSubDetailResponse>

    @PUT("/api/TrainingSubDetail/update")
    suspend fun requestTrainingSubHealthUpdate(@Body request: TrainingSubDetailHealthRequest): Response<TrainingSubResponse.TrainingSubDetailResponse>

    @PUT("/api/TrainingSubDetail/update")
    suspend fun requestTrainingSubUrlUpdate(@Body request: TrainingSubDetailUrlRequest): Response<TrainingSubResponse.TrainingSubDetailResponse>

    @PUT("/api/TrainingSub/updateUrl")
    suspend fun requestTrainingSubRpeUpdateUrl(@Body request: TrainingSub): Call<TrainingSub.TrainingSubObejctResponse>

    @GET("/api/mobile/exercisedetail")
    suspend fun getExerciseDetail(@Query("userId") userId: String,@Query("date") date: String,@Query("exerciseId") exerciseId: String) : Response<TrainingInfoResponse.TrainingInfoResponses>

    @POST("/api/mobile/exerciseupdate")
    suspend fun requestExerciseUpdate(@Body trainingInfoList: List<TrainingInfoResponse>) : Response<ResponseBody>

    @GET("/api/mobile/trainingdetail")
    suspend fun getTrainingConfirm(@Query("userId") userId: String,@Query("date") date: String) : Response<TrainingConfirmResponse>

    @GET("/api/TrainingSubDetail/getTssDataTime")
    suspend fun getTssDataTime(@Query("userId") userId:String, @Query("trainingDate") trainingDate:String) : Response<TrainingTssDataTime.TrainingTssDataTimeResponse>


}