package com.sports2i.trainer.di

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sports2i.trainer.data.model.TokenRequest
import com.sports2i.trainer.data.model.TokenResponseData
import com.sports2i.trainer.data.networks.GroupApi
import com.sports2i.trainer.data.networks.IngredientApi
import com.sports2i.trainer.data.networks.NoticeApi
import com.sports2i.trainer.data.networks.NutritionApi
import com.sports2i.trainer.data.networks.PainInfoApi
import com.sports2i.trainer.data.networks.SurveyApi
import com.sports2i.trainer.data.networks.TokenApi
import com.sports2i.trainer.data.networks.TrainingApi
import com.sports2i.trainer.data.networks.TrainingOverallApi
import com.sports2i.trainer.data.networks.UserApi
import com.sports2i.trainer.db.TrackingDatabase
import com.sports2i.trainer.ui.activity.LoginActivity
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    class AuthInterceptor(private val context: Context) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val token = Global.myInfo.accessToken
            val refreshToken = Global.myInfo.refreshToken

            val originalRequest = chain.request()
                .newBuilder()
                .header("Authorization", token)
                .build()

            var response = chain.proceed(originalRequest)

            val responseBody = response.body
            val responseBodyString = responseBody?.let { body ->
                val source = body.source()
                source.request(Long.MAX_VALUE)
                val buffer = source.buffer
                buffer.clone().readUtf8()
            } ?: "Response body is null"

            val json = JSONObject(responseBodyString)
            val msg = json.optString("msg", "")

            if(response.code == 401 && msg.equals("로그인이 필요합니다.")){
                runBlocking {
                        val newTokenResponse = getNewToken(TokenRequest("", "", "", refreshToken))
                        if (newTokenResponse.isSuccessful) {
                            newTokenResponse.body()?.let {
                                val newToken = it.data?.accessToken!!
                                Preferences.put(Preferences.KEY_ACCESS_TOKEN, newToken)
                                Global.myInfo.accessToken = newToken

                                val newRequest = chain.request()
                                    .newBuilder()
                                    .header("Authorization", newToken)
                                    .build()

                                response = chain.proceed(newRequest)

                            }
                        } else {
                            // 토큰 갱신에 실패한 경우 처리
                                customRefreshCheckDialog(context, true)
                        }
                    }
                } else if(response.code == 401 && msg.equals("로그인을 다시 해주십시오.")) {
                Log.e("로그인을 다시 해주십시오", "refreshToken : $refreshToken")
                    customRefreshCheckDialog(context,true)
            }
            return response
        }
    }

    private suspend fun getNewToken(refreshToken: TokenRequest?): retrofit2.Response<TokenResponseData> {
        val service = createTokenApiService()
        return service.refreshToken(refreshToken!!)
    }
    private fun createTokenApiService(): TokenApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(Global.apiBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(TokenApi::class.java)
    }

        private fun customRefreshCheckDialog(context: Context,reissueCheck:Boolean){
            context?.let { it1 -> Preferences.init(it1, Preferences.DB_USER_INFO) }
            Preferences.clear()
            Global.myInfo.accessToken = ""
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("reissue",reissueCheck)
            context.startActivity(intent)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        @ApplicationContext context: Context // ApplicationContext 사용
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Global.apiBase)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
    @Provides
    @Singleton
    fun provideAuthInterceptor(@ApplicationContext context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }
    @Provides
    @Singleton
    fun provideTokenApi(retrofit: Retrofit): TokenApi {
        return retrofit.create(TokenApi::class.java)
    }
    @Provides
    @Singleton
    fun provideGroupApi(retrofit: Retrofit): GroupApi {
        return retrofit.create(GroupApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTrainingApi(retrofit: Retrofit): TrainingApi {
        return retrofit.create(TrainingApi::class.java)
    }
    @Provides
    @Singleton
    fun provideTrainingOverallApi(retrofit: Retrofit): TrainingOverallApi {
        return retrofit.create(TrainingOverallApi::class.java)
    }
    @Provides
    @Singleton
    fun providePainInfoApi(retrofit: Retrofit): PainInfoApi {
        return retrofit.create(PainInfoApi::class.java)
    }
    @Provides
    @Singleton
    fun provideNoticegApi(retrofit: Retrofit): NoticeApi {
        return retrofit.create(NoticeApi::class.java)
    }
    @Provides
    @Singleton
    fun provideSurveyApi(retrofit: Retrofit): SurveyApi {
        return retrofit.create(SurveyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNutritionApi(retrofit: Retrofit): NutritionApi {
        return retrofit.create(NutritionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIngredientApi(retrofit: Retrofit): IngredientApi {
        return retrofit.create(IngredientApi::class.java)
    }
}
