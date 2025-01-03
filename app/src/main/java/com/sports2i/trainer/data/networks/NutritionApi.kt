package com.sports2i.trainer.data.networks


import com.sports2i.trainer.data.model.NutritionDirection
import com.sports2i.trainer.data.model.NutritionDirectionResponse
import com.sports2i.trainer.data.model.NutritionDirectionSearchResponse
import com.sports2i.trainer.data.model.NutritionPictureUser
import com.sports2i.trainer.data.model.NutritionPictureUserResponse
import com.sports2i.trainer.data.model.NutritionResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface NutritionApi {

    @GET("/api/mobile/search")
    suspend fun searchNutrition(@Query("groupId") groupId: String,@Query("type") type:String,@Query("date") date:String): Response<NutritionResponse>

    @POST("/api/nutritionDirection/insert")
    suspend fun insertNutritionDirection(@Body nutritionDirection: NutritionDirection): Call<NutritionDirectionResponse>

    @GET("/api/nutritionDirection/search")
    suspend fun searchNutritionDirection(@Query("userId") userId: String,@Query("date") date:String): Response<NutritionDirectionSearchResponse>

    @PUT("/api/nutrition/update")
    suspend fun updateNutritionEvaluation(@Body nutritionPictureUserList: MutableList<NutritionPictureUser>): Response<NutritionPictureUserResponse>

    @POST("/api/nutrition/insert")
    suspend fun insertNutritionPicture(@Body nutritionPictureUserList: MutableList<NutritionPictureUser>): Call<NutritionPictureUserResponse>

    @GET("/api/nutrition/search")
    suspend fun searchNutritionPicture(@Query("userId") userId: String,@Query("date") date:String): Response<NutritionPictureUserResponse>

    @POST("/api/nutrition/delete")
    suspend fun deleteNutritionPicture(@Body nutritionPictureIdList: MutableList<Int>): Response<ResponseBody>
}