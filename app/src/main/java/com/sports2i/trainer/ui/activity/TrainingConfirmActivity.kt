package com.sports2i.trainer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExerciseGraph
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.TrainingConfirm
import com.sports2i.trainer.data.model.TrainingExercise
import com.sports2i.trainer.data.model.TrainingInfo
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingOverall
import com.sports2i.trainer.databinding.ActivityTrainingConfirmBinding
import com.sports2i.trainer.ui.adapter.groupmember.TrainingExerciseAdapter
import com.sports2i.trainer.ui.adapter.groupmember.TrainingExerciseGraphAdapter
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.CustomSpinnerAdapter
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.ui.widget.GraphTrainingOverall
import com.sports2i.trainer.utils.DateTimeUtil.formatDateSelection
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.TrainingOverallViewModel
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingConfirmActivity : BaseActivity<ActivityTrainingConfirmBinding>({ActivityTrainingConfirmBinding.inflate(it)}){

    private val groupViewModel: GroupViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()
    private val trainingOverallViewModel: TrainingOverallViewModel by viewModels()

    private var userList: MutableList<GroupUser> = mutableListOf()
    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()
    private var trainingExerciseList: MutableList<TrainingExercise> = mutableListOf()
    private var trainingInfoList: MutableList<TrainingInfoResponse> = mutableListOf()
    private var trainingOverallList:  MutableList<TrainingOverall> = mutableListOf()

    private lateinit var customGroupSpinner: CustomSpinner
    private lateinit var customUserSpinner: CustomSpinner

    private lateinit var dawnAdapter : TrainingExerciseAdapter
    private lateinit var morningAdapter : TrainingExerciseAdapter
    private lateinit var afternoonAdapter : TrainingExerciseAdapter
    private lateinit var dinnerAdapter : TrainingExerciseAdapter
    private lateinit var nightAdapter : TrainingExerciseAdapter

    private lateinit var exerciseGrapgAdapter : TrainingExerciseGraphAdapter

    private var dateSelectionView : DateSelectionView? = null

    var selectedGroup : GroupInfo? = null
    var selectedUserId : String? = null
    var selectedDateTime : String? = ""
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
        binding.root.post{
            setLiveData()
        }
        this@TrainingConfirmActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setLiveData(){

        dateSelectionView = DateSelectionView(this)
        binding.dateSelectionView.apply {
            addView(dateSelectionView)
        }
        dateSelectionView!!.setSelectedDateExternal(selectedDateTime!!)
        dateSelectionView!!.selectedDate.observe(this, ::handleSelectedDate)

    }
    private fun handleSelectedDate(selectedDate: String) {
        selectedDateTime = formatDateSelection(selectedDate)
        trainingViewModel.getTrainingConfirm(selectedUserId!!, selectedDateTime!!)
    }

    private fun setContent() {

        Preferences.init(this, Preferences.DB_USER_INFO)
        groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))

        selectedGroup = intent.getParcelableExtra("selectedGroup")
        selectedUserId = intent.getStringExtra("userId")
        selectedDateTime = intent.getStringExtra("dateTime")

        customGroupSpinner = binding.spinnerGroup
        customUserSpinner = binding.spinnerUser

//        groupViewModel.requestGroupUser(selectedGroup!!.groupId)
//        trainingViewModel.getTrainingConfirm(selectedUserId!!, selectedDateTime!!)

        // RecyclerView 및 어댑터 초기화
        val dawnRecyclerView = binding.rvPriviewDawnExercise
        val morningRecyclerView = binding.rvPriviewMorningExercise
        val afternoonRecyclerView = binding.rvPriviewAfternoonExercise
        val dinnerRecyclerView = binding.rvPriviewDinnerExercise
        val nightRecyclerView = binding.rvPriviewNightExercise
        val exerciseGraphRecyclerView = binding.rvExerciseGraph

        dawnAdapter = TrainingExerciseAdapter(this@TrainingConfirmActivity, mutableListOf())
        morningAdapter = TrainingExerciseAdapter(this@TrainingConfirmActivity, mutableListOf())
        afternoonAdapter = TrainingExerciseAdapter(this@TrainingConfirmActivity, mutableListOf())
        dinnerAdapter = TrainingExerciseAdapter(this@TrainingConfirmActivity, mutableListOf())
        nightAdapter = TrainingExerciseAdapter(this@TrainingConfirmActivity, mutableListOf())

        exerciseGrapgAdapter = TrainingExerciseGraphAdapter(this@TrainingConfirmActivity, mutableListOf())

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

        customGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGroup = parent?.getItemAtPosition(position) as GroupInfo
                groupId= selectedGroup!!.groupId
                groupViewModel.requestGroupUser(groupId)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle Nothing Selected
            }
        }

        customUserSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroupUser = parent?.getItemAtPosition(position) as GroupUser
                selectedUserId = selectedGroupUser.userId
                trainingViewModel.getTrainingConfirm(selectedUserId!!, selectedDateTime!!)
            }
        }

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

                val intent = Intent(this@TrainingConfirmActivity, TrainingExerciseDetailActivity::class.java)
                intent.putExtra("selectedGroup", selectedGroup)
                intent.putExtra("userId", selectedUserId)
                intent.putExtra("trainingTime", trainingExercise.trainingTime)
                intent.putExtra("exerciseName", exercise.exerciseName)
                intent.putExtra("exerciseId", exercise.exerciseId)
                intent.putExtra("dateTime", selectedDateTime)

                startActivity(intent)

        } else if (position >= 0 && position < exerciseList.size) {
            val exercise = exerciseList[position]

                val intent = Intent(this@TrainingConfirmActivity, TrainingExerciseDetailActivity::class.java)
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
            binding.refreshLayout?.isRefreshing = false
        }
    }

    private fun observeGroupInfoState() = observeNetworkState(groupViewModel.groupInfoState){
        handlerGroupInfoSuccess(it.data.data)
        if (selectedGroup != null) {
            val groupAdapter = customGroupSpinner.adapter as CustomSpinnerAdapter<GroupInfo>
            val groupIndex = groupAdapter.getPosition(selectedGroup)
            binding.spinnerGroup.setSelection(groupIndex)
        }
    }
    private fun observeGroupUserState() = observeNetworkState(groupViewModel.groupUserState){
        handlerGroupUserSuccess(it.data.data)
        if(selectedUserId != null){
            val groupUserAdapter = customUserSpinner.adapter as CustomSpinnerAdapter<GroupUser>
            userList.forEach {
                if(it.userId == selectedUserId){
                    val userIndex = groupUserAdapter.getPosition(it)
                    binding.spinnerUser.setSelection(userIndex)
                }
            }
        }
    }
    private fun observeTrainingConfirmState() = observeNetworkState(trainingViewModel.trainingConfirmState){
        handlerTrainingConfirmSuccess(it.data.data)
    }

    private inline fun <reified T : Any> observeNetworkState(stateLiveData: LiveData<NetworkState<T>>, crossinline onSuccess: (NetworkState.Success<T>) -> Unit) {
        stateLiveData.observe(this) {
            when (it) {
                is NetworkState.Success -> {
                    onSuccess(it)
//                    Handler(mainLooper).postDelayed({ handlerLoading(false) }, 600)
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

    private fun handlerGroupInfoSuccess(groupInfos: MutableList<GroupInfo>) {
        groupInfoList = groupInfos
        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort
                if (isDropDown) {
                    textView.setTextColor(this@TrainingConfirmActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@TrainingConfirmActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        customGroupSpinner.setAdapterData(groupInfoList, itemBinder)
    }

    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
        userList = groupUsers

        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupUser> {
            override fun bindItem(view: View, item: GroupUser, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.userName
                if (isDropDown) {
                    textView.setTextColor(this@TrainingConfirmActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@TrainingConfirmActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        customUserSpinner.setAdapterData(userList, itemBinder)
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
