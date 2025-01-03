package com.sports2i.trainer.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.IngredientListResponse
import com.sports2i.trainer.data.model.IngredientResponse
import com.sports2i.trainer.repository.IngredientRepository
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IngredientViewModel @Inject constructor(private val ingredientRepository: IngredientRepository, @ApplicationContext private val context: Context): ViewModel() {

    val ingredientState: MutableLiveData<NetworkState<IngredientResponse>> = MutableLiveData()
    val ingredientListState: MutableLiveData<NetworkState<IngredientListResponse>> = MutableLiveData()

    fun getIngredient() = viewModelScope.launch {
        ingredientState.value = NetworkState.Loading(true)
        val response = ingredientRepository.getIngredient()
        if (response.isSuccessful) {
            response.body()?.let {
                ingredientState.value = NetworkState.Success(it)
            }?: run {
                ingredientState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            ingredientState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

    fun getIngredientList(version:String,category:String,keyword:String,lastSeq:Int,size:Int) = viewModelScope.launch {
        ingredientListState.value = NetworkState.Loading(true)
        val response = ingredientRepository.getIngredientList(version,category,keyword,lastSeq,size)
        if (response.isSuccessful) {
            response.body()?.let {
                ingredientListState.value = NetworkState.Success(it)
            }?: run {
                ingredientListState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
            }
        }else {
            ingredientListState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
        }
    }

}