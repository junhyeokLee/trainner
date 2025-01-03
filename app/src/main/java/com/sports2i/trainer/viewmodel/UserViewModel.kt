package com.sports2i.trainer.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.EmailCheckResponse
import com.sports2i.trainer.data.model.VerifyRequest
import com.sports2i.trainer.data.model.LoginRequest
import com.sports2i.trainer.data.model.LoginResponse
import com.sports2i.trainer.data.model.RequestProfileUpdate
import com.sports2i.trainer.data.model.RequestUserResetPassword
import com.sports2i.trainer.data.model.UserResponseData
import com.sports2i.trainer.data.model.VerifyResponse
import com.sports2i.trainer.repository.UserRepository
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.Global.getString
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import javax.inject.Inject

    @HiltViewModel
    class UserViewModel @Inject constructor(
        private val userRepository: UserRepository,
        @ApplicationContext private val context: Context
    ) : ViewModel() {

         val userState: MutableLiveData<NetworkState<UserResponseData>> = MutableLiveData()
         val loginState: MutableLiveData<NetworkState<LoginResponse>> = MutableLiveData()
         val passwordResetState: MutableLiveData<NetworkState<String>> = MutableLiveData()
         val profileUpdateState: MutableLiveData<NetworkState<String>> = MutableLiveData()
         var userInfoState: MutableLiveData<NetworkState<LoginResponse>> = MutableLiveData()
         val emailCheckState: MutableLiveData<NetworkState<EmailCheckResponse>> = MutableLiveData()
         val emailSendState: MutableLiveData<NetworkState<Boolean>> = MutableLiveData()
         val verifyState : MutableLiveData<NetworkState<VerifyResponse>> = MutableLiveData()


        fun getUserInfo(userId: String) = viewModelScope.launch {
            userState.value = NetworkState.Loading(true)
            try {
                val response = userRepository.getUserInfo(userId)
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        userState.value = NetworkState.Success(user)
                    } ?: run {
                        userState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                    }
                } else {
                    userState.value = NetworkState.Error(context.getString(R.string.network_login_faile))
                }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> userState.value = NetworkState.Error(context.getString(R.string.network_error))
                    else -> userState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
                }
            }
        }

        // 로그인 요청
        fun requestLogin(email: String, password: String) = viewModelScope.launch {
            loginState.value = NetworkState.Loading(true)
            try {
                    val loginRequest = LoginRequest(email = email, password = password)
                    val response = userRepository.requestLogin(loginRequest = loginRequest)
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            loginState.value = NetworkState.Success(user)
                        } ?: run {
                            loginState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        loginState.value = NetworkState.Error(context.getString(R.string.network_login_faile))
                    }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> {
                        loginState.value = NetworkState.Error(context.getString(R.string.network_error))
                    }
                    else -> {
                        loginState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
                    }
                }
            }
        }

        fun requestCheckEmail(email: LoginRequest) = viewModelScope.launch {
            emailCheckState.value = NetworkState.Loading(true)
            try {
                val response = userRepository.requestCheckEmail(email)
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        emailCheckState.value = NetworkState.Success(data)
                    } ?: run {
                        emailCheckState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                    }
                } else {
                    emailCheckState.value = NetworkState.Error(context.getString(R.string.network_login_faile))
                }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> {
                        emailCheckState.value = NetworkState.Error(context.getString(R.string.network_error))
                    }
                    else -> {
                        emailCheckState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
                    }
                }
            }
        }

        fun requestEmailSend(verify: VerifyRequest) = viewModelScope.launch {
            emailSendState.value = NetworkState.Loading(true)
            try {
                val response = userRepository.requestEmailSend(verify)
                val success = response.isSuccessful
                val message = response.message() // Use the response message

                Log.e("TAG", "success: $success")
                if (success) emailSendState.value = NetworkState.Success(success)
                else emailSendState.value = NetworkState.Error(message)

            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> emailSendState.value = NetworkState.Error(context.getString(R.string.network_error))
                    else -> emailSendState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
                }
            }
        }

        fun requestVerify(verify: VerifyRequest) = viewModelScope.launch {
            verifyState.value =  NetworkState.Loading(true)
            try{
                val response = userRepository.requestVerify(verify)
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        verifyState.value = NetworkState.Success(data)
                    } ?: run {
                        verifyState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                    }
                } else {
                    verifyState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }

            } catch (ex:Exception){
                when(ex){
                    is IOException -> verifyState.value = NetworkState.Error(context.getString(R.string.network_error))
                    else -> verifyState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
                }
            }
        }

        // 비밀번호 변경 요청
        fun requestUserResetPassword(requestUserResetPassword: RequestUserResetPassword) = viewModelScope.launch {
            passwordResetState.value = NetworkState.Loading(true)
            try {
                val request = requestUserResetPassword
                val response = userRepository.requestUserResetPassword(request)

                val success = response.isSuccessful
                val message = response.message() // Use the response message

                if (success) passwordResetState.value = NetworkState.Success(message)
                else passwordResetState.value = NetworkState.Error(message)

            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> passwordResetState.value = NetworkState.Error(context.getString(R.string.network_error))
                    else -> passwordResetState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
                }
            }
        }

        fun requestProfileUpdate(userId: String, imageFile: File) = viewModelScope.launch {
            profileUpdateState.value = NetworkState.Loading(true)
            try {
                val response = userRepository.requestProfileUpdate(userId, imageFile)

                val success = response.isSuccessful
                val message = response.message() // Use the response message

                if (success) profileUpdateState.value = NetworkState.Success(message)
                else profileUpdateState.value = NetworkState.Error(message)

            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> profileUpdateState.value = NetworkState.Error(context.getString(R.string.network_error))
                    else -> profileUpdateState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
                }
            }
        }

        fun getGroupUserInfo(auth:String) = viewModelScope.launch {
            userInfoState.value = NetworkState.Loading(true)
            try {
                val response = userRepository.getGroupUserInfo(auth)
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        userInfoState.value = NetworkState.Success(user)
                    } ?: run {
                        userInfoState.value = NetworkState.Error(getString(R.string.network_empty_date))
                    }
                } else {
                    userInfoState.value = NetworkState.Error(getString(R.string.network_login_faile))
                }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> userInfoState.value = NetworkState.Error(getString(R.string.network_error))
                    else -> userInfoState.value = NetworkState.Error(getString(R.string.network_data_change_error))
                }
            }
        }
    }
