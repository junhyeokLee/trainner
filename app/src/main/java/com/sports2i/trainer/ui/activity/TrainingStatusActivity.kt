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
import com.sports2i.trainer.data.model.DeleteTrainingGroupStatus
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.TrainingOverallSearch
import com.sports2i.trainer.databinding.ActivityTrainingStatusBinding
import com.sports2i.trainer.ui.adapter.group.GroupTrainingStatusDetailAdapter
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.CustomSpinnerAdapter
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.OnSingleClickListener
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingStatusActivity : BaseActivity<ActivityTrainingStatusBinding>({ActivityTrainingStatusBinding.inflate(it)}) {

    private val groupViewModel: GroupViewModel by viewModels()
    private val trainingViewModel: TrainingViewModel by viewModels()
    private var trainingGroupStatusList: MutableList<TrainingOverallSearch> = mutableListOf()
    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()
    private var selectedGroupId: String = ""
    private var trainingDate: String = ""

    private lateinit var trainingGroupStatusDetailAdater: GroupTrainingStatusDetailAdapter
    private lateinit var groupCustomSpinner: CustomSpinner // CustomSpinner 정의
    var selectedGroup : GroupInfo? = null
    var selectedDateTime: String? = ""
    private var dateSelectionView : DateSelectionView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        observe()
        setFunction()
        clickBack()
        binding.root.post { setLiveData() }
        this@TrainingStatusActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
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
        trainingViewModel.getTrainingGroupStatus(Global.myInfo.organizationId,selectedGroupId,"training",selectedDateTime!!)
    }

    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)

        this.groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))
        this.selectedGroup = intent.getParcelableExtra("selectedGroup")
        this.selectedDateTime = intent.getStringExtra("dateTime")
        this.groupCustomSpinner = binding.spinnerGroup // CustomSpinner 초기화
        this.trainingGroupStatusDetailAdater = GroupTrainingStatusDetailAdapter(trainingGroupStatusList)

        binding.rvTrainingStatus.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = trainingGroupStatusDetailAdater
        }
//        trainingViewModel.getTrainingGroupStatus(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroup!!.groupId,"training",selectedDateTime!!)

    }

    private fun setFunction() {
        refreshing()

        groupCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGroup = parent?.getItemAtPosition(position) as GroupInfo
                selectedGroupId = selectedGroup!!.groupId
                trainingViewModel.getTrainingGroupStatus(Preferences.string(Preferences.KEY_ORGANIZATION_ID),selectedGroupId,"training",selectedDateTime!!)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.tvTrainningEnroll.setOnClickListener {
            val selectedGroup = binding.spinnerGroup.selectedItem as GroupInfo
            val intent = Intent(this@TrainingStatusActivity, TrainingRegistrationActivity::class.java)
            intent.putExtra("selectedGroup", selectedGroup)
            intent.putExtra("dateTime", selectedDateTime)
            startActivity(intent)
//            finish()
            back()
        }

        binding.tvEdit.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View) {
                trainingGroupStatusDetailAdater.isEditMode = !trainingGroupStatusDetailAdater.isEditMode
                val buttonText = if (trainingGroupStatusDetailAdater.isEditMode) "완료" else "편집"

                if(buttonText.equals("완료")){
                    binding.tvTrainningEnroll.visibility = View.GONE
                }
                else{
                    binding.tvTrainningEnroll.visibility = View.VISIBLE
                    dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate()) // 초기값 설정
                }
                binding.tvEdit.text = buttonText
            }
        })


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

        this.trainingViewModel.trainingGroupStatusState.observe(this){
            when(it){
                is NetworkState.Success -> {
                    handlerTrainingGroupStatusSuccess(it.data.data)
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
//            refresh()
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
                    textView.setTextColor(this@TrainingStatusActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@TrainingStatusActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        groupCustomSpinner.setAdapterData(groupInfoList, itemBinder)
    }

    private fun handlerTrainingGroupStatusSuccess(trainingGroupStatus: MutableList<TrainingOverallSearch>) {
        trainingGroupStatusList = trainingGroupStatus

        trainingGroupStatusDetailAdater.setData(trainingGroupStatusList)

        trainingGroupStatusDetailAdater.mListener = object : GroupTrainingStatusDetailAdapter.OnItemClickListener {
            override fun onTrainingGroupStatusClicked(position: Int, trainingGroupStatus: TrainingOverallSearch) {
                // 편집 모드일때 화면이동 막음
             if(!trainingGroupStatusDetailAdater.isEditMode) {
                 val intent = Intent(this@TrainingStatusActivity, TrainingConfirmActivity::class.java)
                 intent.putExtra("selectedGroup", selectedGroup)
                 intent.putExtra("userId", trainingGroupStatus.userId)
                 intent.putExtra("dateTime", selectedDateTime)
                 startActivity(intent)
             }
        }
            override fun onDeleteExercise(
                position: Int,
                deleteTrainingExercise: MutableList<DeleteTrainingGroupStatus>
            ) {
                    trainingViewModel.deleteTrainingStatus(deleteTrainingExercise)
            }
        }
        binding.rvTrainingStatus.adapter = trainingGroupStatusDetailAdater
    }

    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
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