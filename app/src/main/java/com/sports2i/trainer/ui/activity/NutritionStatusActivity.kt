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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.Nutrition
import com.sports2i.trainer.databinding.ActivityNutritionStatusBinding
import com.sports2i.trainer.ui.adapter.group.GroupNutritionStatusDetailAdapter
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.CustomSpinnerAdapter
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.NutritionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NutritionStatusActivity : BaseActivity<ActivityNutritionStatusBinding>({ActivityNutritionStatusBinding.inflate(it)}) {

    private val NUTRITION_TYPE = "nutrition"

    private val groupViewModel: GroupViewModel by viewModels()
    private val nutritionViewModel: NutritionViewModel by viewModels()
    private var nutritionGroupStatusList: MutableList<Nutrition> = mutableListOf()
    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()
    private var selectedGroupId: String = ""
    private var trainingDate: String = ""

    private lateinit var nutritionGroupStatusDetailAdater: GroupNutritionStatusDetailAdapter
    private lateinit var groupCustomSpinner: CustomSpinner // CustomSpinner 정의
    var selectedGroup : GroupInfo? = null
    var selectedDateTime: String? = ""
    private var dateSelectionView : DateSelectionView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setFunction()
        observe()
        clickBack()
        binding.root.post { setLiveData() }

        this@NutritionStatusActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
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
        nutritionViewModel.searchNutrition(selectedGroupId,NUTRITION_TYPE,selectedDateTime!!)
    }
    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)

        this.groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))
        this.selectedGroup = intent.getParcelableExtra("selectedGroup")
        this.selectedDateTime = intent.getStringExtra("dateTime")
        this.groupCustomSpinner = binding.spinnerGroup // CustomSpinner 초기화
        this.nutritionGroupStatusDetailAdater = GroupNutritionStatusDetailAdapter(nutritionGroupStatusList)

        binding.rvNutritionStatus.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = nutritionGroupStatusDetailAdater
        }

        groupCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGroup = parent?.getItemAtPosition(position) as GroupInfo
                selectedGroupId = selectedGroup!!.groupId

                nutritionViewModel.searchNutrition(selectedGroupId,NUTRITION_TYPE,selectedDateTime!!)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun setFunction() {
        refreshing()

        binding.tvCommentEnroll.setOnClickListener {
            val selectedGroup = binding.spinnerGroup.selectedItem as GroupInfo
            val intent = Intent(this@NutritionStatusActivity, NutritionCommentRegistrationActivity::class.java)
            intent.putExtra("selectedGroup", selectedGroup)
            intent.putExtra("dateTime", selectedDateTime!!)
            startActivity(intent)
//            finish()
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
                        val groupAdapter = groupCustomSpinner.adapter as CustomSpinnerAdapter<GroupInfo>
                        val groupIndex = groupAdapter.getPosition(selectedGroup)
                        binding.spinnerGroup.setSelection(groupIndex)
                    }
                }
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
        }

        this.nutritionViewModel.searchNutritionState.observe(this){
            when(it){
                is NetworkState.Success -> {
                    handlerNutritionGroupStatusSuccess(it.data.data)
                    Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                }
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading(it.isLoading)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun refreshing() {
        binding.refreshLayout?.setOnRefreshListener {
            binding.refreshLayout?.isRefreshing = false
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
                    textView.setTextColor(this@NutritionStatusActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@NutritionStatusActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        groupCustomSpinner.setAdapterData(groupInfoList, itemBinder)
    }

    private fun handlerNutritionGroupStatusSuccess(nutritionGroupStatus: MutableList<Nutrition>) {
        nutritionGroupStatusList = nutritionGroupStatus

        if(nutritionGroupStatusList.isEmpty()) binding.tvNutritionEmpty.visibility = View.VISIBLE
        else binding.tvNutritionEmpty.visibility = View.GONE

        nutritionGroupStatusDetailAdater = GroupNutritionStatusDetailAdapter(nutritionGroupStatusList)
        binding.rvNutritionStatus.adapter = nutritionGroupStatusDetailAdater

        nutritionGroupStatusDetailAdater.mListener = object : GroupNutritionStatusDetailAdapter.OnItemClickListener{
            override fun onNutritionGroupStatusClicked(
                position: Int,
                nutrition: Nutrition,
                nutritionPicture: Nutrition.NutritionPicture
//                nutritionPicture: NutritionPictureUser
            ) {
                val intent = Intent(this@NutritionStatusActivity, NutritionCommentDetailActivity::class.java)
                intent.putExtra("selectedGroup", selectedGroup)
                intent.putExtra("userId", nutrition.userId)
                intent.putExtra("dateTime", selectedDateTime!!)
                intent.putExtra("selectedNutrition", nutritionPicture)
                startActivity(intent)
            }
        }
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