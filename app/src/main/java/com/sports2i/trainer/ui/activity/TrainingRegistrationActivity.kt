package com.sports2i.trainer.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExerciseItem
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.data.model.ExerciseTimeItem
import com.sports2i.trainer.data.model.ExerciseUnit
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.TrainingInfo
import com.sports2i.trainer.databinding.ActivityTrainingRegistrationBinding
import com.sports2i.trainer.interfaces.TypeListener
import com.sports2i.trainer.ui.adapter.group.TypeAdapter
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.CustomSpinnerAdapter
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.DateTimeUtil.getCurrentDate
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.OnSingleClickListener
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*
import com.sports2i.trainer.ui.dialog.DateRangePickerHelper

@AndroidEntryPoint
class TrainingRegistrationActivity : BaseActivity<ActivityTrainingRegistrationBinding>({ActivityTrainingRegistrationBinding.inflate(it)}),
    TypeListener {

//    private lateinit var groupUserAdapter: GroupUserAdapter
    private lateinit var groupExerciseUnitAdapter: com.sports2i.trainer.ui.adapter.group.GroupExerciseUnitAdapter
    private lateinit var dawnAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter
    private lateinit var morningAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter
    private lateinit var afternoonAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter
    private lateinit var dinnerAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter
    private lateinit var nightAdapter : com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter

    private lateinit var groupExerciseItemCustomSpinner: CustomSpinner
    private lateinit var groupExerciseTimeItemCustomSpinner: CustomSpinner
    private lateinit var groupCustomSpinner: CustomSpinner

    private val groupViewModel: GroupViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()

    private var exerciseUnitList: MutableList<ExerciseUnit> = mutableListOf()
    private var exerciseItemList: MutableList<ExerciseItem> = mutableListOf()
    private var exerciseTimeList: MutableList<ExerciseTimeItem> = mutableListOf()
    private var exerciseList: MutableList<TrainingInfo.ExerciseList> = mutableListOf()
    private var trainingInfoList: MutableList<TrainingInfo> = mutableListOf()
    private var exercisePresetList: MutableList<ExercisePreset> = mutableListOf()

    private var unitSize : Int = 1

    private var preSetView: Boolean = false
    private var selectedExercisePresets: ArrayList<ExercisePreset>? = null

    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()

    private var selectedGroup : GroupInfo? = null
    private var selectedDateTime: String? = getCurrentDate()

    private val typeList = ArrayList<String>()
    private val layoutMap = HashMap<Int, LinearLayoutCompat>()
    private val selectMap = HashMap<Int, Int>()
    private var inputMethod: InputMethodManager? = null

    private var dateSelectionView : DateSelectionView? = null
    private val dateRangePickerHelper = DateRangePickerHelper()

    private var requestsCompletedCount = 0 // 요청 완료된 수를 추적하는 변수

    private val callBackActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            preSetView = result.data?.getBooleanExtra("preset",false)!!
            selectedDateTime = result.data?.getStringExtra("selectedDateTime").toString()
            selectedGroup = result.data?.getParcelableExtra("selectedGroup")
            selectedExercisePresets = result.data?.getParcelableArrayListExtra("selectedExercisePresets")
            var selectedTime = result.data?.getStringExtra("selectedDateTime")
            if(selectedTime != null) selectedDateTime = selectedTime

            if(preSetView == true) {
                binding.previewLayout.visibility = View.VISIBLE
                binding.enrollLayout.visibility = View.GONE
                dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: getCurrentDate()) // 초기값 설정
                setPresetContent()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        next()
        init()
        setContent()
        setFunction()
        observe()
        clickBack()
        clickSelect()
        binding.root.post { setLiveData() }
        this@TrainingRegistrationActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onType(layout: LinearLayoutCompat, position: Int) {
        var num = position
        if (num != 0) num += num * 7
        layout.removeAllViews()

        val itemCount = (position + 1) * 8
        val params = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT)

        // 마진 설정
        params.leftMargin = dpToPx(12f)
        params.rightMargin = dpToPx(12f)
        params.topMargin = dpToPx(8f)

        val layoutTop = LinearLayoutCompat(this)
        layoutTop.layoutParams = params
        layoutTop.orientation = LinearLayoutCompat.HORIZONTAL
        val layoutBottom = LinearLayoutCompat(this)
        layoutBottom.layoutParams = params
        layoutBottom.orientation = LinearLayoutCompat.HORIZONTAL

        for (i in num until itemCount) {
            if (i >= this.typeList.size) break
            if (layoutTop.childCount < 4) layoutTop.addView(txtType(i, this.typeList[i]))
            else layoutBottom.addView(txtType(i, this.typeList[i]))
        }

        layout.addView(layoutTop)
        layout.addView(layoutBottom)
        this.layoutMap[position] = layout
    }

    private fun txtType(num: Int, type: String): AppCompatTextView {
        val types = type.split("/")
        val textView = AppCompatTextView(this)

        // 디바이스의 화면 크기에 따라 동적으로 너비를 설정
        val screenWidth = resources.displayMetrics.widthPixels
        val widthRatio = 0.215f // 원하는 비율 값
        val widthFixedValue = (screenWidth * widthRatio).toInt()

        // 디바이스의 가로 크기에 비례하여 높이 설정
        val screenHeight = resources.displayMetrics.heightPixels
        val heightRatio = 0.036f // 원하는 비율 값
        val heightFixedValue = (screenHeight * heightRatio).toInt()

        // 디바이스의 가로 크기에 비례하여 너비 설정
        val params = LinearLayoutCompat.LayoutParams(
            widthFixedValue,
            LinearLayoutCompat.LayoutParams.MATCH_PARENT
        )

        // 마진 설정
        params.leftMargin = dpToPx(4f)
        params.rightMargin = dpToPx(4f)

        textView.tag = types[0]
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER
        textView.maxLines = 1
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.setPadding(dpToPx(12f), dpToPx(4f), dpToPx(12f), dpToPx(4f))

        textView.setOnClickListener {
            this.selectMap[num] = when { this.selectMap[num] != 0 -> 0 else -> 1 }
            attributeType(textView, this.selectMap[num]!!)
        }

        textView.text = types[1]
        attributeType(textView, this.selectMap[num]!!)
        return textView
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    // DateSelectionView 추가 애니메이션 적용
    private fun setLiveData(){
        dateSelectionView = DateSelectionView(this)
        binding.dateSelectionView.addView(dateSelectionView)

//        binding.dateSelectionView.apply {
//            addView(dateSelectionView)
//        }

        dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
        dateSelectionView!!.selectedDate.observe(this, ::handleSelectedDate)

        // 애니메이션을 적용합니다.
        val fadeInAnimation = AlphaAnimation(0f, 1f)
        fadeInAnimation.duration = 200 // 애니메이션 지속 시간을 설정합니다.
        dateSelectionView!!.startAnimation(fadeInAnimation)
    }
    private fun handleSelectedDate(selectedDate: String) {
        selectedDateTime = DateTimeUtil.formatDateSelection(selectedDate)
    }

    fun setContent() {
        groupExerciseTimeItemCustomSpinner = binding.spinnerExerciseTime
        groupExerciseUnitAdapter = com.sports2i.trainer.ui.adapter.group.GroupExerciseUnitAdapter(
            this,
            unitSize,
            exerciseUnitList
        )
        binding.rvExerciseItem.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = groupExerciseUnitAdapter
        }

        // preview Layout
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(calendar.time)

        binding.tvStartDate.text = "$currentDate"
        binding.tvEndDate.text = "$currentDate"

        // RecyclerView 및 어댑터 초기화
        val dawnRecyclerView = binding.rvPriviewDawnExercise
        val morningRecyclerView = binding.rvPriviewMorningExercise
        val afternoonRecyclerView = binding.rvPriviewAfternoonExercise
        val dinnerRecyclerView = binding.rvPriviewDinnerExercise
        val nightRecyclerView = binding.rvPriviewNightExercise

        dawnAdapter =
            com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter(this, mutableListOf())
        morningAdapter =
            com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter(this, mutableListOf())
        afternoonAdapter =
            com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter(this, mutableListOf())
        dinnerAdapter =
            com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter(this, mutableListOf())
        nightAdapter =
            com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter(this, mutableListOf())

        dawnRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        morningRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        afternoonRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        dinnerRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        nightRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        dawnRecyclerView.adapter = dawnAdapter
        morningRecyclerView.adapter = morningAdapter
        afternoonRecyclerView.adapter = afternoonAdapter
        dinnerRecyclerView.adapter = dinnerAdapter
        nightRecyclerView.adapter = nightAdapter
    }

    private fun setPresetContent(){

        if (selectedExercisePresets != null) {
            trainingInfoList.clear()
            exerciseList.clear()

            selectedExercisePresets!!.forEach {
                var trainingInfo = TrainingInfo(
                    it.organizationId,
                    "",
                    "",
                    "",
                    selectedDateTime!!,
                    "",
                    "",
                    it.trainingTime,
                    it.exerciseList
                )

                var exercisePreset = ExercisePreset(
                    it.organizationId,
                    null,
                    null,
                    it.trainingTime,
                    it.exerciseList
                )

                trainingInfoList.add(trainingInfo)
                exercisePresetList.add(exercisePreset)

//                addTrainingInfo(trainingInfo.copy())
                updateUIWithSelectedList(trainingInfoList)

            }
        }
    }

    private fun setFunction() {
        refreshing()

        binding.etExerciseDirect.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
//                    selectedEditTextValues[position] = s.toString()
                val text = s.toString().takeIf { it.isNotEmpty() } ?: "0"
            }
        })

        // 목표추가시 아이템 생성
        binding.btnExerciseGoalAdd.setOnClickListener {
            unitSize++
            val size = groupExerciseUnitAdapter.getItemCount() + 1
            groupExerciseUnitAdapter.addNewItems(size) // 새로운 레이아웃을 추가
        }

        // 목표생성시 미리보기 객체 생성
        binding.btnExerciseCreate.setOnClickListener {
            setTrainingInfo()
            groupExerciseUnitAdapter.resetToInitialState()
            showDialog(resources.getString(R.string.create_training))
        }

        // 목표등록시 선택된 선수들의 훈련등록
        binding.btnExerciseEnroll.setOnClickListener{

            if(!this.selectMap.containsValue(1)){
                Global.showBottomSnackBar(binding.root, resources.getString(R.string.empty_select_user))
                return@setOnClickListener
            }

            if(trainingInfoList.isEmpty()){
                Global.showBottomSnackBar(binding.root, resources.getString(R.string.empty_exercise))
                return@setOnClickListener
            }

            setTrainingRegistration()
            showTrainingRegistrationDialog(resources.getString(R.string.enroll_training))
        }

        // 프리셋 저장하기
        binding.btnSavePreset.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View) {
                if(trainingInfoList.isEmpty()){
                    Global.showBottomSnackBar(binding.root, resources.getString(R.string.empty_exercise))
                    return
                }
                setTrainingPresetSave()
                showDialog(resources.getString(R.string.save_preset_message))
            }
        })

        binding.btnLoadPreset.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View) {
                setTrainingPresetLoad()
            }
        })

        // 미리보기 클릭시 Visible 애니메이션 효과
        binding.btnExercisePreview.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {

                val fadeIn = AnimationUtils.loadAnimation(this@TrainingRegistrationActivity, R.anim.fade_in_animation)
                val fadeOut = AnimationUtils.loadAnimation(this@TrainingRegistrationActivity, R.anim.fade_out_animation)

                // fadeOut 애니메이션을 적용하여 enrollLayout을 사라지게 합니다.
                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) { binding.enrollLayout.visibility = View.GONE }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                binding.enrollLayout.startAnimation(fadeOut)

                // fadeIn 애니메이션을 적용하여 previewLayout을 나타나게 합니다.
                fadeIn.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) { binding.previewLayout.visibility = View.VISIBLE }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                binding.previewLayout.startAnimation(fadeIn)
            }
        })

        // Preview Layout

        binding.tvStartDate.setOnClickListener { datePicker() }
        binding.tvEndDate.setOnClickListener { datePicker() }
        binding.calendarIcon.setOnClickListener { datePicker() }


        val dawnRvLayout = binding.layoutPriviewDawnExercise
        val morningRvLayout = binding.layoutPriviewMorningExercise
        val afternoonRvLayout = binding.layoutPriviewAfternoonExercise
        val dinnerRvLayout = binding.layoutPriviewDinnerExercise
        val nightRvLayout = binding.layoutPriviewNightExercise

        val tvPriviewDawnExerciseEdit = binding.tvPriviewDawnExerciseEdit
        val tvPriviewMorningExerciseEdit = binding.tvPriviewMorningExerciseEdit
        val tvPriviewAfternoonExerciseEdit = binding.tvPriviewAfternoonExerciseEdit
        val tvPriviewDinnerExerciseEdit = binding.tvPriviewDinnerExerciseEdit
        val tvPriviewNightExerciseEdit = binding.tvPriviewNightExerciseEdit

        // Dawn
        deletePriviewItem(dawnAdapter, tvPriviewDawnExerciseEdit, dawnRvLayout, tvPriviewDawnExerciseEdit)
        // Morning
        deletePriviewItem(morningAdapter, tvPriviewMorningExerciseEdit, morningRvLayout, tvPriviewMorningExerciseEdit)
        // Afternoon
        deletePriviewItem(afternoonAdapter, tvPriviewAfternoonExerciseEdit, afternoonRvLayout, tvPriviewAfternoonExerciseEdit)
        // Dinner
        deletePriviewItem(dinnerAdapter, tvPriviewDinnerExerciseEdit, dinnerRvLayout, tvPriviewDinnerExerciseEdit)
        // Night
        deletePriviewItem(nightAdapter, tvPriviewNightExerciseEdit, nightRvLayout, tvPriviewNightExerciseEdit)
    }


    private fun deletePriviewItem(adapter: com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter, editButton: View, recyclerViewLayout: View, buttonTextView: TextView) {
        adapter.mListener = object : com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter.OnDeleteItemClickListener {
            override fun onDeleteExercise(position: Int, trainingInfo: TrainingInfo) {
                val exerciseIdToRemove = trainingInfo.exerciseList[0].exerciseId // 예제에서는 첫 번째 exercise의 exerciseId를 사용합니다.

                // trainingInfoList에서 해당 데이터 삭제
                val trainingInfoToRemove = trainingInfoList.find { it.trainingTime == trainingInfo.trainingTime }
                trainingInfoToRemove?.let {
                    val exerciseList = it.exerciseList
                    val exerciseIdToExerciseListMap = exerciseList.groupBy { it.exerciseId }
                    val filteredExerciseList = exerciseIdToExerciseListMap[exerciseIdToRemove] ?: emptyList()
                    exerciseList.removeAll(filteredExerciseList)
                    if (exerciseList.isEmpty()) {
                        trainingInfoList.remove(it)
                    }
                }

                // exercisePresetList에서 해당 데이터 삭제
                val exercisePresetToRemove = exercisePresetList.find { it.trainingTime == trainingInfo.trainingTime }
                exercisePresetToRemove?.let {
                    val exerciseList = it.exerciseList
                    val exerciseIdToExerciseListMap = exerciseList.groupBy { it.exerciseId }
                    val filteredExerciseList = exerciseIdToExerciseListMap[exerciseIdToRemove] ?: emptyList()
                    exerciseList.removeAll(filteredExerciseList)
                    if (exerciseList.isEmpty()) {
                        exercisePresetList.remove(it)
                    }
                }

                if (adapter.itemCount == 0) {
                    recyclerViewLayout.visibility = View.GONE
                } else {
                    recyclerViewLayout.visibility = View.VISIBLE
                }

                adapter.notifyDataSetChanged()
            }
        }
        editButton.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                adapter.isEditMode = !adapter.isEditMode
                val buttonText = if (adapter.isEditMode) "완료" else "수정"
                buttonTextView.text = buttonText
            }
        })
    }


    // 미리보기 아이템 추가
    private fun setTrainingInfo() {

        val selectedExerciseTime = groupExerciseTimeItemCustomSpinner.selectedItem as ExerciseTimeItem
        val selectedExerciseDate = selectedDateTime
        val selectedExerciseItem = groupExerciseItemCustomSpinner.selectedItem as ExerciseItem
        val selectedExerciseUnits = groupExerciseUnitAdapter.getSelectedValues()
        val selectedExerciseValues = groupExerciseUnitAdapter.getEditTextValues()

        var selectExerciseName = selectedExerciseItem.exerciseName

        val exerciseList = mutableListOf<TrainingInfo.ExerciseList>()

        if(binding.etExerciseDirect.text.toString().isNotEmpty()) {
            selectExerciseName = binding.etExerciseDirect.text.toString()
        }

        for (i in selectedExerciseUnits.indices) {
            val exerciseUnit = selectedExerciseUnits[i]
            val exerciseValueStr = selectedExerciseValues.getOrNull(i) ?: ""
            val exerciseValue = exerciseValueStr.toDoubleOrNull() ?: 0.0

            exerciseList.add(
                TrainingInfo.ExerciseList(
                    selectedExerciseItem.exerciseId,
                    selectExerciseName,
                    exerciseUnit.exerciseUnitId,
                    exerciseUnit.exerciseUnit,
                    exerciseUnit.exerciseUnitName,
                    exerciseValue,
                    0.0
                )
            )
        }

        val trainingTimeId = selectedExerciseTime.timeItemId // 섹션 헤더로 사용

        var trainingInfo = TrainingInfo(
            "",
            "",
            "",
            "",
            selectedExerciseDate!!,
            "",
            "",
            trainingTimeId,
            exerciseList
        )


        var exercisePreset = ExercisePreset(
            "",
            null,
            null,
            trainingTimeId,
            exerciseList
        )


        // 새로운 trainingInfo를 trainingInfoList에 추가
        trainingInfoList.add(trainingInfo)

        exercisePresetList.add(exercisePreset)

        // 추가된 trainingInfo 목록을 미리보기 어댑터에 추가
        addTrainingInfo(trainingInfo.copy())
    }

    // 훈련등록
    private fun setTrainingRegistration() {
        val selectGroupInfo = groupCustomSpinner.selectedItem as GroupInfo
        val selectStartDate = binding.tvStartDate.text.toString().replace(".", "-")
        val selectEndDate = binding.tvEndDate.text.toString().replace(".", "-")

        // 최종적으로 선택된 userList를 기반으로 trainingInfoList를 갱신
        val updatedTrainingInfoList = mutableListOf<TrainingInfo>()
        // groupUser의 userId와 userName을 trainingInfoList에 추가
        for(i in selectMap.filterValues {  it == 1 }.keys){
            var userId = typeList[i].split("/")[0]
            var userName = typeList[i].split("/")[1]
            for (trainingInfo in trainingInfoList) {
                // 각 trainingInfo 객체를 복제하여 사용자 정보 업데이트
                val updatedTrainingInfo = trainingInfo.copy( organizationId = Global.myInfo.organizationId, groupId = selectGroupInfo.groupId,
                    userId = userId , userName = userName, trainingStartDate = selectStartDate , trainingEndDate =  selectEndDate)
                updatedTrainingInfoList.add(updatedTrainingInfo.copy())
            }
        }
        // 훈련등록 api 호출
        this.trainingViewModel.requestTrainingInfo(updatedTrainingInfoList)
    }

    private fun setTrainingPresetSave() {

        // 최종적으로 선택된 userList를 기반으로 trainingInfoList를 갱신
        val updatePresetList = mutableListOf<ExercisePreset>()
        for (preset in exercisePresetList) {
            // 각 trainingInfo 객체를 복제하여 사용자 정보 업데이트
            val updatedPreset = preset.copy( organizationId = Global.myInfo.organizationId)
            updatePresetList.add(updatedPreset.copy())
        }

        this.trainingViewModel.requestExercisePreset(updatePresetList)
    }

    private fun setTrainingPresetLoad(){
        val intent = Intent(this, TrainingPresetActivity::class.java)
        intent.putExtra("selectedGroup", selectedGroup)
        intent.putExtra("selectedDateTime", selectedDateTime)
        callBackActivityResultLauncher.launch(intent)
    }

    fun addTrainingInfo(trainingInfo: TrainingInfo) {
        val timeItemName = trainingInfo.trainingTime
        // RecyclerView Layout
        val dawnRvLayout = binding.layoutPriviewDawnExercise
        val morningRvLayout = binding.layoutPriviewMorningExercise
        val afternoonRvLayout = binding.layoutPriviewAfternoonExercise
        val dinnerRvLayout = binding.layoutPriviewDinnerExercise
        val nightRvLayout = binding.layoutPriviewNightExercise


        when (timeItemName) {
            "T1" -> {
                dawnAdapter.addData(trainingInfo)
                dawnRvLayout.visibility = View.VISIBLE
            }
            "T2" -> {
                morningAdapter.addData(trainingInfo)
                morningRvLayout.visibility = View.VISIBLE
            }
            "T3" -> {
                afternoonAdapter.addData(trainingInfo)
                afternoonRvLayout.visibility = View.VISIBLE
            }
            "T4" -> {
                dinnerAdapter.addData(trainingInfo)
                dinnerRvLayout.visibility = View.VISIBLE
            }
            "T5" -> {
                nightAdapter.addData(trainingInfo)
                nightRvLayout.visibility = View.VISIBLE
            }
            else -> {
                // 처리하지 않은 다른 시간대에 대한 로직 추가
            }
        }
    }

    private fun updateUIWithSelectedList(selectedList: List<TrainingInfo>) {
        // Clear all adapters
        clearAdapters()

        for (trainingInfo in selectedList) {
            val trainingTime = trainingInfo.trainingTime

            // 현재 exercisePreset에서 exerciseId로 그룹화된 exerciseList를 가져옵니다.
            val exerciseIdToExerciseListMap = trainingInfo.exerciseList.groupBy { it.exerciseId }

            when (trainingTime) {
                "T1" -> addDataToAdapter(trainingInfo, dawnAdapter, exerciseIdToExerciseListMap)
                "T2" -> addDataToAdapter(trainingInfo, morningAdapter, exerciseIdToExerciseListMap)
                "T3" -> addDataToAdapter(trainingInfo, afternoonAdapter, exerciseIdToExerciseListMap)
                "T4" -> addDataToAdapter(trainingInfo, dinnerAdapter, exerciseIdToExerciseListMap)
                "T5" -> addDataToAdapter(trainingInfo, nightAdapter, exerciseIdToExerciseListMap)
                else -> {}
            }
        }

        // Update visibility based on item count
        updateVisibility()
    }

    private fun addDataToAdapter(
        trainingInfo: TrainingInfo,
        adapter: com.sports2i.trainer.ui.adapter.group.GroupExercisePreviewAdapter,
        exerciseIdToExerciseListMap: Map<String, List<TrainingInfo.ExerciseList>>
    ) {
        for ((_, groupedExerciseList) in exerciseIdToExerciseListMap) {
            val exercisePresetWithGroupedExerciseList = trainingInfo.copy(exerciseList = groupedExerciseList.toMutableList())
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


    fun refreshing() {
        binding.refreshLayout?.setOnRefreshListener {
//            refresh()
            binding.refreshLayout?.isRefreshing = false
        }
    }

    private fun attributeSelect(color: Int) {
        binding.imgSelect.setColorFilter(ContextCompat.getColor(this, color))
        binding.txtSelect.setTextColor(ContextCompat.getColor(this, color))
    }

    private fun attributeType(textView: AppCompatTextView, num: Int) {
        if (num != 0) {
            textView.setBackgroundResource(R.drawable.round_type_on)
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        }else {
            textView.setBackgroundResource(R.drawable.round_type_off)
            textView.setTextColor(ContextCompat.getColor(this, R.color.primary))
        }
    }

    private fun applyType(num: Int) {
        for (i in 0 until this.selectMap.size) {
            this.selectMap[i] = num
        }
        for (i in 0 until this.layoutMap.size) {
            val layoutTop = this.layoutMap[i]!!.getChildAt(0) as LinearLayoutCompat
            for (i in 0 until layoutTop.size) {
                attributeType(layoutTop.getChildAt(i) as AppCompatTextView, num)
            }
            val layoutBottom = this.layoutMap[i]!!.getChildAt(1) as LinearLayoutCompat
            for (i in 0 until layoutBottom.size) {
                attributeType(layoutBottom.getChildAt(i) as AppCompatTextView, num)
            }
        }
    }

    private fun clickSelect() {
        binding.layoutSelect.setOnClickListener {
            if (typeList.size == 0) return@setOnClickListener
            else if (binding.txtSelect.textColors.defaultColor != ContextCompat.getColor(this, R.color.round_color)) {
                attributeSelect(R.color.round_color)
                applyType(0)
            }else{
                attributeSelect(R.color.primary)
                applyType(1)
            }
        }
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

    private fun observeGroupInfo() = observeNetworkState(
        groupViewModel.groupInfoState,
        onSuccess = { state ->
            handlerGroupInfoSuccess(state.data.data)
            if (selectedGroup != null) {
                val groupAdapter = groupCustomSpinner.adapter as CustomSpinnerAdapter<GroupInfo>
                val groupIndex = groupAdapter.getPosition(selectedGroup)
                binding.spinnerGroup.setSelection(groupIndex)
            }
        },
        onError = { state -> handlerError(state.message) }
    )
    private fun observeGroupUser() = observeNetworkState(
        groupViewModel.groupUserState,
        onSuccess = { state -> handlerGroupUserSuccess(state.data.data) },
        onError = { state -> handlerError(state.message) }
    )

    private fun observeExerciseItem() = observeNetworkState(
        trainingViewModel.exerciseItemState,
        onSuccess = { state -> handlerExerciseItemSuccess(state.data.data) },
        onError = { state -> handlerError(state.message) }
    )

    private fun observeExerciseUnit() = observeNetworkState(
        trainingViewModel.exerciseUnitState,
        onSuccess = { state -> handlerExerciseUnitSuccess(state.data.data) },
        onError = { state -> handlerError(state.message) }
    )

    private fun observeExerciseTimeItem() = observeNetworkState(
        trainingViewModel.exerciseTimeItemState,
        onSuccess = { state -> handlerExerciseTimeItemSuccess(state.data.data) },
        onError = { state -> handlerError(state.message) }
    )

    private fun observe() {
        val networkStateObservers = listOf(
            ::observeGroupInfo,
            ::observeGroupUser,
            ::observeExerciseItem,
            ::observeExerciseUnit,
            ::observeExerciseTimeItem
        )
        networkStateObservers.forEach { observer ->
            observer.invoke()
        }
    }
    private fun handlerGroupInfoSuccess(groupInfos: MutableList<GroupInfo>) {
        Global.progressOFF()
        groupInfoList = groupInfos
        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort
                if (isDropDown) {
                    textView.setTextColor(this@TrainingRegistrationActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@TrainingRegistrationActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        groupCustomSpinner.setAdapterData(groupInfoList, itemBinder)
    }
    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
        Global.progressOFF()
        this.typeList.clear()
        this.selectMap.clear()

        if (binding.viewPager.adapter != null) binding.viewPager.adapter = null

        if (groupUsers.size <= 8) {
            if (binding.indicator.visibility == View.VISIBLE) binding.indicator.visibility = View.INVISIBLE
            if (groupUsers.size == 0) {
                attributeSelect(R.color.round_color)
                return
            }
        }else if (binding.indicator.visibility == View.INVISIBLE) {
            binding.indicator.visibility = View.VISIBLE

        }

        attributeSelect(R.color.primary)

        for (i in 0 until groupUsers.size) {
            this.typeList.add(groupUsers[i].userId + "/" + groupUsers[i].userName)
            this.selectMap[i] = 1
        }

        binding.viewPager.adapter = TypeAdapter(when { this.typeList.size % 8 != 0 -> this.typeList.size / 8 + 1 else -> this.typeList.size / 8 }, this)
        binding.indicator.attachTo(binding.viewPager)

    }
    private fun handlerExerciseItemSuccess(exerciseItem: MutableList<ExerciseItem>) {
        Global.progressOFF()
        exerciseItemList = exerciseItem

        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<ExerciseItem> {
            override fun bindItem(view: View, item: ExerciseItem, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.exerciseName
                textView.setTextColor(this@TrainingRegistrationActivity.resources.getColor(android.R.color.black))
            }
        }
        groupExerciseItemCustomSpinner.setAdapterData(exerciseItemList, itemBinder)

    }

    private fun handlerExerciseUnitSuccess(exerciseUnit: MutableList<ExerciseUnit>) {
        Global.progressOFF()
        exerciseUnitList = exerciseUnit

        groupExerciseUnitAdapter.setUnitSize(unitSize)
        groupExerciseUnitAdapter.notifyDataSetChanged()

        groupExerciseUnitAdapter = com.sports2i.trainer.ui.adapter.group.GroupExerciseUnitAdapter(
            this@TrainingRegistrationActivity,
            unitSize,
            exerciseUnitList
        )
        binding.rvExerciseItem.adapter = groupExerciseUnitAdapter

    }

    private fun handlerExerciseTimeItemSuccess(exerciseTimeItem: MutableList<ExerciseTimeItem>) {
        Global.progressOFF()
        exerciseTimeList = exerciseTimeItem

        val itemBinder = object : ItemBinder<ExerciseTimeItem> {
            override fun bindItem(view: View, item: ExerciseTimeItem, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.timeItemName
                textView.setTextColor(this@TrainingRegistrationActivity.resources.getColor(android.R.color.black))
            }
        }
        groupExerciseTimeItemCustomSpinner.setAdapterData(exerciseTimeList, itemBinder)
    }

    private fun handlerError(errorMessage: String?) {
        Global.progressOFF()
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun showDialog(message: String){
        val alertDialogBuilder = AlertDialog.Builder(this@TrainingRegistrationActivity)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showTrainingRegistrationDialog(message: String){
        val alertDialogBuilder = AlertDialog.Builder(this@TrainingRegistrationActivity)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
//        navigateBackStack(R.id.fragment_group,true)
            finish()
            back()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // "뒤로 가기" 버튼 처리
            if (binding.previewLayout.visibility == View.VISIBLE) {
                binding.previewLayout.visibility = View.GONE
                binding.enrollLayout.visibility = View.VISIBLE
            } else {
                finish()
                back()
            }

        }
    }
    private fun clickBack() {
        binding.layoutBack.setOnClickListener {
            if (binding.previewLayout.visibility == View.VISIBLE) {
                binding.previewLayout.visibility = View.GONE
                binding.enrollLayout.visibility = View.VISIBLE
            } else {
                finish()
                back()
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

    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)

        selectedGroup = intent.getParcelableExtra("selectedGroup")
        selectedDateTime = intent.getStringExtra("dateTime")

        this.inputMethod = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        this.groupCustomSpinner = binding.spinnerGroup
        this.groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))

        this.trainingViewModel.getExerciseItemSearch()
        this.trainingViewModel.getExerciseUnitSearch()
        this.trainingViewModel.getExerciseTimeItemSearch()

        this.groupExerciseItemCustomSpinner = binding.spinnerExerciseName
        this.groupCustomSpinner = binding.spinnerGroup

        this.groupExerciseItemCustomSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedExerciseInfo = parent?.getItemAtPosition(position) as ExerciseItem
                    val selectedExerciseId = selectedExerciseInfo.exerciseId
                    if (selectedExerciseId.equals("E99")) binding.etExerciseDirect.visibility = View.VISIBLE
                    else binding.etExerciseDirect.visibility = View.GONE
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        this.groupCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroupInfo = parent?.getItemAtPosition(position) as GroupInfo
                val selectedGroupId = selectedGroupInfo.groupId
                groupViewModel.requestGroupUser(selectedGroupId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle Nothing Selected
            }
        }

    }

    private fun datePicker(){
        dateRangePickerHelper.showDateRangePicker(
            supportFragmentManager,
            "기간설정"
        ) { startDate, endDate ->
            // 여기에 선택한 날짜에 대한 작업을 수행합니다.
            binding.tvStartDate.text = startDate
            binding.tvEndDate.text = endDate
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Handler(mainLooper).removeCallbacksAndMessages(null) // handler 객체의 모든 딜레이된 작업을 취소
    }

    override fun onStop() {
        super.onStop()
        Handler(mainLooper).removeCallbacksAndMessages(null) // handler 객체의 모든 딜레이된 작업을 취소
    }

}

