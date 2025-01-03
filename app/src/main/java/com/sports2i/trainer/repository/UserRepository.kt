package com.sports2i.trainer.repository

import com.sports2i.trainer.data.model.EmailCheckResponse
import com.sports2i.trainer.data.model.LoginRequest
import com.sports2i.trainer.data.model.LoginResponse
import com.sports2i.trainer.data.model.RequestUserResetPassword
import com.sports2i.trainer.data.model.UserResponseData
import com.sports2i.trainer.data.model.VerifyRequest
import com.sports2i.trainer.data.model.VerifyResponse
import com.sports2i.trainer.data.networks.UserApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response
import java.io.File

@Singleton
class UserRepository @Inject constructor(private val userApi: UserApi) {
    suspend fun getUserInfo(userId: String) : Response<UserResponseData> {
        return userApi.getUserInfo(userId)
    }
    suspend fun requestLogin(loginRequest: LoginRequest) : Response<LoginResponse> {
        return userApi.requestLogin(loginRequest)
    }
    suspend fun requestCheckEmail(email: LoginRequest) : Response<EmailCheckResponse> {
        return userApi.requestCheckEmail(email)
    }

    suspend fun requestEmailSend(verify: VerifyRequest) : Response<ResponseBody> {
        return userApi.requestEmailSend(verify)
    }
    suspend fun requestVerify(verify:VerifyRequest) : Response<VerifyResponse> {
        return userApi.requestVerify(verify)
    }
    suspend fun requestUserResetPassword(requestUserResetPassword: RequestUserResetPassword) : Response<ResponseBody> {
        return userApi.requestUserResetPassword(requestUserResetPassword)
    }

    suspend fun requestProfileUpdate(userId: String, imageFile: File): Response<ResponseBody> {

    val imageFiles: MutableList<File> = ArrayList<File>()
    imageFiles.add(imageFile)

    val imageParts = mutableListOf<MultipartBody.Part>()
    for (imageFile in imageFiles) {
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
        imageParts.add(part)
    }
    return userApi.requestProfileUpdate(userId, imageParts)

    }


    suspend fun getGroupUserInfo(auth:String) : Response<LoginResponse> {
        return userApi.getGroupUserInfo(auth)
    }
}