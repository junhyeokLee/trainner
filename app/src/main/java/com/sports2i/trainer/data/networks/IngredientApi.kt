package com.sports2i.trainer.data.networks

import com.sports2i.trainer.data.model.IngredientListResponse
import com.sports2i.trainer.data.model.IngredientResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IngredientApi {

    @GET("/api/drug/version")
    suspend fun getIngredientVersion(): Response<IngredientResponse>

    @GET("/api/drug/search")
    suspend fun getIngredientSearchList(@Query("version") version: String, @Query("category") category:String , @Query("keyword") keyword:String,
    @Query("lastSeq") lastSeq:Int, @Query("size") size:Int): Response<IngredientListResponse>

}