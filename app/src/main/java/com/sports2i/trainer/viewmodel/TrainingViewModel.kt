package com.sports2i.trainer.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.DeleteTrainingGroupStatus
import com.sports2i.trainer.data.model.ExerciseItemResponse
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.data.model.ExerciseTimeItemResponse
import com.sports2i.trainer.data.model.ExerciseUnitResponse
import com.sports2i.trainer.data.model.TrainingComment
import com.sports2i.trainer.data.model.TrainingCommentRequest
import com.sports2i.trainer.data.model.TrainingConfirmResponse
import com.sports2i.trainer.data.model.TrainingExercise
import com.sports2i.trainer.data.model.TrainingGroupStatus
import com.sports2i.trainer.data.model.TrainingInfo
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingOverall
import com.sports2i.trainer.data.model.TrainingOverallSearch
import com.sports2i.trainer.data.model.TrainingOverallSearchResponse
import com.sports2i.trainer.data.model.TrainingSub
import com.sports2i.trainer.data.model.TrainingSubDetailHealthRequest
import com.sports2i.trainer.data.model.TrainingSubDetailInsert
import com.sports2i.trainer.data.model.TrainingSubDetailRpeRequest
import com.sports2i.trainer.data.model.TrainingSubDetailUrlRequest
import com.sports2i.trainer.data.model.TrainingSubResponse
import com.sports2i.trainer.data.model.TrainingTssDataTime
import com.sports2i.trainer.repository.TrainingRepository
import com.sports2i.trainer.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(private val TrainingRepository: TrainingRepository, @ApplicationContext private val context: Context
): ViewModel() {

    val trainingInfoState : MutableLiveData<NetworkState<MutableList<TrainingInfo>>> = MutableLiveData()
    val exerciseItemState: MutableLiveData<NetworkState<ExerciseItemResponse>> = MutableLiveData()
    val exerciseUnitState: MutableLiveData<NetworkState<ExerciseUnitResponse>> = MutableLiveData()
    val exerciseTimeItemState: MutableLiveData<NetworkState<ExerciseTimeItemResponse>> = MutableLiveData()
    val trainingStatusState: MutableLiveData<NetworkState<TrainingInfoResponse.TrainingInfoResponses>> = MutableLiveData()
    val trainingIndiStatusState: MutableLiveData<NetworkState<TrainingExercise.TrainingExerciseResponse>> = MutableLiveData()
    val trainingIndiStatusStateError: MutableLiveData<Boolean> = MutableLiveData()
    val exercisePresetState: MutableLiveData<NetworkState<MutableList<ExercisePreset>>> = MutableLiveData()
    val exercisePresetResponseState: MutableLiveData<NetworkState<ExercisePreset.ExercisePresetResponse>> = MutableLiveData()
    val trainingOverallUserState: MutableLiveData<NetworkState<TrainingOverall.TrainingOverallResponse>> = MutableLiveData()
    val trainingOverallGroupState: MutableLiveData<NetworkState<TrainingOverall.TrainingOverallResponse>> = MutableLiveData()
    val nutritionOverallGroupState: MutableLiveData<NetworkState<TrainingOverall.TrainingOverallResponse>> = MutableLiveData()
    val trainingGroupStatusState: MutableLiveData<NetworkState<TrainingOverallSearchResponse>> = MutableLiveData()
    val trainingExerciseListState: MutableLiveData<NetworkState<MutableList<TrainingOverallSearch>>> = MutableLiveData()
//    val trainingExerciseListState: MutableLiveData<NetworkState<MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>>> = MutableLiveData()

    val trainingCommentState: MutableLiveData<NetworkState<TrainingComment.TrainingCommentResponse>> = MutableLiveData()
    val trainingCommentAddState: MutableLiveData<NetworkState<TrainingComment.TrainingCommentResponse>> = MutableLiveData()
    val trainingSubInsertState : MutableLiveData<NetworkState<TrainingSubResponse.TrainingSubDetailResponse>> = MutableLiveData()
    val trainingInfoUpdateState : MutableLiveData<NetworkState<TrainingInfoResponse.TrainingInfoUpdateResponse>> = MutableLiveData()
    val trainingSubDetailUpdateState : MutableLiveData<NetworkState<TrainingSubResponse.TrainingSubDetailResponse>> = MutableLiveData()
    val trainingSubSearchState : MutableLiveData<NetworkState<TrainingSubResponse.TrainingSubDetailResponse>> = MutableLiveData()
    val trainingSubSearchStateSuceess: MutableLiveData<Boolean> = MutableLiveData()
    val trainingDetailExerciseState: MutableLiveData<NetworkState<TrainingInfoResponse.TrainingInfoResponses>> = MutableLiveData()
    val trainingDetailExerciseStateSuceess: MutableLiveData<Boolean> = MutableLiveData()
    val trainingExerciseUpdateState: MutableLiveData<NetworkState<String>> = MutableLiveData()
    val trainingTssDataTimeState: MutableLiveData<NetworkState<TrainingTssDataTime.TrainingTssDataTimeResponse>> = MutableLiveData()
    val trainingConfirmState : MutableLiveData<NetworkState<TrainingConfirmResponse>> = MutableLiveData()
    val trainingConfirmStateSuceess: MutableLiveData<Boolean> = MutableLiveData()


    fun getExerciseItemSearch() = viewModelScope.launch {
        exerciseItemState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getExerciseItemSearch()
            if(response.isSuccessful){
                response.body()?.let { exerciseItem ->
                    exerciseItemState.value = NetworkState.Success(exerciseItem)
                } ?: run {
                    exerciseItemState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                exerciseItemState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }

    fun getExerciseUnitSearch() = viewModelScope.launch {
        exerciseUnitState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getExerciseUnitSearch()
            if(response.isSuccessful){
                response.body()?.let { exerciseUnit ->
                    exerciseUnitState.value = NetworkState.Success(exerciseUnit)
                } ?: run {
                    exerciseUnitState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                exerciseUnitState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }

    fun getExerciseTimeItemSearch() = viewModelScope.launch {
        exerciseTimeItemState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getExerciseTimeItemSearch()
            if(response.isSuccessful){
                response.body()?.let { exerciseTimeItem ->
                    exerciseTimeItemState.value = NetworkState.Success(exerciseTimeItem)
                } ?: run {
                    exerciseTimeItemState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                exerciseTimeItemState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }

    fun requestTrainingInfo(trainingInfo: MutableList<TrainingInfo>) = viewModelScope.launch {
        trainingInfoState.value = NetworkState.Loading(true)
        Log.e("requestTrainingInfo out", trainingInfo.toString())

        try{
            val response = TrainingRepository.requestTrainingSave(trainingInfo)
            response.enqueue(object : Callback<TrainingInfoResponse.TrainingInfoResponses>{
                override fun onResponse(call: Call<TrainingInfoResponse.TrainingInfoResponses>, response: Response<TrainingInfoResponse.TrainingInfoResponses>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            trainingInfoState.value = NetworkState.Success(trainingInfo)
                            Log.e("requestTrainingInfo in", trainingInfo.toString())
                        } ?: run {
                            trainingInfoState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        trainingInfoState.value = NetworkState.Error(context.getString(R.string.network_fail))
                    }
                }
                override fun onFailure(call: Call<TrainingInfoResponse.TrainingInfoResponses>, t: Throwable) {
                    trainingInfoState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }

    fun getTrainingStatus(strUserId: String,strTrainingDate: String) = viewModelScope.launch {
        trainingStatusState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getTrainingStatus(strUserId,strTrainingDate)
            if(response.isSuccessful){
                response.body()?.let { trainingStatus ->
                    trainingStatusState.value = NetworkState.Success(trainingStatus)
                } ?: run {
                    trainingStatusState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                trainingStatusState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }

    fun requestExercisePreset(exercisePresets: MutableList<ExercisePreset>) = viewModelScope.launch {
        exercisePresetState.value = NetworkState.Loading(true)

        try{
            val response = TrainingRepository.requestExercisePresetSave(exercisePresets)
            response.enqueue(object : Callback<ExercisePreset.ExercisePresetResponse>{
                override fun onResponse(call: Call<ExercisePreset.ExercisePresetResponse>, response: Response<ExercisePreset.ExercisePresetResponse>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            exercisePresetState.value = NetworkState.Success(exercisePresets)
                        } ?: run {
                            exercisePresetState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        exercisePresetState.value = NetworkState.Error(context.getString(R.string.network_fail))
                    }
                }
                override fun onFailure(call: Call<ExercisePreset.ExercisePresetResponse>, t: Throwable) {
                    exercisePresetState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }

    fun getExercisePreset(strUserId: String) = viewModelScope.launch {
        exercisePresetResponseState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getExercisePreset(strUserId)
            if(response.isSuccessful){
                response.body()?.let { exercisePreset ->
                    exercisePresetResponseState.value = NetworkState.Success(exercisePreset)
                } ?: run {
                    exercisePresetResponseState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                exercisePresetResponseState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }

    fun deleteExercisePreset(strExercisePresetId: String) = viewModelScope.launch {
        exercisePresetResponseState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.deleteExercisePreset(strExercisePresetId)
            response.enqueue(object : Callback<ExercisePreset.ExercisePresetResponse>{
                override fun onResponse(call: Call<ExercisePreset.ExercisePresetResponse>, response: Response<ExercisePreset.ExercisePresetResponse>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            exercisePresetResponseState.value = NetworkState.Success(data)
                        } ?: run {
                            exercisePresetResponseState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        exercisePresetResponseState.value = NetworkState.Error(context.getString(R.string.network_fail))
                    }
                }
                override fun onFailure(call: Call<ExercisePreset.ExercisePresetResponse>, t: Throwable) {
                    exercisePresetResponseState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }

    fun getTrainingOverallUser(strUserId:String,strTrainingDate:String) = viewModelScope.launch {
        trainingOverallUserState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getTrainingOverallUser(strUserId,strTrainingDate)
            if(response.isSuccessful){
                response.body()?.let { trainingOverallUserStatus ->
                    trainingOverallUserState.value = NetworkState.Success(trainingOverallUserStatus)
                } ?: run {
                    trainingOverallUserState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                trainingOverallUserState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingOverallUserState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingOverallUserState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun getTrainingOverallGroup(strOrganizationId:String,strGroupId:String,strTrainingDate:String) = viewModelScope.launch {
        trainingOverallGroupState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getTrainingOverallGroup(strOrganizationId,strGroupId,strTrainingDate)
            if(response.isSuccessful){
                response.body()?.let { trainingOverallGroupStatus ->
                    trainingOverallGroupState.value = NetworkState.Success(trainingOverallGroupStatus)
                } ?: run {
                    trainingOverallGroupState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                trainingOverallGroupState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingOverallGroupState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingOverallGroupState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun getNutritionOverallGroup(strOrganizationId:String,strGroupId:String,strTrainingDate:String) = viewModelScope.launch {
        nutritionOverallGroupState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getNutritionOverallGroup(strOrganizationId,strGroupId,strTrainingDate)
            if(response.isSuccessful){
                response.body()?.let { nutritionOverallGroupStatus ->
                    nutritionOverallGroupState.value = NetworkState.Success(nutritionOverallGroupStatus)
                } ?: run {
                    nutritionOverallGroupState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                nutritionOverallGroupState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }



    fun getTrainingGroupStatus(organizationId: String,groupId: String, type:String, date:String) = viewModelScope.launch {
        trainingGroupStatusState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getTrainingGroupStatus(organizationId,groupId,type,date)
            if(response.isSuccessful){
                response.body()?.let { trainingGroupStatus ->
                    trainingGroupStatusState.value = NetworkState.Success(trainingGroupStatus)
                } ?: run {
                    trainingGroupStatusState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                trainingGroupStatusState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingGroupStatusState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingGroupStatusState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun getTrainingGroupStatus(organizationId: String,userId:String ,groupId: String, type:String, date:String) = viewModelScope.launch {
        trainingExerciseListState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getTrainingGroupStatus(organizationId,groupId,type,date)
            if(response.isSuccessful){
                response.body()?.let { trainingGroupStatus ->
                    val userOverallList = trainingGroupStatus.data.filter { it.userId == userId } as MutableList<TrainingOverallSearch>
                    // 유저 아이디로 필터후 보여줌
                    trainingExerciseListState.value = NetworkState.Success(userOverallList)
                } ?: run {
                    trainingExerciseListState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                trainingExerciseListState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingExerciseListState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingExerciseListState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun deleteTrainingStatus(trainingGroupStatus: MutableList<DeleteTrainingGroupStatus>) = viewModelScope.launch {
        trainingStatusState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.deleteTrainingStatus(trainingGroupStatus)
            response.enqueue(object : Callback<TrainingInfoResponse.TrainingInfoResponses>{
                override fun onResponse(call: Call<TrainingInfoResponse.TrainingInfoResponses>, response: Response<TrainingInfoResponse.TrainingInfoResponses>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            trainingStatusState.value = NetworkState.Success(data)
                        } ?: run {
                            trainingStatusState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        trainingStatusState.value = NetworkState.Error(context.getString(R.string.network_fail))
                    }
                }
                override fun onFailure(call: Call<TrainingInfoResponse.TrainingInfoResponses>, t: Throwable) {
                    trainingStatusState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingStatusState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingStatusState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun requestTrainingComment(trainingCommentRequest: TrainingCommentRequest) = viewModelScope.launch {
        trainingCommentAddState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.requestTrainingComment(trainingCommentRequest)
            response.enqueue(object : Callback<TrainingComment.TrainingCommentResponse>{
                override fun onResponse(call: Call<TrainingComment.TrainingCommentResponse>, response: Response<TrainingComment.TrainingCommentResponse>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            trainingCommentAddState.value = NetworkState.Success(data)
                        } ?: run {
                            trainingCommentAddState.value = NetworkState.Error(context.getString(R.string.network_empty_date))

                        }
                    } else {
                        trainingCommentAddState.value = NetworkState.Error(context.getString(R.string.network_fail))

                    }
                }
                override fun onFailure(call: Call<TrainingComment.TrainingCommentResponse>, t: Throwable) {
                    trainingCommentAddState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingCommentAddState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingCommentAddState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun getTrainingComment(userId:String,date:String) = viewModelScope.launch {
        trainingCommentState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getTrainingComment(userId,date)
            if(response.isSuccessful){
                response.body()?.let {
                    trainingCommentState.value = NetworkState.Success(it)
                } ?: run {
                    trainingCommentState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                trainingCommentState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingCommentState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingCommentState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun getTrainingSubDetail(userId:String,exerciseId:String,trainingDate:String,trainingTime:String) = viewModelScope.launch {
        trainingSubSearchState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getTrainingSubDetail(userId,exerciseId,trainingDate,trainingTime)
            if(response.isSuccessful){
                response.body()?.let { trainingSubSearch ->
                    trainingSubSearchState.value = NetworkState.Success(trainingSubSearch)
                    trainingSubSearchStateSuceess.value = true
                } ?: run {
                    trainingSubSearchState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                    trainingSubSearchStateSuceess.value = false
                }
            } else {
                trainingSubSearchState.value = NetworkState.Error(context.getString(R.string.network_fail))
                trainingSubSearchStateSuceess.value = false
            }
        }
        catch (ex: Exception){
            handleException(ex)
            trainingSubSearchStateSuceess.value = false

        }
    }

    fun deleteTrainingComment(id: String) = viewModelScope.launch {
        trainingCommentState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.deleteTrainingComment(id)
            response.enqueue(object : Callback<TrainingComment.TrainingCommentResponse>{
                override fun onResponse(call: Call<TrainingComment.TrainingCommentResponse>, response: Response<TrainingComment.TrainingCommentResponse>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            trainingCommentState.value = NetworkState.Success(data)
                        } ?: run {
                            trainingCommentState.value = NetworkState.Error(context.getString(R.string.network_empty_date))

                        }
                    } else {
                        trainingCommentState.value = NetworkState.Error(context.getString(R.string.network_fail))

                    }
                }
                override fun onFailure(call: Call<TrainingComment.TrainingCommentResponse>, t: Throwable) {
                    trainingCommentState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingCommentState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingCommentState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun requestTrainingSubInsert(trainingSubDetailInsert: TrainingSubDetailInsert) = viewModelScope.launch {
        trainingSubInsertState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.requestTrainingSubInsert(trainingSubDetailInsert)
            response.enqueue(object : Callback<TrainingSubResponse.TrainingSubDetailResponse>{
                override fun onResponse(call: Call<TrainingSubResponse.TrainingSubDetailResponse>, response: Response<TrainingSubResponse.TrainingSubDetailResponse>) {
                    if(response.isSuccessful){
                        response.body()?.let { data ->
                            trainingSubInsertState.value = NetworkState.Success(data)
                        } ?: run {
                            trainingSubInsertState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                        }
                    } else {
                        trainingSubInsertState.value = NetworkState.Error(context.getString(R.string.network_fail))
                    }
                }
                override fun onFailure(call: Call<TrainingSubResponse.TrainingSubDetailResponse>, t: Throwable) {
                    trainingSubInsertState.value = NetworkState.Error(context.getString(R.string.network_fail))
                }
            })
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingSubInsertState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingSubInsertState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }


    fun requestTrainingSubUpdate(trainingSubResponse: TrainingSubResponse, requestType: String) = viewModelScope.launch {
        trainingSubDetailUpdateState.value = NetworkState.Loading(true)
        try {
            val trainingSubDetail = trainingSubResponse
            when (requestType) {
                "rpe" -> handleRpeUpdate(trainingSubDetail)
                "health" -> handleHealthUpdate(trainingSubDetail)
                "url" -> handleUrlUpdate(trainingSubDetail)
            }
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingSubDetailUpdateState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingSubDetailUpdateState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    private suspend fun handleRpeUpdate(trainingSubDetail: TrainingSubResponse) {
        val rpeRequest = TrainingSubDetailRpeRequest(
            trainingSubDetail.exerciseId,
            trainingSubDetail.userId,
            trainingSubDetail.trainingTime,
            trainingSubDetail.trainingDate,
            trainingSubDetail.rpe
        )
        performUpdate(rpeRequest)
    }

    private suspend fun handleHealthUpdate(trainingSubDetail: TrainingSubResponse) {
        val healthRequest = TrainingSubDetailHealthRequest(
            trainingSubDetail.exerciseId,
            trainingSubDetail.userId,
            trainingSubDetail.trainingTime,
            trainingSubDetail.trainingDate,
            trainingSubDetail.healthData.energy_total,
            trainingSubDetail.healthData.speed_avg,
            trainingSubDetail.healthData.speed_max,
            trainingSubDetail.healthData.speed_min,
            trainingSubDetail.healthData.distance_total,
            trainingSubDetail.healthData.bpm_avg,
            trainingSubDetail.healthData.bpm_max,
            trainingSubDetail.healthData.bpm_min
        )
        performUpdate(healthRequest)
    }

    private suspend fun handleUrlUpdate(trainingSubDetail: TrainingSubResponse) {
        val urlRequest = TrainingSubDetailUrlRequest(
            trainingSubDetail.exerciseId,
            trainingSubDetail.userId,
            trainingSubDetail.trainingTime,
            trainingSubDetail.trainingDate,
            trainingSubDetail.url
        )
        performUpdate(urlRequest)
    }

    private suspend fun performUpdate(request: Any) {
        try {
            val response = when (request) {
                is TrainingSubDetailRpeRequest -> TrainingRepository.requestTrainingSubRpeUpdate(request)
                is TrainingSubDetailHealthRequest -> TrainingRepository.requestTrainingSubHealthUpdate(request)
                is TrainingSubDetailUrlRequest -> TrainingRepository.requestTrainingSubUrlUpdate(request)
                else -> throw IllegalArgumentException("Invalid request type")
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    trainingSubDetailUpdateState.value = NetworkState.Success(it)
                } ?: run {
                    trainingSubDetailUpdateState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                trainingSubDetailUpdateState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingSubDetailUpdateState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingSubDetailUpdateState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun getExerciseDetail(userId: String,date: String,exerciseId: String) = viewModelScope.launch {
        trainingDetailExerciseState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getExerciseDetail(userId,date,exerciseId)
            if(response.isSuccessful){
                response.body()?.let { trainingStatus ->
                    trainingDetailExerciseState.value = NetworkState.Success(trainingStatus)
                    trainingDetailExerciseStateSuceess.value = true
                } ?: run {
                    trainingDetailExerciseState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                    trainingDetailExerciseStateSuceess.value = false
                }
            } else {
                trainingDetailExerciseState.value = NetworkState.Error(context.getString(R.string.network_fail))
                trainingDetailExerciseStateSuceess.value = false
            }
        }
        catch (ex: Exception){
            handleException(ex)
            trainingDetailExerciseStateSuceess.value = false
        }
    }

    fun requestExerciseUpdate(trainingInfoResponseList: List<TrainingInfoResponse>) = viewModelScope.launch {
        trainingExerciseUpdateState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.requestExerciseUpdate(trainingInfoResponseList)
            val success = response.isSuccessful
            val message = response.message() // Use the response message

            if (success) {
                trainingExerciseUpdateState.value = NetworkState.Success(message)
            } else {
                trainingExerciseUpdateState.value = NetworkState.Error(message)
            }
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingExerciseUpdateState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingExerciseUpdateState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun getTssDataTime(userId: String,trainingDate: String) = viewModelScope.launch {
        trainingTssDataTimeState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getTssDataTime(userId,trainingDate)
            if(response.isSuccessful){
                response.body()?.let { trainingStatus ->
                    trainingTssDataTimeState.value = NetworkState.Success(trainingStatus)
                } ?: run {
                    trainingTssDataTimeState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                }
            } else {
                trainingTssDataTimeState.value = NetworkState.Error(context.getString(R.string.network_fail))
            }
        }
        catch (ex: Exception){
            when (ex) {
                is IOException -> trainingTssDataTimeState.value = NetworkState.Error(context.getString(R.string.network_error))
                else -> trainingTssDataTimeState.value =  NetworkState.Error(context.getString(R.string.network_data_change_error))
            }
        }
    }

    fun getTrainingConfirm(userId: String,date:String) = viewModelScope.launch {
        trainingConfirmState.value = NetworkState.Loading(true)
        try{
            val response = TrainingRepository.getTrainingConfirm(userId,date)
            if(response.isSuccessful){
                response.body()?.let { trainingConfirm ->
                    trainingConfirmState.value = NetworkState.Success(trainingConfirm)
                    trainingConfirmStateSuceess.value = true
                } ?: run {
                    trainingConfirmState.value = NetworkState.Error(context.getString(R.string.network_empty_date))
                    trainingConfirmStateSuceess.value = false
                }
            } else {
                trainingConfirmState.value = NetworkState.Error(context.getString(R.string.network_fail))
                trainingConfirmStateSuceess.value = false
            }
        }
        catch (ex: Exception){
            handleException(ex)
        }
    }


    private fun handleException(ex: Exception) {
        when (ex) {
            is IOException -> trainingSubDetailUpdateState.value = NetworkState.Error(context.getString(R.string.network_error))
            else -> trainingSubDetailUpdateState.value = NetworkState.Error(context.getString(R.string.network_data_change_error))
        }
    }

}