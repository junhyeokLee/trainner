package com.sports2i.trainer.ui.fragment.group

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.Notice
import com.sports2i.trainer.data.model.NoticeResponse
import com.sports2i.trainer.data.model.SurveyOverallGraphItem
import com.sports2i.trainer.data.model.TrainingOverall
import com.sports2i.trainer.data.model.TrainingOverallGraphItem
import com.sports2i.trainer.databinding.FragmentGroupBinding
import com.sports2i.trainer.ui.activity.NoticeDetailActivity
import com.sports2i.trainer.ui.activity.NoticeListActivity
import com.sports2i.trainer.ui.activity.NoticeWriteActivity
import com.sports2i.trainer.ui.activity.NutritionCommentRegistrationActivity
import com.sports2i.trainer.ui.activity.NutritionStatusActivity
import com.sports2i.trainer.ui.activity.SurveyActivity
import com.sports2i.trainer.ui.activity.TrainingRegistrationActivity
import com.sports2i.trainer.ui.activity.TrainingStatusActivity
import com.sports2i.trainer.ui.adapter.group.GroupNoticeAdapter
import com.sports2i.trainer.ui.adapter.group.GroupStatusTabAdapter
import com.sports2i.trainer.ui.fragment.BaseFragment
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.ui.widget.GraphTrainingOverall
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.NoticeViewModel
import com.sports2i.trainer.viewmodel.TrainingOverallViewModel
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupFragment : BaseFragment<FragmentGroupBinding>() {

    private var TRAINING_STATUS: Int = 0
    private var NUTRITION_STATUS: Int = 1

    private lateinit var tabAdapter: GroupStatusTabAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var groupNoticeAdapter: GroupNoticeAdapter
    private lateinit var groupCustomSpinner: CustomSpinner // CustomSpinner 정의
    private val fragmentGroupBinding
        get() = binding!!

    private val groupViewModel: GroupViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()
    private val noticeViewModel: NoticeViewModel by viewModels()
    private val trainingOverallViewModel: TrainingOverallViewModel by viewModels()

    private var userList: MutableList<GroupUser> = mutableListOf()
    private var noticeList: MutableList<Notice> = mutableListOf()
    private var noticeLimitList: MutableList<Notice> = mutableListOf()
    private var trainingStatusList: MutableList<TrainingOverall> = mutableListOf()
    private var trainingOverallStatusList: MutableList<TrainingOverallGraphItem.TrainingOverallGraph> = mutableListOf()
    private var surveyOverallStatusList: MutableList<SurveyOverallGraphItem.SurveyOverallGraph> = mutableListOf()
    private var groupInfoList: List<GroupInfo> = mutableListOf()

    private var customTabViews = ArrayList<View>()

    private var selectedGroupId: String = ""
    private var selectedGroup: GroupInfo? = null
    private var strTrainingDate: String = ""

    private var groupInfosLoaded = false
    private var trainingOverallLoaded = false
    private var noticeLoaded = false
    private var trainingOverallListLoaded = false
    private var surveyOverallListLoaded = false

    val callBackActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
        val noticeInsert = result.data?.getBooleanExtra("noticeInsert",false)
        val noticeDelete = result.data?.getBooleanExtra("noticeDelete",false)
        val noticeEdit = result.data?.getBooleanExtra("noticeEdit",false)
        val surveySuccess = result.data?.getBooleanExtra("surveySuccess",false)

        if(noticeInsert == true) noticeViewModel.getNotice(1)
        if(noticeDelete == true) noticeViewModel.getNotice(1)
        if(noticeEdit == true) noticeViewModel.getNotice(1)

        if(surveySuccess == true){
            Log.e("surveySuccess","true")
            trainingOverallViewModel.requestSurveyStatistics(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroupId,"survey",strTrainingDate)
             }
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGroupBinding {
        return FragmentGroupBinding.inflate(inflater, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContent()
        setFunction()
        networkStatus()
    }

    override fun setContent() {
        showBottomNavigation()
        hideTopBar()

        Preferences.init(this.requireContext(), Preferences.DB_USER_INFO)
        noticeViewModel.getNotice(1)

        groupCustomSpinner = fragmentGroupBinding.spinnerGroup // CustomSpinner 초기화
        groupCustomSpinner.isEnabled = false

        val formattedDate = DateTimeUtil.getCurrentFormattedDate()
        fragmentGroupBinding.tvGroupDate.text = formattedDate

        if(checkAdminOrTrainerAuthority())
        {
            groupCustomSpinner.isEnabled = true
            groupCustomSpinner.setBackgroundResource(R.drawable.bg_drop_dowun)

            fragmentGroupBinding.tvNoticeWrite.visibility = View.VISIBLE
            fragmentGroupBinding.tvSurveyWrite.visibility = View.VISIBLE
            fragmentGroupBinding.tvDetailStatus.visibility = View.VISIBLE

            groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))
        }
        else
        {
            groupCustomSpinner.isEnabled = false
            groupCustomSpinner.setBackgroundResource(R.drawable.bg_my_drop_dowun)

            fragmentGroupBinding.tvTrainingEnroll.visibility = View.GONE
            fragmentGroupBinding.tvCommentEnroll.visibility = View.GONE
            fragmentGroupBinding.tvNoticeWrite.visibility = View.GONE
            fragmentGroupBinding.tvSurveyWrite.visibility = View.GONE
            fragmentGroupBinding.tvDetailStatus.visibility = View.GONE

            groupViewModel.getSelectedGroup(Preferences.string(Preferences.KEY_GROUP_ID))
        }

        noticeLimitList = noticeList.take(5).toMutableList()

        groupNoticeAdapter = GroupNoticeAdapter(noticeLimitList)
        tabAdapter = GroupStatusTabAdapter(this@GroupFragment)
        fragmentGroupBinding.statusViewPager.adapter = tabAdapter


        fragmentGroupBinding.rvNotice.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            setNestedScrollingEnabled(false)
            adapter = groupNoticeAdapter
        }

        groupCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGroup = parent?.getItemAtPosition(position) as GroupInfo
                selectedGroupId = selectedGroup!!.groupId
                strTrainingDate = fragmentGroupBinding.tvGroupDate.text.toString().replace(".", "-")
                // selectedGroupInfo를 GroupViewModel에 저장

                groupViewModel.getSelectedGroupInfoLiveData().observe(viewLifecycleOwner, Observer { selectedGroup = it })
                trainingViewModel.getTrainingOverallGroup(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroupId,strTrainingDate)
                trainingOverallViewModel.requestTrainingStatistics(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroupId,"training",strTrainingDate)
                trainingOverallViewModel.requestSurveyStatistics(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroupId,"survey",strTrainingDate)
            }
        }
    }

    override fun setFunction() {
        refreshing()

        fragmentGroupBinding.imgLeftArrow.setOnClickListener {
            DateTimeUtil.navigateToPreviousDay()
            updateDateText()
        }
        fragmentGroupBinding.imgRightArrow.setOnClickListener {
            DateTimeUtil.navigateToNextDay()
            updateDateText()
        }
        fragmentGroupBinding.tvGroupDate.setOnClickListener {
            DateTimeUtil.showDatePickerDialog(requireContext(), fragmentGroupBinding.tvGroupDate.text.toString()) { newSelectedDate ->
                fragmentGroupBinding.tvGroupDate.text = newSelectedDate
                strTrainingDate = newSelectedDate.replace(".", "-")
                trainingViewModel.getTrainingOverallGroup(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroupId,strTrainingDate)
            }
        }
        fragmentGroupBinding.tvTrainingEnroll.setOnClickListener {
            selectedGroup = fragmentGroupBinding.spinnerGroup.selectedItem as GroupInfo
            val intent = Intent(context, TrainingRegistrationActivity::class.java)
            intent.putExtra("selectedGroup", selectedGroup)
            intent.putExtra("dateTime", strTrainingDate)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.page_right_in, R.anim.page_left_out)
        }
        fragmentGroupBinding.tvCommentEnroll.setOnClickListener {
            selectedGroup = fragmentGroupBinding.spinnerGroup.selectedItem as GroupInfo
            val intent = Intent(context, NutritionCommentRegistrationActivity::class.java)
            intent.putExtra("selectedGroup", selectedGroup)
            intent.putExtra("dateTime", strTrainingDate)
            startActivity(intent)
        }
        fragmentGroupBinding.tvSurveyWrite.setOnClickListener {
            val intent = Intent(context, SurveyActivity::class.java)
            intent.putExtra("selectedGroup", selectedGroup)
            callBackActivityResultLauncher.launch(intent)
        }
        fragmentGroupBinding.tvDetailStatus.setOnClickListener {
            selectedGroup = fragmentGroupBinding.spinnerGroup.selectedItem as GroupInfo
            // statusTabLayout 또는 statusViewPager의 현재 위치
            val currentTabPosition = fragmentGroupBinding.statusViewPager.currentItem
            if (currentTabPosition == 0) {
                val intent = Intent(context, TrainingStatusActivity::class.java)
                intent.putExtra("selectedGroup", selectedGroup)
                intent.putExtra("dateTime", strTrainingDate)
                startActivity(intent)
            } else {
                val intent = Intent(context, NutritionStatusActivity::class.java)
                intent.putExtra("selectedGroup", selectedGroup)
                intent.putExtra("dateTime", strTrainingDate)
                startActivity(intent)
            }
        }

        fragmentGroupBinding.tvTotalNotice.setOnClickListener {
            val intent = Intent(context, NoticeListActivity::class.java)
            callBackActivityResultLauncher.launch(intent)
        }

        fragmentGroupBinding.tvNoticeWrite.setOnClickListener {
            val intent = Intent(context, NoticeWriteActivity::class.java)
            callBackActivityResultLauncher.launch(intent)
        }

    }

    private fun setTrainingStatusTabAdapter(filteredTrainingList: MutableList<TrainingOverall>) {

        // ER) TRAINEE 권한일때 프래그먼트 재이동시 selectedGroup이 null일 때가 있음
        if(filteredTrainingList != null && selectedGroup != null) {

            tabAdapter = GroupStatusTabAdapter(this@GroupFragment)
            tabAdapter.addFragment(
                GroupTrainingStatusFragment.newInstance(
                    TRAINING_STATUS,
                    filteredTrainingList,
                    selectedGroup!!,
                    strTrainingDate
                ), getString(R.string.training_stastus)
            )
            tabAdapter.addFragment(
                GroupTrainingStatusFragment.newInstance(
                    NUTRITION_STATUS,
                    filteredTrainingList,
                    selectedGroup!!,
                    strTrainingDate
                ), getString(R.string.diet_stastus)
            )

            fragmentGroupBinding.statusViewPager.adapter = tabAdapter
            tabLayout = fragmentGroupBinding.statusTabLayout

            TabLayoutMediator(
                fragmentGroupBinding.statusTabLayout, fragmentGroupBinding.statusViewPager
            ) { tab, position ->
                tab.text = tabAdapter.getTabTitle(position) // 탭 타이틀 설정
            }.attach()

            if (customTabViews.isEmpty()) tabLayoutTrainingStatusTextColor(tabLayout)
            val initialTabPosition = tabLayout.selectedTabPosition

            updateTrainingStatusTabTextColor(initialTabPosition)  // 초기 선택된 탭의 색상을 업데이트

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val position = tabLayout.selectedTabPosition
                    updateTrainingStatusTabTextColor(position) // 선택된 탭의 색상을 업데이트
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            fragmentGroupBinding.statusViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    var statusFragment =
                        tabAdapter.getFragment(position) as? GroupTrainingStatusFragment
                    when (position) {
                        0 -> if (statusFragment?.isAdded == true) statusFragment?.refreshData()
                        1 -> if (statusFragment?.isAdded == true) statusFragment?.refreshData()
                    }
                }
            })

        }
        else return
    }

    private fun updateDateText() {
        val formattedDate = DateTimeUtil.getCurrentFormattedDate()
        fragmentGroupBinding.tvGroupDate.text = formattedDate
        strTrainingDate = formattedDate.replace(".", "-")
        trainingViewModel.getTrainingOverallGroup(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroupId,strTrainingDate)
        trainingOverallViewModel.requestTrainingStatistics(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroupId,"training",strTrainingDate)
        trainingOverallViewModel.requestSurveyStatistics(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroupId,"survey",strTrainingDate)
    }

    private fun updateTrainingStatusTabTextColor(selectedPosition: Int) {
        for (i in customTabViews.indices) {
            val tabView = customTabViews[i]
            val tabText = tabView.findViewById<TextView>(R.id.tabText)
            if (i == selectedPosition) {
                if (checkAdminOrTrainerAuthority()) {
                    when (selectedPosition) {
                        0 -> {
                            fragmentGroupBinding.tvTrainingEnroll.visibility = View.VISIBLE
                            fragmentGroupBinding.tvCommentEnroll.visibility = View.GONE
                        }
                        else -> {
                            fragmentGroupBinding.tvCommentEnroll.visibility = View.VISIBLE
                            fragmentGroupBinding.tvTrainingEnroll.visibility = View.GONE
                        }
                    }
                }

                tabText.setTextColor(resources.getColor(R.color.black))
                tabText.setTypeface(null, Typeface.NORMAL)  // Bold로 설정

            } else {
                tabText.setTextColor(resources.getColor(R.color.gray))
                tabText.setTypeface(null, Typeface.NORMAL)  // Normal로 설정
            }
        }
    }

    private fun tabLayoutTrainingStatusTextColor(tabLayout: TabLayout) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val customTabView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_tab_layout, null)
            val tabText = customTabView.findViewById<TextView>(R.id.tabText)

            tabText.text = tab?.text
            customTabViews.add(customTabView)
            tab?.customView = customTabView
        }

        // 초기에 첫 번째 탭 설정
        val firstTabView = customTabViews[0]
        val firstTabText = firstTabView.findViewById<TextView>(R.id.tabText)
        firstTabText.setTextColor(resources.getColor(R.color.black))
    }

    fun updateTrainingGraphList(selectedList: List<Graph>) {
        val graphLayout1 = binding.graphLayout1
        graphLayout1.removeAllViews()
        graphLayout1.addView(GraphTrainingOverall(requireContext(), 1, "name", selectedList))
    }

    fun updateTrainingGraphListMap(tabPosition: Int, selectedMap: Map<String, List<Graph>>) {
        val selectedList = selectedMap[getSelectedTrainingMapKey(tabPosition)] ?: emptyList()
        val distinctList = selectedList.distinctBy { it.userName }

        if (selectedList.isNullOrEmpty()) {
            fragmentGroupBinding.tvGraph1Empty.visibility = View.VISIBLE
            fragmentGroupBinding.graphLayout1.visibility = View.GONE
        } else {
            updateTrainingGraphList(distinctList)
            fragmentGroupBinding.tvGraph1Empty.visibility = View.GONE
            fragmentGroupBinding.graphLayout1.visibility = View.VISIBLE
        }
    }

    fun updateSurveyGraphList(selectedList: List<Graph>) {
        val graphLayout2 = binding.graphLayout2
        graphLayout2.removeAllViews()
        graphLayout2.addView(GraphTrainingOverall(requireContext(), 1, "name",selectedList))
    }

    fun updateSurveyGraphList(tabPosition: Int, selectedMap: Map<String, List<Graph>>) {
        val selectedList = selectedMap[getSelectedSurveyMapKey(tabPosition,surveyOverallStatusList)] ?: emptyList()
        val distinctList = selectedList.distinctBy { it.userName }
        if(selectedList.isNullOrEmpty()){
            fragmentGroupBinding.tvGraph2Empty.visibility = View.VISIBLE
            fragmentGroupBinding.graphLayout2.visibility = View.GONE
        }
        else{
            updateSurveyGraphList(distinctList)
            fragmentGroupBinding.tvGraph2Empty.visibility = View.GONE
            fragmentGroupBinding.graphLayout2.visibility = View.VISIBLE
        }
    }

    private fun setTrainingOverallStatusTabAdapter(trainingOverallStatusList: List<TrainingOverallGraphItem.TrainingOverallGraph>) {
        val statisticsTabLayout = fragmentGroupBinding.statisticsTabLayout
        val todayCheckBox = fragmentGroupBinding.checkboxToday
        val weekCheckBox = fragmentGroupBinding.checkboxWeeks
        val monthCheckBox = fragmentGroupBinding.checkboxMonth

        todayCheckBox.isChecked = true
        weekCheckBox.isChecked = false
        monthCheckBox.isChecked = false

        todayCheckBox.text = fragmentGroupBinding.tvGroupDate.text.toString().takeLast(5)

        val trainingOverallDayMap = mutableMapOf<String, List<Graph>>()
        val trainingOverallWeekMap = mutableMapOf<String, List<Graph>>()
        val trainingOverallMonthMap = mutableMapOf<String, List<Graph>>()

        statisticsTabLayout.removeAllTabs()


        for (i in 0 until trainingOverallStatusList.size) {
            val itemName = trainingOverallStatusList[i].trainingOverallItemName
            val tab = statisticsTabLayout.newTab()

            tab.text = getStatisticsTrainingTabName(itemName)
            statisticsTabLayout.addTab(tab)

            if (itemName in setOf("Performance", "Injury", "Sleep", "SleepDuration", "Nutrition")) {
                trainingOverallDayMap[itemName] = extractTrainingGraphList(trainingOverallStatusList[i], itemName, "day")
                trainingOverallWeekMap[itemName] = extractTrainingGraphList(trainingOverallStatusList[i], itemName, "week")
                trainingOverallMonthMap[itemName] = extractTrainingGraphList(trainingOverallStatusList[i], itemName, "month")
            }

            tab.view.setOnClickListener {
                updateTrainingGraphListMap(tab.position, when {
                    todayCheckBox.isChecked -> trainingOverallDayMap
                    weekCheckBox.isChecked -> trainingOverallWeekMap
                    monthCheckBox.isChecked -> trainingOverallMonthMap
                    else -> emptyMap()
                })
            }
        }

        updateTrainingGraphListMap(0, trainingOverallDayMap)
        todayCheckBox.isClickable = false

        todayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                todayCheckBox.isClickable = false

                weekCheckBox.isChecked = false
                monthCheckBox.isChecked = false

                weekCheckBox.isClickable = true
                monthCheckBox.isClickable = true

                updateTrainingGraphListMap(statisticsTabLayout.selectedTabPosition, trainingOverallDayMap)
            }
        }

        weekCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                weekCheckBox.isClickable = false

                todayCheckBox.isChecked = false
                monthCheckBox.isChecked = false

                todayCheckBox.isClickable = true
                monthCheckBox.isClickable = true

                updateTrainingGraphListMap(statisticsTabLayout.selectedTabPosition, trainingOverallWeekMap)
            }
        }

        monthCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                monthCheckBox.isClickable = false

                todayCheckBox.isChecked = false
                weekCheckBox.isChecked = false

                todayCheckBox.isClickable = true
                weekCheckBox.isClickable = true

                updateTrainingGraphListMap(statisticsTabLayout.selectedTabPosition, trainingOverallMonthMap)
            }
        }
    }

    private fun setSurveyOverallStatusTabAdapter(surveyOverallList: MutableList<SurveyOverallGraphItem.SurveyOverallGraph>){
        val statisticsTabLayout = fragmentGroupBinding.statisticsTabLayout2
        val todayCheckBox = fragmentGroupBinding.checkboxToday2
        val weekCheckBox = fragmentGroupBinding.checkboxWeeks2
        val monthCheckBox = fragmentGroupBinding.checkboxMonth2

        todayCheckBox.isChecked = true
        weekCheckBox.isChecked = false
        monthCheckBox.isChecked = false

        todayCheckBox.text = fragmentGroupBinding.tvGroupDate.text.toString().takeLast(5)

        val surveyOverallDayMap = mutableMapOf<String, List<Graph>>()
        val surveyOverallWeekMap = mutableMapOf<String, List<Graph>>()
        val surveyOverallMonthMap = mutableMapOf<String, List<Graph>>()

        statisticsTabLayout.removeAllTabs()

        for (i in 0 until surveyOverallList.size) {
            val itemName = surveyOverallList[i].surveyItemName

            val tab = statisticsTabLayout.newTab()
            tab.text = itemName
            statisticsTabLayout.addTab(tab)

            surveyOverallDayMap[itemName] = extractSurveyGraphList(surveyOverallList[i], "day")
            surveyOverallWeekMap[itemName] = extractSurveyGraphList(surveyOverallList[i], "week")
            surveyOverallMonthMap[itemName] = extractSurveyGraphList(surveyOverallList[i], "month")


            tab.view.setOnClickListener {
                updateSurveyGraphList(tab.position, when {
                    todayCheckBox.isChecked -> surveyOverallDayMap
                    weekCheckBox.isChecked -> surveyOverallWeekMap
                    monthCheckBox.isChecked -> surveyOverallMonthMap
                    else -> emptyMap()
                })
            }
        }


        // Initial setup for the first tab and checkbox
        updateSurveyGraphList(0, surveyOverallDayMap)
        todayCheckBox.isClickable = false

        todayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                todayCheckBox.isClickable = false

                weekCheckBox.isChecked = false
                monthCheckBox.isChecked = false

                weekCheckBox.isClickable = true
                monthCheckBox.isClickable = true
                updateSurveyGraphList(statisticsTabLayout.selectedTabPosition, surveyOverallDayMap)
            }
        }

        weekCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                weekCheckBox.isClickable = false

                todayCheckBox.isChecked = false
                monthCheckBox.isChecked = false

                todayCheckBox.isClickable = true
                monthCheckBox.isClickable = true
                updateSurveyGraphList(statisticsTabLayout.selectedTabPosition, surveyOverallWeekMap)
            }
        }

        monthCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                monthCheckBox.isClickable = false

                todayCheckBox.isChecked = false
                weekCheckBox.isChecked = false

                todayCheckBox.isClickable = true
                weekCheckBox.isClickable = true

                updateSurveyGraphList(statisticsTabLayout.selectedTabPosition, surveyOverallMonthMap)
            }
        }
    }



    override fun networkStatus() {

        groupViewModel.selectedGroupInfoState.observe(viewLifecycleOwner) { selectedGroupInfoState ->
            when (selectedGroupInfoState) {
                is NetworkState.Success -> {
                    handlerGroupInfoSuccess(selectedGroupInfoState.data.data)
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(selectedGroupInfoState.message)
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(selectedGroupInfoState.isLoading)
            }
        }

        groupViewModel.groupInfoState.observe(viewLifecycleOwner) { groupInfoState ->
            when (groupInfoState) {
                is NetworkState.Success -> {
                    handlerGroupInfosSuccess(groupInfoState.data.data)
                    groupInfosLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(groupInfoState.message)
                    groupInfosLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(groupInfoState.isLoading)
            }
        }

        groupViewModel.groupUserState.observe(viewLifecycleOwner) { groupUserState ->
            when (groupUserState) {
                is NetworkState.Success -> {
                    handlerGroupUsersSuccess(groupUserState.data.data)
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(groupUserState.message)
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(groupUserState.isLoading)
            }
        }

        noticeViewModel.noticeState.observe(viewLifecycleOwner) { noticeState ->
            when (noticeState) {
                is NetworkState.Success -> {
                    handlerNoticeSuccess(noticeState.data.data)
                    noticeLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(noticeState.message)
                    noticeLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(noticeState.isLoading)
            }
        }

        trainingViewModel.trainingOverallGroupState.observe(viewLifecycleOwner) { trainingOverallState ->
            when (trainingOverallState) {
                is NetworkState.Success -> {
                    handlerTrainingOverallSuccess(trainingOverallState.data.data)
                    trainingOverallLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(trainingOverallState.message)
                    trainingOverallLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(trainingOverallState.isLoading)
            }
        }

        trainingOverallViewModel.trainingOverallState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Success -> {
                    handlerTrainingOverallListSuccess(it.data.data)
                    trainingOverallListLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(it.message)
                    trainingOverallListLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
        }
        trainingOverallViewModel.surveyOverallState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Success -> {
                    handlerSurveyOverallListSuccess(it.data.data)
                    surveyOverallListLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(it.message)
                    surveyOverallListLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun refreshing() {
        fragmentGroupBinding.refreshLayout?.setOnRefreshListener {
            val formattedDate = DateTimeUtil.getCurrentFormattedDate()
            fragmentGroupBinding.tvGroupDate.text = formattedDate
            strTrainingDate = formattedDate.replace(".", "-")

            noticeViewModel.getNotice(1)
            trainingViewModel.getTrainingOverallGroup(Preferences.string(Preferences.KEY_ORGANIZATION_ID),(groupCustomSpinner.selectedItem as GroupInfo).groupId,strTrainingDate)
            trainingOverallViewModel.requestTrainingStatistics(Preferences.string(Preferences.KEY_ORGANIZATION_ID),(groupCustomSpinner.selectedItem as GroupInfo).groupId,"training",strTrainingDate)
            trainingOverallViewModel.requestSurveyStatistics(Preferences.string(Preferences.KEY_ORGANIZATION_ID),(groupCustomSpinner.selectedItem as GroupInfo).groupId,"survey",strTrainingDate)

            fragmentGroupBinding.refreshLayout?.isRefreshing = false

        }
    }

    private fun handlerGroupInfoSuccess(groupInfos: GroupInfo) {
        selectedGroup = groupInfos
        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort

                if (isDropDown) textView.setTextColor(requireContext().resources.getColor(android.R.color.black))
                else textView.setTextColor(requireContext().resources.getColor(android.R.color.white))

            }
        }
        var groupInfoList = mutableListOf<GroupInfo>()
        groupInfoList.add(selectedGroup!!)
        groupCustomSpinner.setAdapterData(groupInfoList, itemBinder)
    }

    private fun handlerGroupInfosSuccess(groupInfos: List<GroupInfo>) {
        groupInfoList = groupInfos
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort
                if (isDropDown) {
                    textView.setTextColor(requireContext().resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(requireContext().resources.getColor(android.R.color.white))
                }
            }
        }
        groupCustomSpinner.setAdapterData(groupInfoList, itemBinder)
    }

    private fun handlerGroupUsersSuccess(groupUsers: MutableList<GroupUser>) {
        userList = groupUsers
    }

    private fun handlerTrainingOverallSuccess(trainingStatus: MutableList<TrainingOverall>) {

        trainingStatusList.clear()
        trainingStatusList.addAll(trainingStatus)

        if(trainingStatusList.isNullOrEmpty()) {
            fragmentGroupBinding.tvStatusEmpty.visibility = View.VISIBLE
            tabAdapter.clearFragment()
        }
        else {
            val filteredTrainingList : MutableList<TrainingOverall> = trainingStatusList.distinctBy { it.userId } as MutableList<TrainingOverall>
            fragmentGroupBinding.tvStatusEmpty.visibility = View.GONE
            setTrainingStatusTabAdapter(filteredTrainingList)
        }

    }

    private fun handlerNoticeSuccess(notices: NoticeResponse.NoticeListResponse) {
        noticeList.clear()
        notices.noticeList.forEach {
            noticeList.add(it)
        }

        noticeLimitList = noticeList.take(5).toMutableList()

        groupNoticeAdapter = GroupNoticeAdapter(noticeLimitList)
        fragmentGroupBinding.rvNotice.adapter = groupNoticeAdapter
        fragmentGroupBinding.rvNotice.setNestedScrollingEnabled(false)

        groupNoticeAdapter.notifyDataSetChanged()


        groupNoticeAdapter.mListener = object : GroupNoticeAdapter.OnItemClickListener {
            override fun onNoticeClicked(position: Int, notice: Notice) {
                val intent = Intent(context, NoticeDetailActivity::class.java)
                intent.putExtra("noticeId", notice.id)
                intent.putExtra("groupId", notice.groupId)
                intent.putExtra("dateTime", strTrainingDate)

                callBackActivityResultLauncher.launch(intent)
            }
        }
    }
    private fun handlerTrainingOverallListSuccess(trainingOverallList: MutableList<TrainingOverallGraphItem.TrainingOverallGraph>) {
        trainingOverallStatusList.clear()
        trainingOverallStatusList.addAll(trainingOverallList)
        setTrainingOverallStatusTabAdapter(trainingOverallStatusList)
    }
    private fun handlerSurveyOverallListSuccess(surveyOverallList: MutableList<SurveyOverallGraphItem.SurveyOverallGraph>) {
        surveyOverallStatusList.clear()
        surveyOverallStatusList.addAll(surveyOverallList)
        setSurveyOverallStatusTabAdapter(surveyOverallStatusList)
    }


    private fun getStatisticsTrainingTabName(itemName: String): String {
        return when (itemName) {
            "Performance" -> "훈련성취도"
            "Injury" -> "부상위험도"
            "Sleep" -> "수면품질"
            "SleepDuration" -> "수면시간"
            "Nutrition" -> "영양섭취"
            else -> itemName
        }
    }

    private fun extractTrainingGraphList(trainingOverallGraph: TrainingOverallGraphItem.TrainingOverallGraph, itemName: String, period: String): List<Graph> {
        return when (period) {
            "day" -> trainingOverallGraph.trainingOverallData.day.map { itemData ->
                val userName = itemData.userName
                val trainingDate = itemData.trainingDate
                val value = when (itemName) {
                    "Performance" -> itemData.performanceIndex?.toInt() ?: 0
                    "Injury" -> (itemData.injuryIndex!! * 100).toInt() ?: 0
                    "Sleep" -> itemData.sleepIndex?.toInt() ?: 0
                    "SleepDuration" -> itemData.sleepDuration?.toInt() ?: 0
                    "Nutrition" -> itemData.nutritionIndex?.toInt() ?: 0
                    else -> 0
                }
                Graph(userName = userName, trainingDate = trainingDate, value = value)
            }

            "week" -> trainingOverallGraph.trainingOverallData.week.map { itemData ->
                val userName = itemData.userName
                val trainingDate = itemData.trainingDate
                val value = when (itemName) {
                    "Performance" -> itemData.performanceWeekAvg?.toInt() ?: 0
                    "Injury" -> (itemData.injuryWeekAvg!! * 100).toInt() ?: 0
                    "Sleep" -> itemData.sleepWeekAvg?.toInt() ?: 0
                    "SleepDuration" -> itemData.sleepDurationWeekAvg?.toInt() ?: 0
                    "Nutrition" -> itemData.nutritionWeekAvg?.toInt() ?: 0
                    else -> 0
                }
                Graph(userName = userName, trainingDate = trainingDate, value = value)
            }
            "month" -> trainingOverallGraph.trainingOverallData.month.map { itemData ->
                val userName = itemData.userName
                val trainingDate = itemData.trainingDate
                val value = when (itemName) {
                    "Performance" -> itemData.performanceMonthAvg?.toInt() ?: 0
                    "Injury" -> (itemData.injuryMonthAvg!! * 100).toInt() ?: 0
                    "Sleep" -> itemData.sleepMonthAvg?.toInt() ?: 0
                    "SleepDuration" -> itemData.sleepDurationMonthAvg?.toInt() ?: 0
                    "Nutrition" -> itemData.nutritionMonthAvg?.toInt() ?: 0
                    else -> 0
                }
                Graph(userName = userName, trainingDate = trainingDate, value = value)
            }
            else -> emptyList()
        }
    }

    private fun getSelectedTrainingMapKey(tabPosition: Int): String {
        return when (tabPosition) {
            0 -> "Performance"
            1 -> "Injury"
            2 -> "Sleep"
            3 -> "SleepDuration"
            4 -> "Nutrition"
            else -> ""
        }
    }


    private fun extractSurveyGraphList(
        surveyOverallGraph: SurveyOverallGraphItem.SurveyOverallGraph, period: String): List<Graph> {
        return when (period) {
            "day" -> surveyOverallGraph.surveyData?.day ?: emptyList()
            "week" -> surveyOverallGraph.surveyData?.week ?: emptyList()
            "month" -> surveyOverallGraph.surveyData?.month ?: emptyList()
            else -> emptyList()
        }.map { itemData ->
            val userName = itemData.userName
            val trainingDate = ""
            val value = itemData.surveyValueAvg!!.toInt() * 10 ?: 0
            Graph(userName = userName, trainingDate = trainingDate, value = value)
        }
    }

    private fun getSelectedSurveyMapKey(tabPosition: Int,surveyOverallList: MutableList<SurveyOverallGraphItem.SurveyOverallGraph>): String {

        return when (tabPosition) {
            in 0 until surveyOverallList.size -> surveyOverallList[tabPosition].surveyItemName
            else -> ""
        }
    }

    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun handlerLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.llProgressBar.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.llProgressBar.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updateLoadingState() {

        if (noticeLoaded && trainingOverallLoaded
            && trainingOverallListLoaded && surveyOverallListLoaded ) {
            // 모든 통신이 완료되었을 때 로딩 상태를 업데이트.
            handlerLoading(false)
        }
    }
}

