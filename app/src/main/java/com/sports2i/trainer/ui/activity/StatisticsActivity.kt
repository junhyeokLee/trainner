package com.sports2i.trainer.ui.activity

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.TrainingOverallGraphItem
import com.sports2i.trainer.data.model.TrainingSubStatisticsGraphItem
import com.sports2i.trainer.data.model.TrainingTssDataTime
import com.sports2i.trainer.databinding.ActivityStatisticsBinding
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.ui.widget.GraphTrainingOverall
import com.sports2i.trainer.ui.widget.GraphTrainingStatistics
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.TrainingOverallViewModel
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsActivity: BaseActivity<ActivityStatisticsBinding>({ ActivityStatisticsBinding.inflate(it)}) {

    private var AUTHORITY = "" // 권한
    private var GROUP_ID = "" // 그룹 아이디
    private var KEY_ORGANIZATION_ID = "" // 조직 아이디

    private val groupViewModel: GroupViewModel by viewModels()
    private val trainingOverallViewModel: TrainingOverallViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()

    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()
    private var trainingOverallStatusList: MutableList<TrainingOverallGraphItem.TrainingOverallGraph> = mutableListOf()
    private var trainingStatistics : TrainingSubStatisticsGraphItem.TrainingSubStatisticsGraph = TrainingSubStatisticsGraphItem.TrainingSubStatisticsGraph()
    private var tssDataTime = TrainingTssDataTime()

    private var selectedGroup : GroupInfo? = null
    private var selectedDateTime: String? = ""
    private var selectedUserId: String? = ""

    private lateinit var groupCustomSpinner: CustomSpinner
    private lateinit var userCustomSpinner: CustomSpinner

    private var userList: MutableList<GroupUser> = mutableListOf()

    private var injuryWeekList: MutableList< TrainingOverallGraphItem> = mutableListOf()
    private var injuryMonthList: MutableList< TrainingOverallGraphItem> = mutableListOf()

    private var dateSelectionView : DateSelectionView? = null

    // 네트워크 상태 관찰자를 합쳐서 로딩 상태를 처리.
    val networkStateObservers = listOf(
        ::observeGroupInfo,
        ::observeSelectGroupInfo,
        ::observeGroupUser,
        ::observeStatisticsSleep,
        ::observeTrainingSubStatistics,
        ::observeTssDataTime
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setFunction()
        observe()
        clickBack()
        binding.root.post { setLiveData() }
        this@StatisticsActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)
        AUTHORITY = Preferences.string(Preferences.KEY_AUTHORITY)
        KEY_ORGANIZATION_ID = Preferences.string(Preferences.KEY_ORGANIZATION_ID)

        selectedGroup = intent.getParcelableExtra("selectedGroup")
        selectedDateTime = intent.getStringExtra("dateTime")
        selectedUserId = intent.getStringExtra("userId")

        if (checkTraineeAuthority()) {
            GROUP_ID = Preferences.string(Preferences.KEY_GROUP_ID)
            selectedUserId = Preferences.string(Preferences.KEY_USER_ID)
            groupViewModel.getSelectedGroup(GROUP_ID)

        } else {
            groupViewModel.requestGroupInfo(KEY_ORGANIZATION_ID)
            setGroupCustomSpinnerListener()

        }

        with(binding) {
            spinnerGroup.visibility = if (checkTraineeAuthority()) View.GONE else View.VISIBLE
            spinnerUser.visibility = if (checkTraineeAuthority()) View.GONE else View.VISIBLE
        }
    }
    private fun setLiveData() {

        dateSelectionView = DateSelectionView(this)
        binding.dateSelectionView.apply {
            addView(dateSelectionView)
        }
        dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
        dateSelectionView!!.selectedDate.observe(this, ::handleSelectedDate)
    }

    private fun handleSelectedDate(selectedDate: String) {
        selectedDateTime = DateTimeUtil.formatDateSelection(selectedDate)
        trainingOverallViewModel.requestTrainingStatistics(KEY_ORGANIZATION_ID, GROUP_ID, "training", selectedDateTime!!)
        trainingOverallViewModel.requestTssData(selectedUserId!!, selectedDateTime!!)
        trainingViewModel.getTssDataTime(selectedUserId!!, selectedDateTime!!)
    }

    private fun setFunction() {
        refreshing()
    }

    private fun setGroupCustomSpinnerListener() {
        groupCustomSpinner = binding.spinnerGroup // CustomSpinner 초기화
        groupCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGroup = parent?.getItemAtPosition(position) as GroupInfo
                GROUP_ID = selectedGroup!!.groupId
//                groupViewModel.getSelectedGroup(GROUP_ID)
                groupViewModel.requestGroupUser(GROUP_ID)
                setUserCustomSpinnerListener()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setUserCustomSpinnerListener() {
        userCustomSpinner = binding.spinnerUser
        userCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroupUser = parent?.getItemAtPosition(position) as GroupUser
                selectedUserId = selectedGroupUser.userId

                trainingOverallViewModel.requestTssData(selectedUserId!!, selectedDateTime!!)
                trainingOverallViewModel.requestTrainingStatistics(KEY_ORGANIZATION_ID, GROUP_ID, "training", selectedDateTime!!)
                trainingViewModel.getTssDataTime(selectedUserId!!, selectedDateTime!!)
            }
        }
    }

    private fun observeGroupInfo() = observeNetworkState(
        groupViewModel.groupInfoState,{
            handlerGroupInfoSuccess(it.data.data)
        }, { Log.e("error", it.message) })
    private fun observeSelectGroupInfo() = observeNetworkState(
        groupViewModel.selectedGroupInfoState,{handlerSelectedGroupInfoSuccess(it.data.data)},
        { Log.e("error", it.message) })
    private fun observeGroupUser() = observeNetworkState(
        groupViewModel.groupUserState, {
            handlerGroupUserSuccess(it.data.data)},
        { Log.e("error", it.message)})
    private fun observeStatisticsSleep() = observeNetworkState(
        trainingOverallViewModel.trainingOverallState, { handleStatisticsSleepSuccess(it.data.data) },
        { Log.e("error", it.message)
            binding.tvSleepGraphEmpty.visibility = View.VISIBLE
            binding.sleepGraph.visibility = View.GONE
        })
    private fun observeTrainingSubStatistics() = observeNetworkState(
        trainingOverallViewModel.tssDataState , { handleTrainingSubStatisticsSuccess(it.data.data) },
        { Log.e("error", it.message)
            binding.tvGraph2Empty.visibility = View.VISIBLE
            binding.injuryGraph.visibility = View.GONE
            binding.injuryAvgGraph.visibility = View.GONE
        })

    private fun observeTssDataTime() = observeNetworkState(
        trainingViewModel.trainingTssDataTimeState, { handleTssDataTimeSuccess(it.data.data!!) },
        { Log.e("error", it.message) })


    private fun observe() {
        networkStateObservers.forEach { observer ->
            observer.invoke()
        }
    }


    fun updateSleepGraphList(selectedList: List<Graph>) {
        // 그래프 업데이트
        binding.sleepGraph.apply {
            removeAllViews()
            addView(GraphTrainingOverall(this@StatisticsActivity, 2, "date", selectedList))
        }
    }


    fun updateSleepGraphListMap(selectedMap: Map<String, List<Graph>>?) {
        // 선택된 맵이 null이 아니고, 모든 항목의 value와 value2의 값이 0이 아닌 경우에는 데이터를 업데이트.
        if (selectedMap != null && selectedMap.values.flatten().all { it.value == 0 && it.value2 == 0 }) {
            // 모든 항목의 value와 value2의 값이 0인 경우에만 empty 상태로 처리.
            binding.tvSleepGraphEmpty.visibility = View.VISIBLE
            binding.sleepGraph.visibility = View.GONE
        } else {
            // 선택된 맵이 null이거나, 하나 이상의 항목의 value와 value2의 값이 0이 아닌 경우에는 데이터를 업데이트.
            if (selectedMap != null) {
                updateSleepGraphList(selectedMap.values.flatten())
                binding.tvSleepGraphEmpty.visibility = View.GONE
                binding.sleepGraph.visibility = View.VISIBLE
            }
        }
    }

    private fun setTrainingOverallSlpeeAdapter(trainingOverallStatusList: List<TrainingOverallGraphItem.TrainingOverallGraph>) {
        val weekCheckBox = binding.checkboxSleepWeeks
        val monthCheckBox = binding.checkboxSleepMonth

        weekCheckBox.isChecked = true
        monthCheckBox.isChecked = false

        val trainingOverallWeekMap = mutableMapOf<String, List<Graph>>()
        val trainingOverallMonthMap = mutableMapOf<String, List<Graph>>()

        for (item in trainingOverallStatusList) {
            val itemName = item.trainingOverallItemName
            trainingOverallWeekMap[itemName] = extractSleepGraphList(item, "week")
            trainingOverallMonthMap[itemName] = extractSleepGraphList(item, "month")
        }

        updateSleepGraphListMap(trainingOverallWeekMap)
        weekCheckBox.isClickable = false


        weekCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                weekCheckBox.isClickable = false
                monthCheckBox.isChecked = false
                monthCheckBox.isClickable = true

                updateSleepGraphListMap(trainingOverallWeekMap)
            }
        }

        monthCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                monthCheckBox.isClickable = false
                weekCheckBox.isChecked = false
                weekCheckBox.isClickable = true

                updateSleepGraphListMap(trainingOverallMonthMap)

            }
        }
    }

    private fun extractSleepGraphList(trainingOverallGraph: TrainingOverallGraphItem.TrainingOverallGraph, period: String): List<Graph> {
        return when (period) {
            "week" -> extractSleepGraphList(trainingOverallGraph.trainingOverallData.week, period)
            "month" -> extractSleepGraphList(trainingOverallGraph.trainingOverallData.month, period)
            else -> emptyList()
        }
    }

    private fun extractSleepGraphList(dataList: List<TrainingOverallGraphItem>, period: String): List<Graph> {
        return dataList.map { itemData ->
            createSleepGraphFromItemData(itemData, period)
        }
    }

    private fun createSleepGraphFromItemData(itemData: TrainingOverallGraphItem, period: String): Graph {
        val userName = itemData.userName
        val trainingDate = itemData.trainingDate
        val value = when (period) {
            "week" -> ((itemData.sleepDuration!! / 24) * 100).toInt() // 24시간을 100%로 보고 계산
            "month" -> ((itemData.sleepDuration!! / 24) * 100).toInt() // 24시간을 100%로 보고 계산
            else -> null
        }
        val value2 = when (period) {
            "week" -> itemData.sleepIndex?.toInt()
            "month" -> itemData.sleepIndex?.toInt()
            else -> null
        }

        return Graph(userName = userName, trainingDate = trainingDate, value = value ?: 0, value2 = value2 ?: 0)
    }


    private fun setTrainingStatisticsAdapter(trainingStatisticsStatus: TrainingSubStatisticsGraphItem.TrainingSubStatisticsGraph,injuryWeekList: List<TrainingOverallGraphItem>,injuryMonthList: List<TrainingOverallGraphItem>) {
        val weekCheckBox = binding.checkboxInjuryWeeks
        val monthCheckBox = binding.checkboxInjuryMonth

        weekCheckBox.isChecked = true
        monthCheckBox.isChecked = false

        // 훈련부하,평균부하 데이터
        var trainingStatisticsWeekList : MutableList<TrainingSubStatisticsGraphItem> = mutableListOf()
        var trainingStatisticsMonthList : MutableList<TrainingSubStatisticsGraphItem> = mutableListOf()

        var trainingStatisticsWeek = trainingStatisticsStatus.week
        var trainingStatisticsMonth = trainingStatisticsStatus.month

        trainingStatisticsWeekList.addAll(trainingStatisticsWeek)
        trainingStatisticsMonthList.addAll(trainingStatisticsMonth)

        val weekGraph =  extractStatisticsGraphList(trainingStatisticsWeekList)
        val monthGraph = extractStatisticsGraphList(trainingStatisticsMonthList)

        updateStatisticsGraphListMap(weekGraph)

        // 부상위험도 데이터
        val trainingStatisticsInjuryWeekList : MutableList<TrainingOverallGraphItem> = mutableListOf()
        val trainingStatisticsInjuryMonthList : MutableList<TrainingOverallGraphItem> = mutableListOf()

        val trainingStatisticsInjuryWeek = injuryWeekList
        val trainingStatisticsInjuryMonth = injuryMonthList

        trainingStatisticsInjuryWeekList.addAll(trainingStatisticsInjuryWeek)
        trainingStatisticsInjuryMonthList.addAll(trainingStatisticsInjuryMonth)

        val weekInjuryGraph =  extractInjuryStatisticsGraphList(trainingStatisticsInjuryWeekList,"week")
        val monthInjuryGraph = extractInjuryStatisticsGraphList(trainingStatisticsInjuryMonthList,"month")

        updateStatisticsInjuryGraphListMap(weekInjuryGraph)


        weekCheckBox.isClickable = false

        weekCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                weekCheckBox.isClickable = false
                monthCheckBox.isChecked = false
                monthCheckBox.isClickable = true

                updateStatisticsInjuryGraphListMap(weekInjuryGraph)
                updateStatisticsGraphListMap(weekGraph)
            }
        }

        monthCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                monthCheckBox.isClickable = false
                weekCheckBox.isChecked = false
                weekCheckBox.isClickable = true

                updateStatisticsInjuryGraphListMap(monthInjuryGraph)
                updateStatisticsGraphListMap(monthGraph)
            }
        }
    }

    fun updateStatisticsGraphList(selectedList: List<Graph.StatisticsGraph>) {
        // 그래프 업데이트
        binding.injuryAvgGraph.apply {
            removeAllViews()
            addView(GraphTrainingStatistics(this@StatisticsActivity, 4, "date", selectedList))
        }
    }

    fun updateStatisticsInjuryGraphList(selectedList: List<Graph.StatisticsGraph>) {
        // 그래프 업데이트
        binding.injuryGraph.apply {
            removeAllViews()
            addView(GraphTrainingStatistics(this@StatisticsActivity, 5, "date", selectedList))
        }
    }

    fun updateStatisticsGraphListMap(selectedMap: List<Graph.StatisticsGraph>?) {
        // 선택된 맵이 null이 아니고, 하나 이상의 항목의 value1, value2, value3가 0인 경우에만 empty로 처리.
        if (selectedMap != null && selectedMap.any { it.value != 0 || it.value2 != 0 || it.value3 != 0 }) {
                updateStatisticsGraphList(selectedMap)
                binding.tvGraph2Empty.visibility = View.GONE
                binding.injuryAvgGraph.visibility = View.VISIBLE
        } else {
            // 모든 항목의 value1, value2, value3가 0인 경우에만 empty로 처리.
            binding.tvGraph2Empty.visibility = View.VISIBLE
            binding.injuryAvgGraph.visibility = View.GONE
            binding.injuryAvgGraph.removeAllViews()
        }
    }
    fun updateStatisticsInjuryGraphListMap(selectedMap:  List<Graph.StatisticsGraph>?) {
        // 선택된 맵이 null이 아니고, 모든 항목의 value4가 0인 경우에만 빈 상태로 처리.
        if (selectedMap != null && selectedMap.any { it.value4 != 0 }) {
            updateStatisticsInjuryGraphList(selectedMap)
            binding.injuryGraph.visibility = View.VISIBLE
        } else {
            binding.injuryGraph.visibility = View.GONE
            binding.injuryGraph.removeAllViews()
        }
    }

    private fun extractStatisticsGraphList(dataList: List<TrainingSubStatisticsGraphItem>): List<Graph.StatisticsGraph> {
        return dataList.map { itemData ->
            createStatisticsGraphFromItemData(itemData)
        }
    }
    private fun createStatisticsGraphFromItemData(itemData: TrainingSubStatisticsGraphItem): Graph.StatisticsGraph {
        val trainingDate = itemData.trainingDate
        val value = itemData.tss
        val value2 = itemData.tss7Avg
        val value3 = itemData.tss28Avg
        return Graph.StatisticsGraph(trainingDate = trainingDate, value = value ?: 0, value2 = value2 ?: 0, value3 = value3 ?: 0)
    }


    // 부상위험도 데이터
    private fun extractInjuryStatisticsGraphList(dataList: List<TrainingOverallGraphItem>,tabValue:String): List<Graph.StatisticsGraph> {
        return dataList.map { itemData ->
            createInjuryStatisticsGraphFromItemData(itemData,tabValue)
        }
    }
    private fun createInjuryStatisticsGraphFromItemData(itemData: TrainingOverallGraphItem,tabValue:String): Graph.StatisticsGraph {
        val trainingDate = itemData.trainingDate
        if(tabValue.equals("week")) {
            val value4 = (itemData.injuryWeekAvg!! * 1000).toInt()
            return Graph.StatisticsGraph(trainingDate = trainingDate, value4 = value4 ?: 0)
        }else if(tabValue.equals("month")) {
            val value4 = (itemData.injuryMonthAvg!! * 1000).toInt()
            return Graph.StatisticsGraph(trainingDate = trainingDate, value4 = value4 ?: 0)
        }
//        val value4 = (itemData.injuryIndex!! * 100).toInt()
//        return Graph.StatisticsGraph(trainingDate = trainingDate, value4 = value4 ?: 0)
        return Graph.StatisticsGraph(trainingDate = trainingDate, value4 = 0)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun refreshing() {
        binding.refreshLayout?.setOnRefreshListener {
            handleSelectedDate(dateSelectionView!!.getSelectedDate())
            binding.refreshLayout?.isRefreshing = false
        }
    }

    private fun handlerSelectedGroupInfoSuccess(groupInfos: GroupInfo) {
        selectedGroup = groupInfos

        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort

                if (isDropDown) textView.setTextColor(this@StatisticsActivity.resources.getColor(android.R.color.black))
                else textView.setTextColor(this@StatisticsActivity.resources.getColor(android.R.color.white))

            }
        }
        var groupInfoList = mutableListOf<GroupInfo>()
        groupInfoList.add(selectedGroup!!)
        groupCustomSpinner.setAdapterData(groupInfoList, itemBinder)
    }

    private fun handlerGroupInfoSuccess(groupInfos: MutableList<GroupInfo>) {
        groupInfoList = groupInfos
        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort
                if (isDropDown) {
                    textView.setTextColor(this@StatisticsActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@StatisticsActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        groupCustomSpinner.setAdapterData(groupInfoList, itemBinder)
        // Global.progressOFF()
    }

    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
        // Global.progressOFF()
        userList = groupUsers

        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupUser> {
            override fun bindItem(view: View, item: GroupUser, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.userName
                if (isDropDown) {
                    textView.setTextColor(this@StatisticsActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@StatisticsActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        userCustomSpinner.setAdapterData(userList, itemBinder)
    }

    private fun handleStatisticsSleepSuccess(trainingOverallList: MutableList<TrainingOverallGraphItem.TrainingOverallGraph>) {
        trainingOverallStatusList.clear()
        injuryWeekList.clear()
        injuryMonthList.clear()

        trainingOverallList.forEach { item ->
            when (item.trainingOverallItemName) {
                "Injury" -> {
                    item.trainingOverallData.week.filter { it.userId == selectedUserId }.forEach {
                        injuryWeekList.add(it)
                    }
                    item.trainingOverallData.month.filter { it.userId == selectedUserId }.forEach {
                        injuryMonthList.add(it)
                    }

                    item.trainingOverallData.day.map {
                        if (it.userId == selectedUserId && it.trainingDate == selectedDateTime) {
                            binding.tvInjury.text = (it.injuryIndex!! * 100).toInt().toString() + "%"
                        }
                    }
                }
                "Sleep" -> {
                    trainingOverallStatusList.addAll(listOf(item))
                    item.trainingOverallData.day.map {
                        if (it.userId == selectedUserId && it.trainingDate == selectedDateTime) {
                            binding.tvSleepDuration.text = "${it.sleepDuration!!.toInt()}h"
                            binding.tvSleepQuality.text = it.sleepIndex!!.toInt().toString()
                        }
                    }
                }
            }
        }


    }

    private fun handleTrainingSubStatisticsSuccess(trainingSubStatisticsData: TrainingSubStatisticsGraphItem.TrainingSubStatisticsGraph) {
        trainingStatistics = trainingSubStatisticsData
    }

    private fun handleTssDataTimeSuccess(trainingTssDataTime: TrainingTssDataTime) {
        tssDataTime = trainingTssDataTime

        val tssAll = tssDataTime.all
        val tssT1 = tssDataTime.T1
        val tssT2 = tssDataTime.T2
        val tssT3 = tssDataTime.T3
        val tssT4 = tssDataTime.T4
        val tssT5 = tssDataTime.T5

        binding.tvAllTss.text = tssAll.toString()
        binding.tvT1.text = getString(R.string.dawn)  + " : " + tssT1.toString()
        binding.tvT2.text = getString(R.string.morning)  + " : " + tssT2.toString()
        binding.tvT3.text = getString(R.string.afternoon)  + " : " + tssT3.toString()
        binding.tvT4.text = getString(R.string.dinner)  + " : " + tssT4.toString()
        binding.tvT5.text = getString(R.string.night)  + " : " + tssT5.toString()
    }

    private inline fun <reified T : Any> observeNetworkState(
        stateLiveData: LiveData<NetworkState<T>>,
        crossinline onSuccess: (NetworkState.Success<T>) -> Unit,
        crossinline onError: (NetworkState.Error) -> Unit
    ) {
        stateLiveData.observe(this) { state ->
            when (state) {
                is NetworkState.Success -> {
                    onSuccess(state)
                    setTrainingOverallSlpeeAdapter(trainingOverallStatusList)
                    setTrainingStatisticsAdapter(trainingStatistics,injuryWeekList, injuryMonthList)
                    Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                }
                is NetworkState.Error -> {
                    handlerError(state.message)
                    onError(state)
                }
                is NetworkState.Loading -> {
                    handlerLoading(state.isLoading)
                }
            }
        }
    }

    private fun handlerLoading(isLoading: Boolean) {
        if (_binding != null) {

            if (isLoading) {
                binding.llProgressBar.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.llProgressBar.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {}
    }


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
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