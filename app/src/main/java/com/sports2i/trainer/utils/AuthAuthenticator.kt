package com.mrntlu.tokenauthentication.utils

import android.util.Log
import com.sports2i.trainer.data.model.TokenRequest
import com.sports2i.trainer.data.model.TokenResponseData
import com.sports2i.trainer.data.networks.TokenApi
import com.sports2i.trainer.utils.Global
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val token: String,
): Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        return runBlocking {
            val newToken = getNewToken(TokenRequest("", "", "", token))
            newToken.body()?.let {
                response.request.newBuilder()
                    .header("Authorization", token)
                    .build()
            }
        }
    }

    private suspend fun getNewToken(refreshToken: TokenRequest?): retrofit2.Response<TokenResponseData> {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Global.apiBase)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        val service = retrofit.create(TokenApi::class.java)
        return service.refreshToken(refreshToken!!)
    }
}