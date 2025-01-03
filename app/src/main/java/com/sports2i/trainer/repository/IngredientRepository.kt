package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.IngredientListResponse
import com.sports2i.trainer.data.model.IngredientResponse
import com.sports2i.trainer.data.networks.IngredientApi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngredientRepository @Inject constructor(private val ingredientApi: IngredientApi) {
    suspend fun getIngredient(): Response<IngredientResponse> {
        return ingredientApi.getIngredientVersion()
    }

    suspend fun getIngredientList(version:String,category:String,keyword:String,lastSeq:Int,size:Int): Response<IngredientListResponse> {
        return ingredientApi.getIngredientSearchList(version,category,keyword,lastSeq,size)
    }
}