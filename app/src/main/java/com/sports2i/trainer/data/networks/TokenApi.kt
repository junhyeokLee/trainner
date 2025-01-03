package com.sports2i.trainer.data.networks

import com.sports2i.trainer.data.model.TokenRequest
import com.sports2i.trainer.data.model.TokenResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenApi {
    @POST("/api/auth/reissue")
    suspend fun refreshToken(@Body refreshBody : TokenRequest): Response<TokenResponseData>

}