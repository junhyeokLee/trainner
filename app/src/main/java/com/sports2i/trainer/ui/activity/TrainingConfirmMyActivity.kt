package com.sports2i.trainer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.data.model.ExerciseGraph
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.TrainingConfirm
import com.sports2i.trainer.data.model.TrainingExercise
import com.sports2i.trainer.data.model.TrainingInfo
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingOverall
import com.sports2i.trainer.databinding.ActivityTrainingConfirmMyBinding
import com.sports2i.trainer.ui.adapter.groupmember.TrainingExerciseAdapter
import com.sports2i.trainer.ui.adapter.groupmember.TrainingExerciseGraphAdapter
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.ui.widget.GraphTrainingOverall
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.TrainingOverallViewModel
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingConfirmMyActivity : BaseActivity<ActivityTrainingConfirmMyBinding>({ActivityTrainingConfirmMyBinding.inflate(it)}){

    private val groupViewModel: GroupViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()
    private val trainingOverallViewModel: TrainingOverallViewModel by viewModels()

    private var userList: MutableList<GroupUser> = mutableListOf()
    private var trainingIndiStatusList: MutableList<TrainingExercise> = mutableListOf()
    private var trainingExerciseList: MutableList<TrainingExercise> = mutableListOf()
    private var trainingInfoList: MutableList<TrainingInfoResponse> = mutableListOf()
    private var trainingOverallList:  MutableList<TrainingOverall> = mutableListOf()

    private lateinit var dawnAdapter : TrainingExerciseAdapter
    private lateinit var morningAdapter : TrainingExerciseAdapter
    private lateinit var afternoonAdapter : TrainingExerciseAdapter
    private lateinit var dinnerAdapter : TrainingExerciseAdapter
    private lateinit var nightAdapter : TrainingExerciseAdapter

    private lateinit var exerciseGrapgAdapter : TrainingExerciseGraphAdapter

    private var dateSelectionView : DateSelectionView? = null

    var selectedGroup : GroupInfo? = null
    var selectedUserId : String? = null
    var selectedDateTime : String? = null
    private var groupId: String = ""

    private val networkStateObservers = listOf(
        ::observeGroupInfoState,
        ::observeGroupUserState,
        ::observeTrainingConfirmState
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        setContent()
        setFunction()
        networkStatus()
        clickBack()
        binding.root.post{ setLiveData() }
        this@TrainingConfirmMyActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setLiveData(){
        dateSelectionView = DateSelectionView(this)
        binding.dateSelectionView.apply { addView(dateSelectionView) }
        dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
        dateSelectionView!!.selectedDate.observe(this, ::handleSelectedDate)
    }
    private fun handleSelectedDate(selectedDate: String) {
        selectedDateTime = DateTimeUtil.formatDateSelection(selectedDate)
        trainingViewModel.getTrainingConfirm(selectedUserId!!, selectedDateTime!!)
    }

    private fun setContent() {

        Preferences.init(this, Preferences.DB_USER_INFO)

        groupViewModel.getSelectedGroup(Preferences.string(Preferences.KEY_GROUP_ID))
        groupViewModel.requestGroupUser(Preferences.string(Preferences.KEY_GROUP_ID))

        selectedGroup = intent.getParcelableExtra("selectedGroup")
        selectedUserId = intent.getStringExtra("userId")
        selectedDateTime = intent.getStringExtra("dateTime")
        groupId = Preferences.string(Preferences.KEY_GROUP_ID)

        // RecyclerView 및 어댑터 초기화
        val dawnRecyclerView = binding.rvPriviewDawnExercise
        val morningRecyclerView = binding.rvPriviewMorningExercise
        val afternoonRecyclerView = binding.rvPriviewAfternoonExercise
        val dinnerRecyclerView = binding.rvPriviewDinnerExercise
        val nightRecyclerView = binding.rvPriviewNightExercise
        val exerciseGraphRecyclerView = binding.rvExerciseGraph

        exerciseGrapgAdapter = TrainingExerciseGraphAdapter(this@TrainingConfirmMyActivity, mutableListOf())

        dawnAdapter = TrainingExerciseAdapter(this@TrainingConfirmMyActivity, mutableListOf())
        morningAdapter = TrainingExerciseAdapter(this@TrainingConfirmMyActivity, mutableListOf())
        afternoonAdapter = TrainingExerciseAdapter(this@TrainingConfirmMyActivity, mutableListOf())
        dinnerAdapter = TrainingExerciseAdapter(this@TrainingConfirmMyActivity, mutableListOf())
        nightAdapter = TrainingExerciseAdapter(this@TrainingConfirmMyActivity, mutableListOf())

        dawnRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        morningRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        afternoonRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        dinnerRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        nightRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        exerciseGraphRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        dawnRecyclerView.adapter = dawnAdapter
        morningRecyclerView.adapter = morningAdapter
        afternoonRecyclerView.adapter = afternoonAdapter
        dinnerRecyclerView.adapter = dinnerAdapter
        nightRecyclerView.adapter = nightAdapter

        exerciseGraphRecyclerView.adapter = exerciseGrapgAdapter

    }


    private fun setFunction() {
        refreshing()

        dawnAdapter.mListener = object : TrainingExerciseAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, trainingExercise: TrainingExercise) {
                moveToExerciseDetail(position,trainingExercise)
            }
        }
        morningAdapter.mListener = object : TrainingExerciseAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, trainingExercise: TrainingExercise) {
                moveToExerciseDetail(position,trainingExercise)
            }
        }
        afternoonAdapter.mListener = object : TrainingExerciseAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, trainingExercise: TrainingExercise) {
                moveToExerciseDetail(position,trainingExercise)
            }
        }
        dinnerAdapter.mListener = object : TrainingExerciseAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, trainingExercise: TrainingExercise) {
                moveToExerciseDetail(position,trainingExercise)
            }
        }
        nightAdapter.mListener = object : TrainingExerciseAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, trainingExercise: TrainingExercise) {
                moveToExerciseDetail(position,trainingExercise)
            }
        }
    }

    private fun moveToExerciseDetail(position: Int, trainingExercise: TrainingExercise) {
        val exerciseList = trainingExercise.exerciseList

        if (exerciseList.size == 1) {
            val exercise = exerciseList[0]
                val intent = Intent(this@TrainingConfirmMyActivity, TrainingExerciseDetailMyActivity::class.java)
                intent.putExtra("selectedGroup", selectedGroup)
                intent.putExtra("userId", selectedUserId)
                intent.putExtra("trainingTime", trainingExercise.trainingTime)
                intent.putExtra("exerciseName", exercise.exerciseName)
                intent.putExtra("exerciseId", exercise.exerciseId)
                intent.putExtra("dateTime", selectedDateTime)

                startActivity(intent)
        } else if (position >= 0 && position < exerciseList.size) {
            val exercise = exerciseList[position]
                val intent = Intent(this@TrainingConfirmMyActivity, TrainingExerciseDetailMyActivity::class.java)
                intent.putExtra("selectedGroup", selectedGroup)
                intent.putExtra("userId", selectedUserId)
                intent.putExtra("trainingTime", trainingExercise.trainingTime)
                intent.putExtra("exerciseName", exercise.exerciseName)
                intent.putExtra("exerciseId", exercise.exerciseId)
                intent.putExtra("dateTime", selectedDateTime)

                startActivity(intent)
        } else {
            // 유효하지 않은 position에 대한 예외처리 또는 메시지 출력
            Log.e("TAG", "Invalid position: $position")
        }
    }

    private fun updateUIWithSelectedList(selectedList: List<TrainingExercise>) {
        // Clear all adapters
        clearAdapters()

        for (trainingExercise in selectedList) {
            val trainingTime = trainingExercise.trainingTime
            // 현재 exercisePreset에서 exerciseId로 그룹화된 exerciseList를 가져옵니다.
            val exerciseIdToExerciseListMap = trainingExercise.exerciseList.groupBy { it.exerciseId }

            when (trainingTime) {
                "T1" -> addDataToAdapter(trainingExercise, dawnAdapter, exerciseIdToExerciseListMap)
                "T2" -> addDataToAdapter(trainingExercise, morningAdapter, exerciseIdToExerciseListMap)
                "T3" -> addDataToAdapter(trainingExercise, afternoonAdapter, exerciseIdToExerciseListMap)
                "T4" -> addDataToAdapter(trainingExercise, dinnerAdapter, exerciseIdToExerciseListMap)
                "T5" -> addDataToAdapter(trainingExercise, nightAdapter, exerciseIdToExerciseListMap)
                else -> {}
            }
        }
        // Update visibility based on item count
        updateVisibility()
    }

    private fun addDataToAdapter(
        trainingExercise: TrainingExercise,
        adapter: TrainingExerciseAdapter,
        exerciseIdToExerciseListMap: Map<String, List<TrainingInfo.ExerciseList>>
    ) {
        for ((_, groupedExerciseList) in exerciseIdToExerciseListMap) {
            val exercisePresetWithGroupedExerciseList = trainingExercise.copy(exerciseList = groupedExerciseList.toMutableList())
            adapter.addData(exercisePresetWithGroupedExerciseList)
        }
    }
    private fun clearAdapters() {
        dawnAdapter.clearData()
        morningAdapter.clearData()
        afternoonAdapter.clearData()
        dinnerAdapter.clearData()
        nightAdapter.clearData()
    }
    private fun updateVisibility() {
        binding.layoutPriviewDawnExercise.visibility = if (dawnAdapter.itemCount > 0) View.VISIBLE else View.GONE
        binding.layoutPriviewMorningExercise.visibility = if (morningAdapter.itemCount > 0) View.VISIBLE else View.GONE
        binding.layoutPriviewAfternoonExercise.visibility = if (afternoonAdapter.itemCount > 0) View.VISIBLE else View.GONE
        binding.layoutPriviewDinnerExercise.visibility = if (dinnerAdapter.itemCount > 0) View.VISIBLE else View.GONE
        binding.layoutPriviewNightExercise.visibility = if (nightAdapter.itemCount > 0) View.VISIBLE else View.GONE
    }
    private fun refreshing() {
        binding.refreshLayout?.setOnRefreshListener {
            handleSelectedDate(dateSelectionView!!.getSelectedDate())
            binding.refreshLayout?.isRefreshing = false
        }
    }

    private fun observeGroupInfoState() = observeNetworkState(groupViewModel.selectedGroupInfoState){
        handlerSelectedGroupInfoSuccess(it.data.data)
    }
    private fun observeGroupUserState() = observeNetworkState(groupViewModel.groupUserState){
        handlerGroupUserSuccess(it.data.data)
    }
    private fun observeTrainingConfirmState() = observeNetworkState(trainingViewModel.trainingConfirmState){
        handlerTrainingConfirmSuccess(it.data.data)
    }

    private inline fun <reified T : Any> observeNetworkState(stateLiveData: LiveData<NetworkState<T>>, crossinline onSuccess: (NetworkState.Success<T>) -> Unit) {
        stateLiveData.observe(this) {
            when (it) {
                is NetworkState.Success -> {
                    onSuccess(it)
                    Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                }
                is NetworkState.Error -> {
                    handlerError(it.message)
                }
                is NetworkState.Loading -> {
                    handlerLoading(it.isLoading)
                }
            }
        }
    }

    private fun networkStatus() {
        trainingViewModel.trainingConfirmStateSuceess.observe(this) {
            if(it) {
                Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                binding.tvDetailGoalEmpty.visibility = View.GONE
                binding.layoutPreview.visibility = View.VISIBLE
                binding.tvGraph1Empty.visibility = View.GONE
                binding.graphLayout1.visibility = View.VISIBLE
                binding.tvGraph2Empty.visibility = View.GONE
                binding.rvExerciseGraph.visibility = View.VISIBLE
            }
            else{
                Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                binding.graphLayout1.removeAllViews()
                binding.rvExerciseGraph.removeAllViews()
                binding.tvDetailGoalEmpty.visibility = View.VISIBLE
                binding.layoutPreview.visibility = View.GONE
                binding.tvGraph1Empty.visibility = View.VISIBLE
                binding.graphLayout1.visibility = View.GONE
                binding.tvGraph2Empty.visibility = View.VISIBLE
                binding.rvExerciseGraph.visibility = View.GONE
            }
        }

        networkStateObservers.forEach { observer ->
            observer.invoke()
        }
    }


    private fun handlerSelectedGroupInfoSuccess(groupInfos: GroupInfo) {
        selectedGroup = groupInfos
    }

    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
        userList = groupUsers
    }

    private fun handlerTrainingIndiStatusSuccess(trainingIndiStatus: MutableList<TrainingExercise>) {
        trainingIndiStatusList.clear()
        trainingIndiStatusList.addAll(trainingIndiStatus)
        updateUIWithSelectedList(trainingIndiStatusList)
    }

    private fun handlerTrainingConfirmSuccess(trainingConfirm: MutableList<TrainingConfirm>) {

        trainingExerciseList.clear()
        trainingOverallList.clear()
        trainingInfoList.clear()

        for(i in 0 until trainingConfirm.size){
            // TrainingExercise
            trainingExerciseList.addAll(trainingConfirm[i].goal)
            trainingOverallList.addAll(trainingConfirm[i].trainingoverall)
            trainingInfoList.addAll(trainingConfirm[i].training)
        }

        // exerciseList
        updateUIWithSelectedList(trainingExerciseList)

        // trainingOverallList
        val trainingOveralGraphMap = extractTrainingOverallGraph(trainingOverallList)
        val graphLayout1 = binding.graphLayout1

        if(trainingOveralGraphMap.isNullOrEmpty()){
            graphLayout1.removeAllViews()
            binding.tvGraph1Empty.visibility = View.VISIBLE
            binding.graphLayout1.visibility = View.GONE
        }
        else{
            binding.tvGraph1Empty.visibility = View.GONE
            binding.graphLayout1.visibility = View.VISIBLE
            graphLayout1.removeAllViews()
            graphLayout1.addView(GraphTrainingOverall(this, 1,"date" ,trainingOveralGraphMap))
        }

        // trainingInfoList
        // 그룹화하기 전에 exerciseId로 중복된 항목들의 totalAchieveRate를 리스트로 만들기
        val groupedExerciseList = trainingInfoList.groupBy { it.exerciseName }
            .map { (exerciseName, exerciseList) ->
                ExerciseGraph(
                    exerciseName = exerciseName!!,
                    trainingDates = exerciseList.mapNotNull { it.trainingDate },
                    exerciseAchieveRate = exerciseList.mapNotNull { it.exerciseAchieveRate?.toInt() ?: 0 }
                )
            }
        // 어댑터에 그룹화된 아이템과 totalAchieveRateList 전달
        exerciseGrapgAdapter.addListData(groupedExerciseList)
    }

    private fun extractTrainingOverallGraph(
        trainingOverallGraph: MutableList<TrainingOverall>?
    ): List<Graph> {
        return trainingOverallGraph?.map {
            val userName = it.userName
            val trainingDate = it.trainingDate
            val value = it.performanceIndex?.toInt() ?: 0
            Graph(userName = userName, trainingDate = trainingDate, value = value)
        } ?: emptyList()
    }

    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun handlerLoading(isLoading: Boolean) {
        if(_binding != null) {
            if (isLoading) {
                binding.llProgressBar.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.llProgressBar.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
    }


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // "뒤로 가기" 버튼 처리
                finish()
                back()
        }
    }
    private fun clickBack() {
        binding.layoutBack.setOnClickListener {
                finish()
                back()
        }
    }
}
