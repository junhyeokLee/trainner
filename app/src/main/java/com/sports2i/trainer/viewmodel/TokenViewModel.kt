package com.sports2i.trainer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TokenRequest
import com.sports2i.trainer.data.model.TokenResponseData
import com.sports2i.trainer.repository.TokenRepository
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

    @HiltViewModel
    class TokenViewModel @Inject constructor(
        private val tokenRepository: TokenRepository,
        @ApplicationContext private val context: Context
    ) : ViewModel() {

        val tokenState: MutableLiveData<NetworkState<TokenResponseData>> = MutableLiveData()
        // 토큰 요청
        fun requestToken(refreshToken: String) = viewModelScope.launch {
            tokenState.value = NetworkState.Loading(true)
            try {
                    val tokenRequest = TokenRequest("","","",refreshToken)
                    val response = tokenRepository.refreshToken(tokenRequest)
                    if (response.isSuccessful) {
                        response.body()?.let { token ->
                            tokenState.value = NetworkState.Success(token)
                        } ?: run {
                            tokenState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        tokenState.value = NetworkState.Error(context.getString(R.string.network_login_faile))
                    }
            } catch (ex: Exception) {
                when (ex) {
                    is IOException -> {
                        tokenState.value = NetworkState.Error(context.getString(R.string.network_error))
                    }
                    else -> {
                        tokenState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
                    }
                }
            }
        }
    }
