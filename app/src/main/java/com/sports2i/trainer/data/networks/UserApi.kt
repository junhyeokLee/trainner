package com.sports2i.trainer.data.networks

import com.sports2i.trainer.data.model.EmailCheckResponse
import com.sports2i.trainer.data.model.VerifyRequest
import com.sports2i.trainer.data.model.LoginRequest
import com.sports2i.trainer.data.model.LoginResponse
import com.sports2i.trainer.data.model.RequestProfileUpdate
import com.sports2i.trainer.data.model.RequestUserResetPassword
import com.sports2i.trainer.data.model.TrainingComment
import com.sports2i.trainer.data.model.UserResponseData
import com.sports2i.trainer.data.model.VerifyResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    @GET("/api/account/{userId}")
    suspend fun getUserInfo(@Path("userId") userId: String): Response<UserResponseData>

    @POST("/api/auth/login")
    suspend fun requestLogin(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/check")
    suspend fun requestCheckEmail(@Body email: LoginRequest): Response<EmailCheckResponse>

    @POST("/api/email/send")
    suspend fun requestEmailSend(@Body email: VerifyRequest): Response<ResponseBody>
    @POST("/api/auth/verify")
    suspend fun requestVerify(@Body email: VerifyRequest): Response<VerifyResponse>

    @PUT("/api/account/reset")
    suspend fun requestUserResetPassword(@Body request: RequestUserResetPassword): Response<ResponseBody>

    @Multipart
    @PUT("/api/account/profile/{userId}")
    suspend fun requestProfileUpdate(@Path("userId") userId: String, @Part file: List<MultipartBody.Part>): Response<ResponseBody>

    @DELETE("/api/training/comment/delete/{id}")
    fun deleteTrainingComment(@Path("id") id: String): Call<TrainingComment.TrainingCommentResponse>


    @GET("/api/auth/info}")
    suspend fun getGroupUserInfo(@Header("Authorization") auth:String): Response<LoginResponse>
}