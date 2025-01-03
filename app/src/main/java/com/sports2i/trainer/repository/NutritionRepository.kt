package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.NutritionDirection
import com.sports2i.trainer.data.model.NutritionDirectionResponse
import com.sports2i.trainer.data.model.NutritionDirectionSearchResponse
import com.sports2i.trainer.data.model.NutritionPictureUser
import com.sports2i.trainer.data.model.NutritionPictureUserResponse
import com.sports2i.trainer.data.model.NutritionResponse
import com.sports2i.trainer.data.networks.NutritionApi
import okhttp3.ResponseBody
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
class NutritionRepository @Inject constructor(private val nutritionApi: NutritionApi) {

    suspend fun searchNutrition(groupId:String,type:String,date:String) : Response<NutritionResponse> {
        return nutritionApi.searchNutrition(groupId,type,date)
    }

    suspend fun insertNutritionDirection(nutritionDirection: NutritionDirection) : Call<NutritionDirectionResponse> {
        return nutritionApi.insertNutritionDirection(nutritionDirection)
    }

    suspend fun searchNutritionDirection(userId:String,date:String) : Response<NutritionDirectionSearchResponse> {
        return nutritionApi.searchNutritionDirection(userId,date)
    }

    suspend fun updateNutritionEvaluation(nutritionPictureUserList: MutableList<NutritionPictureUser>) : Response<NutritionPictureUserResponse> {
        return nutritionApi.updateNutritionEvaluation(nutritionPictureUserList)
    }

    suspend fun insertNutritionPicture(nutritionPictureUserList: MutableList<NutritionPictureUser>) : Call<NutritionPictureUserResponse> {
        return nutritionApi.insertNutritionPicture(nutritionPictureUserList)
    }

    suspend fun searchNutritionPicture(userId:String,date:String) : Response<NutritionPictureUserResponse> {
        return nutritionApi.searchNutritionPicture(userId,date)
    }

    suspend fun deleteNutritionPicture(nutritionPictureIdList: MutableList<Int>) : Response<ResponseBody> {
        return nutritionApi.deleteNutritionPicture(nutritionPictureIdList)
    }

}