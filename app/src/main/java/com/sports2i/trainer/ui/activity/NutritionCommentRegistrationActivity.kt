package com.sports2i.trainer.ui.activity

import android.app.AlertDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.NutritionDirection
import com.sports2i.trainer.data.model.NutritionDirectionKeyword
import com.sports2i.trainer.databinding.ActivityNutritionCommentRegistrationBinding
import com.sports2i.trainer.interfaces.TypeListener
import com.sports2i.trainer.ui.adapter.group.GroupUserAdapter
import com.sports2i.trainer.ui.adapter.group.NutritionCommentAdapter
import com.sports2i.trainer.ui.adapter.group.TypeAdapter
import com.sports2i.trainer.ui.dialog.DateRangePickerHelper
import com.sports2i.trainer.ui.view.AnimatedExpandableListView
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.CustomSpinnerAdapter
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.NutritionViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale


@AndroidEntryPoint
class NutritionCommentRegistrationActivity : BaseActivity<ActivityNutritionCommentRegistrationBinding>({ActivityNutritionCommentRegistrationBinding.inflate(it)}),
    TypeListener {

    private lateinit var groupUserAdapter: GroupUserAdapter
    private lateinit var groupCustomSpinner: CustomSpinner

    private val groupViewModel: GroupViewModel by viewModels()
    private val nutritionViewModel: NutritionViewModel by viewModels()

    private var userList: MutableList<GroupUser> = mutableListOf()
    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()

    var selectedGroup : GroupInfo? = null
    var selectedDateTime: String? = ""

    private val typeList = ArrayList<String>()
    private val layoutMap = HashMap<Int, LinearLayoutCompat>()
    private val selectMap = HashMap<Int, Int>()
    private var inputMethod: InputMethodManager? = null

    private val groupData = listOf("탄수화물", "단백질", "지방", "당", "식사량")
    private val childData = listOf(listOf("과다", "부족"), listOf("과다", "부족"), listOf("과다", "부족"), listOf("과다", "부족"), listOf("과다", "부족"))
    private val selectedKeywords = mutableListOf<NutritionDirectionKeyword>()

    private var userIdList = mutableListOf<String>()
    private var dateSelectionView : DateSelectionView? = null
    private val dateRangePickerHelper = DateRangePickerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setContent()
        setFunction()
        observe()
        clickBack()
        clickSelect()
        binding.root.post{
            setLiveData()
        }
        this@NutritionCommentRegistrationActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
    private fun setLiveData(){
        dateSelectionView = DateSelectionView(this)
        binding.dateSelectionView.apply {
            addView(dateSelectionView)
        }
        dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
        dateSelectionView!!.selectedDate.observe(this, ::handleSelectedDate)
    }
    private fun handleSelectedDate(selectedDate: String) {
        selectedDateTime = DateTimeUtil.formatDateSelection(selectedDate)
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

     fun setContent() {

        // preview Layout
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(calendar.time)

        binding.tvStartDate.text = "$currentDate"
        binding.tvEndDate.text = "$currentDate"

         val expandableListView: AnimatedExpandableListView = binding.expandableListView

         val adapter = NutritionCommentAdapter(this, groupData, childData)
         adapter.setOnCheckBoxStateChangedListener(checkBoxStateChangedListener)
         expandableListView.setAdapter(adapter)

//         애니메이션 적용시 너무 렉걸려서 보류중
//         expandableListView.setOnGroupClickListener { parent, v, groupPosition, id ->
//             if (expandableListView.isGroupExpanded(groupPosition)) {
//                 expandableListView.collapseGroupWithAnimation(groupPosition)
//             } else {
//                 expandableListView.expandGroupWithAnimation(groupPosition)
//             }
//             true
//         }
    }


    private val checkBoxStateChangedListener = object : NutritionCommentAdapter.OnCheckBoxStateChangedListener {
        override fun onCheckBoxStateChanged(groupPosition: Int, childPosition: Int, type: String, isChecked: Boolean) {
            val keyword = when (groupPosition) {
                0 -> "탄수화물"
                1 -> "단백질"
                2 -> "지방"
                3 -> "당"
                4 -> "식사량"
                else -> ""
            }

            val keywordList = NutritionDirectionKeyword(keyword, type)

            if (isChecked) {
                // 같은 keyword가 이미 선택되었는지 확인하고, 있으면 이전 선택을 제거
                val existingKeyword = selectedKeywords.find { it.item == keyword }
                if (existingKeyword != null) {
                    selectedKeywords.remove(existingKeyword)
                }
                selectedKeywords.add(keywordList)
            } else {
                // 선택 해제된 경우 해당 항목 제거
                selectedKeywords.removeAll { it.item == keyword }
            }
        }
    }


    private fun setFunction() {
        refreshing()

        groupCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedGroupInfo = parent?.getItemAtPosition(position) as GroupInfo
                val selectedGroupId = selectedGroupInfo.groupId
                groupViewModel.requestGroupUser(selectedGroupId)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle Nothing Selected
            }
        }

        binding.tvStartDate.setOnClickListener {
            dateRangePickerHelper.showDateRangePicker(
                supportFragmentManager,
                "기간설정"
            ) { startDate, endDate ->
                // 여기에 선택한 날짜에 대한 작업을 수행합니다.
                binding.tvStartDate.text = startDate
                binding.tvEndDate.text = endDate
            }
        }

        binding.tvEndDate.setOnClickListener {
            dateRangePickerHelper.showDateRangePicker(
                supportFragmentManager,
                "기간설정"
            ) { startDate, endDate ->
                // 여기에 선택한 날짜에 대한 작업을 수행합니다.
                binding.tvStartDate.text = startDate
                binding.tvEndDate.text = endDate
            }
        }

        binding.etNutrition.setFocusableInTouchMode(true)


        binding.btnNutritionEnroll.setOnClickListener{

                if(!this.selectMap.containsValue(1)){
                    Global.showBottomSnackBar(binding.root, resources.getString(R.string.empty_select_user))
                    return@setOnClickListener
                }
                setNutritionCommentRegistration()
                showNutritionRegistrationDialog(resources.getString(R.string.enroll_nutrition_comment))
        }
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
    private fun setNutritionCommentRegistration(){
        val startDate = binding.tvStartDate.text.toString().replace(".", "-")
        val endDate = binding.tvEndDate.text.toString().replace(".", "-")
        val content = binding.etNutrition.text.toString()

        // groupUser의 userId와 userName을 trainingInfoList에 추가
        for(i in selectMap.filterValues {  it == 1 }.keys){
            var userId = typeList[i].split("/")[0]
            var userName = typeList[i].split("/")[1]
            userIdList.add(userId)
        }

        val nutritionDirection = NutritionDirection(userIdList = userIdList, keywordList = selectedKeywords,content = content, startDate = startDate, endDate = endDate)

        this.nutritionViewModel.insertNutritionDirection(nutritionDirection)
    }

    private fun observe() {
        this.groupViewModel.groupInfoState.observe(this) {
            when (it) {
                is NetworkState.Success -> {
                    handlerGroupInfoSuccess(it.data.data)
//                    // selectedGroup이 null이 아닌 경우, 스피너에서 해당 그룹을 선택합니다.
                    if (selectedGroup != null) {
                        val groupAdapter = groupCustomSpinner.adapter as CustomSpinnerAdapter<GroupInfo>
                        val groupIndex = groupAdapter.getPosition(selectedGroup)
                        binding.spinnerGroup.setSelection(groupIndex)
                    }
                }
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
        }

        this.groupViewModel.groupUserState.observe(this) {
            when (it) {
                is NetworkState.Success -> {
                    handlerGroupUserSuccess(it.data.data)
                    Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                }
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
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
                    textView.setTextColor(this@NutritionCommentRegistrationActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@NutritionCommentRegistrationActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        groupCustomSpinner.setAdapterData(groupInfoList, itemBinder)
    }
    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
        this.typeList.clear()
        this.selectMap.clear()
        userList = groupUsers
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
    private fun showNutritionRegistrationDialog(message: String){
        val alertDialogBuilder = AlertDialog.Builder(this@NutritionCommentRegistrationActivity)
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

    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)
        this.inputMethod = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        this.groupCustomSpinner = binding.spinnerGroup
        selectedGroup = intent.getParcelableExtra("selectedGroup")
        selectedDateTime = intent.getStringExtra("dateTime")
        groupUserAdapter = GroupUserAdapter(userList) // 초기에 빈 리스트로 어댑터 초기화
        this.groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))
        this.groupCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroupInfo = parent?.getItemAtPosition(position) as GroupInfo
                groupViewModel.requestGroupUser(selectedGroupInfo.groupId)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
