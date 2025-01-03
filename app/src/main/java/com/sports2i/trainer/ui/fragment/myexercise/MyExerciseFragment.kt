package com.sports2i.trainer.ui.fragment.myexercise

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.NutritionPictureUser
import com.sports2i.trainer.data.model.PainInfo
import com.sports2i.trainer.data.model.SurveyInsert
import com.sports2i.trainer.data.model.SurveyPreset
import com.sports2i.trainer.data.model.SurveySearch
import com.sports2i.trainer.data.model.TrainingGroupStatus
import com.sports2i.trainer.data.model.TrainingOverall
import com.sports2i.trainer.databinding.FragmentMyExerciseBinding
import com.sports2i.trainer.ui.activity.NutritionCommentDetailMyActivity
import com.sports2i.trainer.ui.activity.PainActivity
import com.sports2i.trainer.ui.activity.TrainingConfirmMyActivity
import com.sports2i.trainer.ui.activity.TrainingExerciseDetailMyActivity
import com.sports2i.trainer.ui.activity.StatisticsActivity
import com.sports2i.trainer.ui.adapter.myexercise.ExerciseTimeTabMyAdapter
import com.sports2i.trainer.ui.adapter.groupmember.GroupMemberNutritionPictureAdapter
import com.sports2i.trainer.ui.dialog.CustomIngredientDialogFragment
import com.sports2i.trainer.ui.dialog.CustomSurveyInsertDialog
import com.sports2i.trainer.ui.fragment.BaseFragment
import com.sports2i.trainer.ui.widget.GraphTrainingOverall
import com.sports2i.trainer.utils.ColorUtil
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.NutritionViewModel
import com.sports2i.trainer.viewmodel.PainInfoViewModel
import com.sports2i.trainer.viewmodel.SurveyViewModel
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale


@AndroidEntryPoint
class MyExerciseFragment : BaseFragment<FragmentMyExerciseBinding>() {

    private lateinit var tabAdapter: ExerciseTimeTabMyAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var nutritionGroupMemberStatusDetailAdater: GroupMemberNutritionPictureAdapter
    private val fragmentGroupMemberBinding
        get() = binding!!

    private val groupViewModel: GroupViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()
    private val nutritionViewModel: NutritionViewModel by viewModels()
    private val painInfoViewModel: PainInfoViewModel by viewModels()
    private val surveyViewModel : SurveyViewModel by viewModels()

    private var userList: MutableList<GroupUser> = mutableListOf()
    private var nutritionUserStatusList: MutableList<NutritionPictureUser> = mutableListOf()
    private var trainingExerciseList: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise> = mutableListOf()
    private var surveyPresetList: MutableList<SurveyPreset> = mutableListOf()

    private var userId: String = ""
    private var dateTime: String = ""
    private var groupId: String = ""
    private var organizationId: String = ""
    private var groupInfo: GroupInfo? = null

    private val painMap = HashMap<Int, PainInfo>()
    private var painX = 0.0F
    private var painY = 0.0F

    private var customTabViews = ArrayList<View>()

    private lateinit var customGroupSpinner: TextView
    private lateinit var customUserSpinner: TextView

    private var groupUserLoaded = false
    private var trainingOverallUserLoaded = false
    private var nutritionPictureUserLoaded = false
    private var painLoaded = false
    private var surveyPresetLoaded = false
    private var surveySearchLoaded = false

    val callBackActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val painSuccess = result.data?.getBooleanExtra("painSuccess", false)
            if(painSuccess == true) painInfoViewModel.requestPainInfoList(userId,dateTime)

            setContent()
            setFunction()
            networkStatus()
        }
    }
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMyExerciseBinding {
        return FragmentMyExerciseBinding.inflate(inflater, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setContent()
        setFunction()
        networkStatus()
    }

    private fun init(){
        showBottomNavigation()
        hideTopBar()

        Preferences.init(this.requireContext(), Preferences.DB_USER_INFO)
        groupId = Preferences.string(Preferences.KEY_GROUP_ID)
        userId = Preferences.string(Preferences.KEY_USER_ID)
        organizationId = Preferences.string(Preferences.KEY_ORGANIZATION_ID)

        customGroupSpinner = fragmentGroupMemberBinding.spinnerGroup
        customUserSpinner = fragmentGroupMemberBinding.spinnerUser

        val formattedDate = DateTimeUtil.getCurrentFormattedDate()
        fragmentGroupMemberBinding.tvGroupDate.text = formattedDate
        dateTime = fragmentGroupMemberBinding.tvGroupDate.text.toString().replace(".", "-")

        groupViewModel.getSelectedGroup(groupId)
        groupViewModel.requestGroupUser(groupId)

    }
    override fun setContent() {

        trainingViewModel.getTrainingGroupStatus(organizationId,userId,groupId,"training",dateTime)
        trainingViewModel.getTrainingOverallUser(userId, dateTime)
        nutritionViewModel.searchNutritionPicture(userId,dateTime)
        painInfoViewModel.requestPainInfoList(userId,dateTime)
        surveyViewModel.requestSurveyPresetBy(userId)
        surveyViewModel.requestSurveySearch(userId,dateTime)

        nutritionGroupMemberStatusDetailAdater = GroupMemberNutritionPictureAdapter(nutritionUserStatusList)

        fragmentGroupMemberBinding.rvNutrition.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = nutritionGroupMemberStatusDetailAdater
        }
    }

    override fun setFunction() {
        refreshing()

        dateTime = fragmentGroupMemberBinding.tvGroupDate.text.toString().replace(".", "-")

        fragmentGroupMemberBinding.imgLeftArrow.setOnClickListener {
            DateTimeUtil.navigateToPreviousDay()
            updateDateText()
        }

        fragmentGroupMemberBinding.imgRightArrow.setOnClickListener {
            DateTimeUtil.navigateToNextDay()
            updateDateText()
        }

        fragmentGroupMemberBinding.tvGroupDate.setOnClickListener {
            DateTimeUtil.showDatePickerDialog(requireContext(), fragmentGroupMemberBinding.tvGroupDate.text.toString()) { newSelectedDate ->
                fragmentGroupMemberBinding.tvGroupDate.text = newSelectedDate
                dateTime = newSelectedDate.replace(".", "-")

                trainingViewModel.getTrainingGroupStatus(organizationId,userId,groupId,"training",dateTime)
                trainingViewModel.getTrainingOverallUser(userId, dateTime)
                nutritionViewModel.searchNutritionPicture(userId,dateTime)
                painInfoViewModel.requestPainInfoList(userId,dateTime)
                surveyViewModel.requestSurveyPresetBy(userId)
                surveyViewModel.requestSurveySearch(userId,dateTime)
            }
        }

        fragmentGroupMemberBinding.progressBar.setOnClickListener {
            val intent = Intent(context, TrainingConfirmMyActivity::class.java)
            intent.putExtra("selectedGroup", groupInfo)
            intent.putExtra("userId", userId)
            intent.putExtra("dateTime", dateTime)
            startActivity(intent)
        }

        fragmentGroupMemberBinding.tvCommentEnroll.setOnClickListener {
            val intent = Intent(context, NutritionCommentDetailMyActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("dateTime", dateTime)
            callBackActivityResultLauncher.launch(intent)
        }

        fragmentGroupMemberBinding.tvPainInput.setOnClickListener {
            val intent = Intent(context, PainActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("dateTime", dateTime)
            callBackActivityResultLauncher.launch(intent)
        }

        fragmentGroupMemberBinding.surveyEnroll.setOnClickListener {

            if(surveyPresetList.isNullOrEmpty()){
                Global.showBottomSnackBar(fragmentGroupMemberBinding.root, "등록된 설문 항목이 없습니다.")
                return@setOnClickListener
            }

            val positiveButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            }
            val surveyInsertListener = object : SurveyInsertListener {
                override fun onSurveyInsertSelected(surveyInsertList: MutableList<SurveyInsert>) {
                    surveyViewModel.requestSurveyInsert(surveyInsertList)
                }
            }
            val dialogFragment = CustomSurveyInsertDialog.newInstance(surveyInsertListener,positiveButtonClickListener,surveyPresetList,userId,dateTime)
            dialogFragment.show(childFragmentManager, CustomIngredientDialogFragment.TAG)
        }
//        GroupInfo(groupId=GA04,groupNameShort=Sports2i, groupNameLong=스포츠투아이, capacity='0')
//        GroupInfo(groupId=GA04,groupNameShort=Sports2i, groupNameLong=null, capacity='0')
        fragmentGroupMemberBinding.tvStatisticsMore.setOnClickListener {
            dateTime = fragmentGroupMemberBinding.tvGroupDate.text.toString().replace(".", "-")
            val intent = Intent(context, StatisticsActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("selectedGroup", groupInfo)
            intent.putExtra("dateTime", dateTime)
            startActivity(intent)
        }
    }

    override fun refreshing() {

        fragmentGroupMemberBinding.refreshLayout?.setOnRefreshListener {
            fragmentGroupMemberBinding.refreshLayout?.isRefreshing = false

            trainingViewModel.getTrainingGroupStatus(organizationId,userId,groupId,"training",dateTime)
            trainingViewModel.getTrainingOverallUser(userId, dateTime)
            nutritionViewModel.searchNutritionPicture(userId,dateTime)
            painInfoViewModel.requestPainInfoList(userId,dateTime)
            surveyViewModel.requestSurveyPresetBy(userId)
            surveyViewModel.requestSurveySearch(userId,dateTime)
        }
    }

    override fun networkStatus() {

        groupViewModel.selectedGroupInfoState.observe(viewLifecycleOwner) { selectedGroupInfoState ->
            when (selectedGroupInfoState) {
                is NetworkState.Success -> handlerSelectedGroupInfoSuccess(selectedGroupInfoState.data.data)
                is NetworkState.Error -> handlerError(selectedGroupInfoState.message)
                is NetworkState.Loading -> handlerLoading(selectedGroupInfoState.isLoading)
            }
        }

        groupViewModel.groupUserState.observe(viewLifecycleOwner) { groupUserState ->
            when (groupUserState) {
                is NetworkState.Success -> {
                    handlerGroupUserSuccess(groupUserState.data.data)
                    groupUserLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(groupUserState.message)
                    groupUserLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(groupUserState.isLoading)
            }
        }


        trainingViewModel.trainingExerciseListState.observe(viewLifecycleOwner) { trainingExerciseListState ->
            when (trainingExerciseListState) {
            is NetworkState.Success -> {
                val userExerciseList = trainingExerciseListState.data.first().exerciseList ?: emptyList() // 데이터가 null이면 빈 리스트를 사용
                trainingExerciseList.clear()
                trainingExerciseList.addAll(userExerciseList)
                trainingOverallSearchUI(trainingExerciseList)

            val trainingOverallPerformanceIndex = trainingExerciseListState.data.first().trainingOverallData?.performanceIndex ?: 0
            fragmentGroupMemberBinding.progressBar.progress = trainingOverallPerformanceIndex.toInt()
            fragmentGroupMemberBinding.tvAchievementPercent.text = trainingOverallPerformanceIndex.toString() + "%"

            }
            is NetworkState.Error -> handlerError(trainingExerciseListState.message)
            is NetworkState.Loading -> handlerLoading(trainingExerciseListState.isLoading)

        }
    }

        trainingViewModel.trainingOverallUserState.observe(viewLifecycleOwner) { trainingOverallUserState ->
            when (trainingOverallUserState) {
                is NetworkState.Success -> {
                    handlerTrainingOverallUserSuccess(trainingOverallUserState.data.data)
                    trainingOverallUserLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(trainingOverallUserState.message)
                    trainingOverallUserLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> {
                    handlerLoading(trainingOverallUserState.isLoading)
                }
            }
        }


        nutritionViewModel.searchNutritionPictureState.observe(viewLifecycleOwner){ nutritionPictureUserState ->
            when(nutritionPictureUserState){
                is NetworkState.Success -> {
                    handlerNutritionPictureUserSuccess(nutritionPictureUserState.data.data)

                    nutritionPictureUserLoaded = true
                    updateLoadingState()

                }
                is NetworkState.Error -> {
                    handlerError(nutritionPictureUserState.message)

                    nutritionPictureUserLoaded = true
                    updateLoadingState()

                    if(nutritionUserStatusList.isNullOrEmpty()){
                        fragmentGroupMemberBinding.tvNutritionEmpty.visibility = View.VISIBLE
                    }else{
                        fragmentGroupMemberBinding.tvNutritionEmpty.visibility = View.GONE
                    }
                }
                is NetworkState.Loading -> handlerLoading(nutritionPictureUserState.isLoading)
            }
        }

        painInfoViewModel.painInfoListState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Success -> {
                    handlePainSuccess(it.data.data)
                    painLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(it.message)
                    painLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
        }

        surveyViewModel.surveyPresetByState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Success -> {
                    handleSurveyPresetSuccess(it.data.data)
                    surveyPresetLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(it.message)
                    surveyPresetLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
        }

        surveyViewModel.surveySearchState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Success -> {
                    handleSurveySearchSuccess(it.data.data)
                    surveySearchLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Error -> {
                    handlerError(it.message)
                    surveySearchLoaded = true
                    updateLoadingState()
                }
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
        }

        surveyViewModel.surveyInsertState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Success -> { surveyViewModel.requestSurveySearch(userId,dateTime) }
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
        }
    }

    private fun handlerSelectedGroupInfoSuccess(groupInfos: GroupInfo) {
        groupInfo = groupInfos
        fragmentGroupMemberBinding.spinnerGroup.text = groupInfos.groupNameShort
    }

    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
        userList = groupUsers
        userList.map {
            if(it.userId == userId){
                fragmentGroupMemberBinding.spinnerUser.text = it.userName
            }
        }
    }

    private fun trainingOverallSearchUI(trainingOverallSearch: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>){

        val filterT1 = trainingOverallSearch.filter { it.trainingTime == "T1" }
        val filterT2 = trainingOverallSearch.filter { it.trainingTime == "T2" }
        val filterT3 = trainingOverallSearch.filter { it.trainingTime == "T3" }
        val filterT4 = trainingOverallSearch.filter { it.trainingTime == "T4" }
        val filterT5 = trainingOverallSearch.filter { it.trainingTime == "T5" }

        val exerciseViewPager = fragmentGroupMemberBinding.exerciseViewPager
//        exerciseViewPager.isUserInputEnabled = false // 스크롤시 페이지 안넘어가게

        tabAdapter = ExerciseTimeTabMyAdapter(this)
        tabAdapter.addFragment(ExerciseTimeMyFragment.newInstance(filterT1 as MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>), getString(R.string.dawn))
        tabAdapter.addFragment(ExerciseTimeMyFragment.newInstance(filterT2 as MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>), getString(R.string.morning))
        tabAdapter.addFragment(ExerciseTimeMyFragment.newInstance(filterT3 as MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>), getString(R.string.afternoon))
        tabAdapter.addFragment(ExerciseTimeMyFragment.newInstance(filterT4 as MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>), getString(R.string.dinner))
        tabAdapter.addFragment(ExerciseTimeMyFragment.newInstance(filterT5 as MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>), getString(R.string.night))

        // 프래그먼트 아이템 클릭시 리스너를 통해 상세화면으로 이동
        for(i in 0 until tabAdapter.itemCount){
            val exetciseFragment = tabAdapter.getFragment(i) as ExerciseTimeMyFragment
            exetciseFragment.exerciseItemClickListener = object :
                ExerciseTimeMyFragment.OnExerciseItemClickListener {
                override fun onExerciseItemClicked(exerciseInfo: TrainingGroupStatus.TrainingGroupStatusExercise) {
                    val intent = Intent(context, TrainingExerciseDetailMyActivity::class.java)
                    intent.putExtra("userId", userId)
                    intent.putExtra("trainingTime", exerciseInfo.trainingTime)
                    intent.putExtra("exerciseName", exerciseInfo.exerciseName)
                    intent.putExtra("exerciseId", exerciseInfo.exerciseId)
                    intent.putExtra("dateTime", exerciseInfo.trainingDate)
                    callBackActivityResultLauncher.launch(intent)
                }
            }
        }

        exerciseViewPager.adapter = tabAdapter

        tabLayout = fragmentGroupMemberBinding.exerciseTabLayout

        TabLayoutMediator(tabLayout, exerciseViewPager) { tab, position ->
            tab.text = tabAdapter.getTabTitle(position) // 탭 타이틀 설정
        }.attach()

        // 기존에 초기화한 customTabViews를 사용하도록 수정
        if (customTabViews.isEmpty()) tabLayoutTextColor(tabLayout)

        fragmentGroupMemberBinding.exerciseTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = fragmentGroupMemberBinding.exerciseTabLayout.selectedTabPosition
                val selectedTabView = customTabViews[position]
                val selectedTabText = selectedTabView.findViewById<TextView>(R.id.tabText)
                selectedTabText.setTextColor(resources.getColor(R.color.black))
                selectedTabText.typeface = Typeface.DEFAULT
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                val unselectedTabView = customTabViews[position]
                val unselectedTabText = unselectedTabView.findViewById<TextView>(R.id.tabText)

                unselectedTabText.setTextColor(resources.getColor(R.color.gray))
                unselectedTabText.typeface = Typeface.DEFAULT
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Tab reselected, you can handle this if needed
            }
        })
    }
    private fun handlerTrainingOverallUserSuccess(trainingOverall: MutableList<TrainingOverall>) {

        fragmentGroupMemberBinding.tvInjuryRisk.text = (trainingOverall[0].injuryIndex!! * 100).toInt().toString() + "%"
        fragmentGroupMemberBinding.tvSleep.text = trainingOverall[0].sleepIndex.toString()
        fragmentGroupMemberBinding.tvSleepSmall.text = trainingOverall[0].sleepIndex.toString()
        fragmentGroupMemberBinding.tvSleepDuration.text = trainingOverall[0].sleepDuration.toString() + "h"
    }

    private fun handlerNutritionPictureUserSuccess(nutritionPictureUser: MutableList<NutritionPictureUser>) {
        nutritionUserStatusList.clear()
        nutritionUserStatusList.addAll(nutritionPictureUser)

        if(nutritionUserStatusList.isNullOrEmpty()){
            fragmentGroupMemberBinding.tvNutritionEmpty.visibility = View.VISIBLE
        }else{
            fragmentGroupMemberBinding.tvNutritionEmpty.visibility = View.GONE
        }

        nutritionGroupMemberStatusDetailAdater = GroupMemberNutritionPictureAdapter(nutritionUserStatusList)
        fragmentGroupMemberBinding.rvNutrition.adapter = nutritionGroupMemberStatusDetailAdater

        var nutritionEvaluation = 0.0
        nutritionUserStatusList.forEach {
            nutritionEvaluation += it.evaluation
        }
        val averageEvaluation = nutritionEvaluation / nutritionUserStatusList.size
        val averageEvaluationText = String.format("%.1f", nutritionEvaluation / nutritionUserStatusList.size.toDouble())

        if(averageEvaluation.toString().equals("NaN") || averageEvaluation.toString().equals("Infinity")) {
            fragmentGroupMemberBinding.tvNutritionValue.text = "0"
            fragmentGroupMemberBinding.ivNutritionValue.setImageResource(R.drawable.ic_x_36x36)
            fragmentGroupMemberBinding.tvNutritionValue.setBackgroundResource(R.color.background_color2)
        }
        else fragmentGroupMemberBinding.tvNutritionValue.text = averageEvaluationText

        if(averageEvaluation < 2 || fragmentGroupMemberBinding.tvNutritionValue.text.equals("0")){
            fragmentGroupMemberBinding.ivNutritionValue.setImageResource(R.drawable.ic_x_36x36)
            fragmentGroupMemberBinding.tvNutritionValue.setBackgroundResource(R.color.background_color2)
        }
        else if(averageEvaluation > 2 && averageEvaluation < 4){
            fragmentGroupMemberBinding.ivNutritionValue.setImageResource(R.drawable.ic_t_36x36)
            fragmentGroupMemberBinding.tvNutritionValue.setBackgroundResource(R.color.background_color2)
        }
        else if (averageEvaluation <= 5){
            fragmentGroupMemberBinding.ivNutritionValue.setImageResource(R.drawable.ic_o_36x36)
            fragmentGroupMemberBinding.tvNutritionValue.setBackgroundResource(R.color.background_color2)
        }

        nutritionGroupMemberStatusDetailAdater.mListener = object : GroupMemberNutritionPictureAdapter.OnItemClickListener{
            override fun onNutritionGroupMemberStatusClicked(
                position: Int,
                nutrition: NutritionPictureUser
            ) {
                val intent = Intent(context, NutritionCommentDetailMyActivity::class.java)
                intent.putExtra("selectedGroup", groupInfo)
                intent.putExtra("userId", userId)
                intent.putExtra("dateTime", dateTime)
                callBackActivityResultLauncher.launch(intent)
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun handlePainSuccess(painInfoList: MutableList<PainInfo>) {
        binding.layoutContainer.removeAllViews()
        if (binding.layoutSampleFront.childCount > 1) {
            for (i in 1 until binding.layoutSampleFront.childCount) {
                binding.layoutSampleFront.removeAllViews()
                val imageView = AppCompatImageView(requireContext())
                imageView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.mipmap.front))
                binding.layoutSampleFront.addView(imageView)
            }
        }
        if (binding.layoutSampleBack.childCount > 1) {
            for (i in 1 until binding.layoutSampleBack.childCount) {
                binding.layoutSampleBack.removeAllViews()
                val imageView = AppCompatImageView(requireContext())
                imageView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.mipmap.back))
                binding.layoutSampleBack.addView(imageView)
            }
        }
        for (i in 0 until painInfoList.size) {
            this.painMap[painInfoList[i].id] = painInfoList[i]
            if (painInfoList[i].painLocation == "F") binding.layoutSampleFront.addView(painIcon(painInfoList[i].painLevel, painInfoList[i].locationX, painInfoList[i].locationY))
            else binding.layoutSampleBack.addView(painIcon(painInfoList[i].painLevel, painInfoList[i].locationX, painInfoList[i].locationY))
            val cardView = CardView(requireContext())
            var layoutParams = LinearLayoutCompat.LayoutParams((300 * resources.displayMetrics.density).toInt(), (80 * resources.displayMetrics.density).toInt())
            layoutParams.marginStart = (12 * resources.displayMetrics.density).toInt()
            cardView.layoutParams = layoutParams
            cardView.radius = 20.0F
            cardView.useCompatPadding = true
            val container = RelativeLayout(requireContext())
            container.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val layout = LinearLayoutCompat(requireContext())
            layout.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            layout.orientation = LinearLayoutCompat.HORIZONTAL
            val view = View(requireContext())
            layoutParams = LinearLayoutCompat.LayoutParams((10 * resources.displayMetrics.density).toInt(), (10 * resources.displayMetrics.density).toInt())
            layoutParams.marginStart = (12 * resources.displayMetrics.density).toInt()
            layoutParams.topMargin = (12 * resources.displayMetrics.density).toInt()
            view.layoutParams = layoutParams
            view.setBackgroundResource(R.drawable.round_dot)
            (view.background as GradientDrawable).setColor(ContextCompat.getColor(requireContext(), ColorUtil.PainColor(painInfoList[i].painLevel)))
            layout.addView(view)

            val textViewPainLevel = AppCompatTextView(requireContext())
            val textParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            textParams.marginStart = (4 * resources.displayMetrics.density).toInt() // 텍스트와 도트 사이 여백 조정
            textParams.topMargin = (6 * resources.displayMetrics.density).toInt()
            textViewPainLevel.layoutParams = textParams
            textViewPainLevel.setTextAppearance(R.style.text_roboto_R3)
            textViewPainLevel.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            textViewPainLevel.text = painInfoList[i].painLevel.toString()+"점"
            layout.addView(textViewPainLevel)

            var textView = AppCompatTextView(requireContext())
            layoutParams = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            layoutParams.marginStart = (9 * resources.displayMetrics.density).toInt()
            layoutParams.topMargin = (7 * resources.displayMetrics.density).toInt()
            textView.layoutParams = layoutParams
            textView.text = painInfoList[i].comment
            layout.addView(textView)
            container.addView(layout)
            cardView.addView(container)
            binding.layoutContainer.addView(cardView)
        }
    }

    private fun handleSurveyPresetSuccess(surveyPreset: MutableList<SurveyPreset>) {
        surveyPresetList.clear()
        surveyPresetList.addAll(surveyPreset)
    }

    private fun handleSurveySearchSuccess(surveySearch: MutableList<SurveySearch>) {
        val graphLayout = fragmentGroupMemberBinding.graphLayout
        var surveyEnroll = fragmentGroupMemberBinding.surveyEnroll
        var surveyGraph: List<Graph>? = null

        if(surveySearch.isNullOrEmpty()){
            surveyEnroll.visibility = View.VISIBLE
            val surveySearchRandom = mutableListOf<SurveySearch>()
            val surveyRandomItemName = listOf("피로도", "부상도", "포만감", "만족도", "성취도", "피로도", "부상도") // 설문하지 않았을때 배경 그래프적용
            val randomValue = ArrayList<Int>()

            for(i in 0 until 7){
                randomValue.add((3..10).random())
                surveySearchRandom.add(SurveySearch(i, "random", "", "ss"+i, surveyRandomItemName[i], randomValue[i], "0"))
                surveyGraph = extractSurveyGraph(surveySearchRandom)
            }

            fun updateGraphList(selectedList: List<Graph>) {
                graphLayout.removeAllViews()
                graphLayout.addView(
                    GraphTrainingOverall(
                        requireContext(),
                        1,
                        "itemName",
                        selectedList
                    )
                )
            }
            updateGraphList(surveyGraph ?: emptyList())

            graphLayout.alpha = 0.3f // 투명도 값 (0.0에서 1.0 사이)

        }else {
            surveyEnroll.visibility = View.GONE
            for (i in 0 until surveySearch.size) {
                surveyGraph = extractSurveyGraph(surveySearch)
            }

            fun updateGraphList(selectedList: List<Graph>) {
                graphLayout.removeAllViews()
                graphLayout.addView(
                    GraphTrainingOverall(
                        requireContext(),
                        1,
                        "itemName",
                        selectedList
                    )
                )
            }

            updateGraphList(surveyGraph ?: emptyList())

            graphLayout.alpha = 1f // 투명도 값 (0.0에서 1.0 사이)


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
            binding.progressLoadingBar.visibility = View.VISIBLE
        } else {
            binding.llProgressBar.visibility = View.GONE
            binding.progressLoadingBar.visibility = View.GONE
        }
    }

    private fun updateLoadingState() {

//        if (groupUserLoaded && trainingOverallUserLoaded && nutritionPictureUserLoaded
//            && painLoaded && surveyPresetLoaded && surveySearchLoaded) {
//            // 모든 통신이 완료되었을 때 로딩 상태를 업데이트.
//            handlerLoading(false)
//        }
        if (nutritionPictureUserLoaded
            && painLoaded && surveyPresetLoaded && surveySearchLoaded) {
            // 모든 통신이 완료되었을 때 로딩 상태를 업데이트.
            handlerLoading(false)
        }
    }

    private fun extractSurveyGraph(surveySearch: MutableList<SurveySearch>): List<Graph> {
        return surveySearch.map {
            Graph(userName = it.surveyItemName, trainingDate = it.surveyDate, value = it.surveyValue.toInt() * 10 ?: 0)
        }
    }

    private fun updateDateText() {
        val formattedDate = DateTimeUtil.getCurrentFormattedDate()
        fragmentGroupMemberBinding.tvGroupDate.text = formattedDate
        dateTime = formattedDate.replace(".", "-")

        val calendar: Calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)

        if (formattedDate == currentDate) fragmentGroupMemberBinding.imgLeftArrow.visibility = View.VISIBLE
        else fragmentGroupMemberBinding.imgLeftArrow.visibility = View.VISIBLE

        trainingViewModel.getTrainingGroupStatus(organizationId,userId,groupId,"training",dateTime)
        nutritionViewModel.searchNutritionPicture(userId,dateTime)
        painInfoViewModel.requestPainInfoList(userId,dateTime)
        surveyViewModel.requestSurveyPresetBy(userId)
        surveyViewModel.requestSurveySearch(userId,dateTime)
    }

    private fun tabLayoutTextColor(tabLayout: TabLayout) {
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

    private fun painIcon(level: Int, x: Float, y: Float): AppCompatImageView {
        this.painX = x
        this.painY = y
        val imageView = AppCompatImageView(requireContext())
        imageView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.mipmap.touch_point))
        imageView.setColorFilter(ContextCompat.getColor(requireContext(),
            ColorUtil.PainColor(level)
        ))
        imageView.x = x
        imageView.y = y
        return imageView
    }


    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    interface SurveyInsertListener {
        fun onSurveyInsertSelected(surveyInsertList: MutableList<SurveyInsert>)
    }
}
