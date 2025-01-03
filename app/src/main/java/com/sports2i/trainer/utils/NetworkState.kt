package com.sports2i.trainer.utils

//sealed class NetworkState<out T : Any> {
//    object Loading : NetworkState<Nothing>()
//    data class Success<T : Any>(val data: T) : NetworkState<T>()
//    data class Error(val message: String) : NetworkState<Nothing>()
//}

sealed class NetworkState<out T : Any> {
    data class Loading<out T : Any>(val isLoading: Boolean) : NetworkState<T>()
    data class Success<T : Any>(val data: T) : NetworkState<T>()
    data class Error(val message: String) : NetworkState<Nothing>()
}
