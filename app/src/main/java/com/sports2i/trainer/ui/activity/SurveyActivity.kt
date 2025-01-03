package com.sports2i.trainer.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.Survey
import com.sports2i.trainer.data.model.SurveyItemList
import com.sports2i.trainer.databinding.ActivitySurveyBinding
import com.sports2i.trainer.interfaces.EditListener
import com.sports2i.trainer.interfaces.NoticeListener
import com.sports2i.trainer.interfaces.TypeListener
import com.sports2i.trainer.ui.adapter.group.TypeAdapter
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.CustomSpinnerAdapter
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.ui.widget.SurveyItemView
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.SurveyViewModel
import com.sports2i.trainer.viewmodel.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.set

@AndroidEntryPoint
class SurveyActivity: BaseActivity<ActivitySurveyBinding>({ActivitySurveyBinding.inflate(it)}), TypeListener, EditListener, NoticeListener {
    private lateinit var customSpinner: CustomSpinner
    private val surveyViewModel: SurveyViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()
    private var inputMethod: InputMethodManager? = null
    private val typeList = ArrayList<String>()
    private val selectMap = HashMap<Int, Int>()
    private val layoutMap = HashMap<Int, LinearLayoutCompat>()
    private var index = 0
    private var count = 0
    private var isCheck = false

    private var surveyList = mutableListOf<Survey>()

    var selectedGroup : GroupInfo? = null
    var selectedDateTime: String? = ""

    private var surveyItemSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        observe()
        changeText()
        clickBack()
        clickAdd()
        clickEdit()
        clickSelect()
        clickRequest()

        this@SurveyActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }
    override fun onType(layout: LinearLayoutCompat, position: Int) {
        var num = position
        if (num != 0) num += num * 7
        layout.removeAllViews()

        val itemCount = (position + 1) * 8
        val params = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)

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

    override fun onEdit(cardView: CardView) {
        this.index = binding.layoutContainer.size
        this.count += when { (cardView.getChildAt(0) as RelativeLayout).id != -1 -> -1 else -> 0 }
        this.surveyViewModel.requestSurveyItemDelete(cardView.tag.toString())
        binding.layoutContainer.removeView(cardView)
        attributeRequest()
        if (binding.layoutContainer.size == 0) {
            binding.txtEdit.setTextColor(ContextCompat.getColor(this, R.color.round_color))
            Handler(mainLooper).postDelayed({
                binding.txtEmpty.visibility = View.VISIBLE
            }, 350)
        }
    }

    override fun onNotice(num: Int) {
        this.count += num
        attributeRequest()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(surveyItemSuccess) {
                val resultIntent = Intent()
                resultIntent.putExtra("surveySuccess", true)
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
            back()
        }
    }

    private fun clickBack() {
        binding.layoutBack.setOnClickListener {
            if(surveyItemSuccess) {
                val resultIntent = Intent()
                resultIntent.putExtra("surveySuccess", true)
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
            back()
        }
    }

    private fun observe() {
        this.groupViewModel.groupInfoState.observe(this) {
            when (it) {
                is NetworkState.Success -> {
                    handlerGroupInfoSuccess(it.data.data)
                    // selectedGroup이 null이 아닌 경우, 스피너에서 해당 그룹을 선택합니다.
                    if (selectedGroup != null) {
                        val groupAdapter = customSpinner.adapter as CustomSpinnerAdapter<GroupInfo>
                        val groupIndex = groupAdapter.getPosition(selectedGroup)
                        binding.spinnerGroup.setSelection(groupIndex)
                    }
                }
                is NetworkState.Error -> handleError(it.message)
                is NetworkState.Loading -> hold()
            }
        }
        this.groupViewModel.groupUserState.observe(this) {
            when (it) {
                is NetworkState.Success -> handlerGroupUserSuccess(it.data.data)
                is NetworkState.Error -> handleError(it.message)
                is NetworkState.Loading -> hold()
            }
        }
        this.surveyViewModel.surveyItemState.observe(this) {
            when (it) {
                is NetworkState.Success -> handleSuccess(it.data.data)
                is NetworkState.Error -> handleError(it.message)
                is NetworkState.Loading -> hold()
            }
        }

        this.surveyViewModel.surveyPresetState.observe(this) {
            when (it) {
                is NetworkState.Success -> handleSuccess(it.data)
                is NetworkState.Error -> handleError(it.message)
                is NetworkState.Loading -> hold()
            }
        }
    }

    private fun hold() {
//        Global.progressON(this)
    }

    private fun clickEdit() {
        binding.txtEdit.setOnClickListener {
            if (binding.layoutContainer.size == 0) return@setOnClickListener
            else if (binding.editWrite.text.toString() != "") hide()
            if (binding.layoutInput.visibility == View.GONE) {
                binding.layoutInput.visibility = View.VISIBLE
                binding.txtEdit.text = getString(R.string.complete)
                for (i in 0 until binding.layoutContainer.size) {
                    val layout = (binding.layoutContainer.getChildAt(i) as CardView).getChildAt(0) as RelativeLayout
                    layout.getChildAt(1).visibility = View.GONE
                    layout.getChildAt(2).visibility = View.VISIBLE
                    attributeItem(layout, -1)
                }
            }else{
                binding.layoutInput.visibility = View.GONE
                binding.txtEdit.text = getString(R.string.edit)
                for (i in 0 until binding.layoutContainer.size) {
                    val layout = (binding.layoutContainer.getChildAt(i) as CardView).getChildAt(0) as RelativeLayout
                    layout.getChildAt(2).visibility = View.GONE
                    layout.getChildAt(1).visibility = View.VISIBLE
                    attributeItem(layout, layout.id)
                }
            }
        }
    }

    private fun attributeSelect(color: Int) {
        binding.imgSelect.setColorFilter(ContextCompat.getColor(this, color))
        binding.txtSelect.setTextColor(ContextCompat.getColor(this, color))
    }

    private fun handleError(errorMessage: String?) {
        errorMessage?.let {
            Global.showBottomSnackBar(binding.root, it)
        }
    }

    private fun hide() {
        this.inputMethod!!.hideSoftInputFromWindow(binding.editWrite.windowToken, 0)
        binding.editWrite.clearFocus()
        binding.editWrite.setText("")
    }

    private fun clickRequest() {
        binding.txtRequest.setOnClickListener {
            if (this.count == 0 || !this.selectMap.containsValue(1)) return@setOnClickListener
            val surveyItemList = mutableListOf<SurveyItemList>()
            for (i in 0 until binding.layoutContainer.size) {
                val layout = (binding.layoutContainer.getChildAt(i) as CardView).getChildAt(0) as RelativeLayout
                if (layout.id == 1) surveyItemList.add(SurveyItemList(binding.layoutContainer.getChildAt(i).tag.toString(), (layout.getChildAt(0) as AppCompatTextView).text.toString()))
            }
            for (i in selectMap.filterValues { it == 1 }.keys) {
                surveyList.add(Survey(userId = typeList[i].split("/")[0], surveyItemList = surveyItemList))
            }
            surveyItemSuccess = true
            surveyViewModel.requestSurveyPresetInsert(surveyList)
        }
    }

    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
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

    private fun handleSuccess(code: Int) {
        Global.showBottomSnackBar(binding.root, "설문등록이 추가되었습니다")
        finish()
        back()
    }

    private fun clickSelect() {
        binding.layoutSelect.setOnClickListener {
            Log.w("papa", "click")
            if (typeList.size == 0) return@setOnClickListener
            else if (binding.txtSelect.textColors.defaultColor != ContextCompat.getColor(this, R.color.round_color)) {
                attributeSelect(R.color.round_color)
                applyType(0)
            }else{
                attributeSelect(R.color.primary)
                applyType(1)
            }
            attributeRequest()
        }
    }

    private fun attributeItem(layout: RelativeLayout, num: Int) {
        if (num != 1) {
            (layout.getChildAt(0) as AppCompatTextView).setTextColor(ContextCompat.getColor(this, R.color.black))
            layout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }else {
            (layout.getChildAt(0) as AppCompatTextView).setTextColor(ContextCompat.getColor(this, R.color.white))
            layout.setBackgroundResource(R.drawable.gradient_survey_item)
        }
    }

    private fun handleSuccess(surveyList: List<Survey>) {

        if (surveyList.isEmpty()) {
            binding.txtEdit.setTextColor(ContextCompat.getColor(this, R.color.round_color))
            binding.txtEmpty.visibility = View.VISIBLE
            return
        }
        for (i in this.index until surveyList.size) {
            binding.layoutContainer.addView(SurveyItemView(this, surveyList[i].surveyItemId, surveyList[i].surveyItemName, this, this))
        }

//        if (this.index != 0 && this.index < surveyList.size) {
//            Handler(mainLooper).postDelayed({
//                binding.scrollView.fullScroll(NestedScrollView.FOCUS_DOWN)
//            }, 200)
//        }
    }

    private fun changeText() {
        binding.editWrite.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == "") {
                    isCheck = false
                    binding.txtAdd.setBackgroundResource(R.drawable.round_button_block)
                }else if (s.toString() != "" && !isCheck) {
                    isCheck = true
                    binding.txtAdd.setBackgroundResource(R.drawable.round_button_confirm)
                }
            }
        })
    }
    private fun clickAdd() {
        binding.txtAdd.setOnClickListener {
            if (!isCheck) return@setOnClickListener
            else if (binding.txtEmpty.visibility == View.VISIBLE) binding.txtEmpty.visibility = View.GONE
            if (binding.layoutContainer.size == 0) binding.txtEdit.setTextColor(ContextCompat.getColor(this, R.color.primary))
            val newSurveyItemName = binding.editWrite.text.toString().trim()
            // 중복된 이름 체크
            if (isDuplicateSurveyItemName(newSurveyItemName)) {
                // 중복된 이름이 있을 경우 사용자에게 알림
                Global.showBottomSnackBar(binding.root, "중복된 이름입니다.")
                return@setOnClickListener
            }

            this.index = binding.layoutContainer.size
            surveyItemSuccess = true
            this.surveyViewModel.requestSurveyItemInsert(Preferences.string(Preferences.KEY_ORGANIZATION_ID), binding.editWrite.text.toString().trim())
            hide()
        }
    }

    private fun isDuplicateSurveyItemName(newSurveyItemName: String): Boolean {
        for (i in 0 until binding.layoutContainer.size) {
            val layout = (binding.layoutContainer.getChildAt(i) as CardView).getChildAt(0) as RelativeLayout
            val textView = layout.getChildAt(0) as AppCompatTextView
            if (textView.text.toString().trim() == newSurveyItemName) {
                // 중복된 이름이 있을 경우 true 반환
                return true
            }
        }
        // 중복된 이름이 없을 경우 false 반환
        return false
    }

    private fun attributeRequest() {
        if (this.count != 0 && this.selectMap.containsValue(1)) binding.txtRequest.setBackgroundResource(R.drawable.round_button_confirm)
        else binding.txtRequest.setBackgroundResource(R.drawable.round_button_block)
    }

    private fun handlerGroupInfoSuccess(groupInfoList: List<GroupInfo>) {
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort
                if (isDropDown) textView.setTextColor(ContextCompat.getColor(this@SurveyActivity, R.color.black))
                else textView.setTextColor(ContextCompat.getColor(this@SurveyActivity, R.color.white))
            }
        }
        customSpinner.setAdapterData(groupInfoList, itemBinder)
    }

    @SuppressLint("ResourceType")
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

    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)

        selectedGroup = intent.getParcelableExtra("selectedGroup")
        selectedDateTime = intent.getStringExtra("dateTime")

        this.inputMethod = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        this.customSpinner = binding.spinnerGroup
        this.groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))
        this.surveyViewModel.requestSurveyItemBy(Preferences.string(Preferences.KEY_ORGANIZATION_ID))
        this.customSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroupInfo = parent?.getItemAtPosition(position) as GroupInfo
                groupViewModel.requestGroupUser(selectedGroupInfo.groupId)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}