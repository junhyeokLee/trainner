package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.TokenRequest
import com.sports2i.trainer.data.model.TokenResponseData
import com.sports2i.trainer.data.networks.TokenApi
import javax.inject.Inject
import retrofit2.Response

class TokenRepository @Inject constructor(private val tokenApi: TokenApi) {
    suspend fun refreshToken(refreshBody: TokenRequest) : Response<TokenResponseData> {
        return tokenApi.refreshToken(refreshBody)
    }
}

//class TokenRepository(private val tokenApi: TokenApi) {
//    suspend fun refreshToken(refreshToken: TokenRequest): Response<TokenResponseData> {
//        // Retrofit을 사용하여 리프레시 토큰 요청을 보내고 새로운 액세스 토큰을 반환합니다.
//        return tokenApi.refreshToken(refreshToken)
//    }
//}
