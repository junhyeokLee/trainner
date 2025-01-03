package com.sports2i.trainer.ui.activity

import android.content.DialogInterface
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.Nutrition
import com.sports2i.trainer.data.model.NutritionDirectionKeyword
import com.sports2i.trainer.data.model.NutritionDirectionSearchResponse
import com.sports2i.trainer.data.model.NutritionPictureUser
import com.sports2i.trainer.databinding.ActivityNutritionCommentDetailBinding
import com.sports2i.trainer.ui.adapter.groupmember.GroupMemberNutritionCommentAdapter
import com.sports2i.trainer.ui.adapter.groupmember.GroupMemberNutritionPictureDetailAdapter
import com.sports2i.trainer.ui.dialog.CustomNutritionEvaluationDialogFragment
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

@AndroidEntryPoint
class NutritionCommentDetailActivity : BaseActivity<ActivityNutritionCommentDetailBinding>({ActivityNutritionCommentDetailBinding.inflate(it)}){

    private val groupViewModel: GroupViewModel by viewModels()
    private val nutritionViewModel: NutritionViewModel by viewModels()

    private var userList: MutableList<GroupUser> = mutableListOf()
    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()
    private var keywordList: MutableList<NutritionDirectionKeyword> = mutableListOf()
    private var nutritionUserStatusList: MutableList<NutritionPictureUser> = mutableListOf()

    private var nutritionDirectionSearch: NutritionDirectionSearchResponse.DirectionData? = null

    private lateinit var nutritionCommentAdapter : GroupMemberNutritionCommentAdapter
    private lateinit var nutritionGroupMemberPictureDetailAdater: GroupMemberNutritionPictureDetailAdapter

    private lateinit var customGroupSpinner: CustomSpinner
    private lateinit var customUserSpinner: CustomSpinner

    var selectedPositions: Set<Int> = setOf()
    var selectedUserPictureList: MutableList<NutritionPictureUser> = mutableListOf()

//    var selectedNutrition : Nutrition.NutritionPicture? = null
    var selectedNutrition :  Nutrition.NutritionPicture? = null

    var selectedGroup : GroupInfo? = null
    var selectedUserId : String? = null
    var selectedDateTime: String? = ""
    var groupId: String? = ""

    private var dateSelectionView : DateSelectionView? = null

    private var editMode = false

    private val networkStateObservers = listOf(
        ::observeGroupInfoState,
        ::observeGroupUserState,
        ::observeNutritionDirectionSearchState,
        ::observeNutritionPictureState
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        setContent()
        setFunction()
        networkStatus()
        clickBack()
        binding.root.post { setLiveData() }
        this@NutritionCommentDetailActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
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
        nutritionViewModel.searchNutritionDirection(selectedUserId!!, selectedDateTime!!)
        nutritionViewModel.searchNutritionPicture(selectedUserId!!, selectedDateTime!!)
    }


    private fun setContent() {
        Preferences.init(this, Preferences.DB_USER_INFO)
        groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))

        selectedNutrition = intent.getParcelableExtra("selectedNutrition")
        selectedGroup = intent.getParcelableExtra("selectedGroup")
        selectedUserId = intent.getStringExtra("userId")
        selectedDateTime = intent.getStringExtra("dateTime")

        customGroupSpinner = binding.spinnerGroup
        customUserSpinner = binding.spinnerUser

//        groupViewModel.requestGroupUser(selectedGroup!!.groupId)
//        nutritionViewModel.searchNutritionDirection(selectedUserId!!, selectedDateTime!!)
//        nutritionViewModel.searchNutritionPicture(selectedUserId!!, selectedDateTime!!)

        nutritionCommentAdapter = GroupMemberNutritionCommentAdapter(keywordList)
        nutritionGroupMemberPictureDetailAdater = GroupMemberNutritionPictureDetailAdapter(nutritionUserStatusList)

        binding.rvNutritionImage.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = nutritionGroupMemberPictureDetailAdater
        }

        binding.rvNutritionComment.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            nutritionCommentAdapter = GroupMemberNutritionCommentAdapter(keywordList)
            adapter = nutritionCommentAdapter
        }


        if(selectedNutrition != null){
            Glide.with(binding.ivNutritionImage)
                .load(selectedNutrition?.pictureUrl)
                .transform(CenterCrop(), RoundedCorners(24)) // 라운드 처리
                .error(R.drawable.ic_empty_food) // 에러 시 디폴트 이미지 표시
                .into(binding.ivNutritionImage)

            binding.tvNutritionTime.text = "촬영시간: "+selectedNutrition?.reportingDate
        }
        else {
            Glide.with(binding.ivNutritionImage)
                .load(R.drawable.ic_empty_food)
                .transform(CenterCrop(), RoundedCorners(24)) // 라운드 처리
                .error(R.drawable.ic_empty_food) // 에러 시 디폴트 이미지 표시
                .into(binding.ivNutritionImage)
        }
    }

    private fun setFunction() {
        refreshing()

        customGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedGroup = parent?.getItemAtPosition(position) as GroupInfo
                groupId = selectedGroup!!.groupId
                groupViewModel.requestGroupUser(groupId!!)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle Nothing Selected
            }
        }

        customUserSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedGroupUser = parent?.getItemAtPosition(position) as GroupUser
                selectedUserId = selectedGroupUser.userId
                nutritionViewModel.searchNutritionDirection(selectedUserId!!, selectedDateTime!!)
                nutritionViewModel.searchNutritionPicture(selectedUserId!!, selectedDateTime!!)
            }
        }

        binding.btnNutritionEatEvaluation.setOnClickListener {
            selectedPositions = nutritionGroupMemberPictureDetailAdater.getSelectedPositions()
            selectedUserPictureList.clear()
            selectedPositions.forEach {
                selectedUserPictureList.add(nutritionUserStatusList[it])
            }

            val positiveButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
                Global.showBottomSnackBar(binding.root, "평가완료 되었습니다.")
                dialog.dismiss() }

            val dialogFragment = CustomNutritionEvaluationDialogFragment.newInstance(
                positiveButtonClickListener,selectedUserPictureList
            )

            if(!selectedPositions.isNullOrEmpty()) {
                dialogFragment.show(supportFragmentManager, CustomNutritionEvaluationDialogFragment.TAG)
            } else {
                Global.showBottomSnackBar(binding.root, "선택된 사진이 없습니다.")

            }
        }
    }

    private fun refreshing() {
        binding.refreshLayout?.setOnRefreshListener {
            binding.refreshLayout?.isRefreshing = false
        }
    }

    private fun observeGroupInfoState() = observeNetworkState(groupViewModel.groupInfoState) {
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
    private fun observeNutritionDirectionSearchState() = observeNetworkState(nutritionViewModel.searchNutritionDirectionSearchSatate){
        handlerNutritionDirectionSearchSuccess(it.data.data)
    }

    private fun observeNutritionPictureState() = observeNetworkState(nutritionViewModel.searchNutritionPictureState){
        handlerNutritionPictureUserSuccess(it.data.data)
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

        networkStateObservers.forEach { observer ->
            observer()
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
                    textView.setTextColor(this@NutritionCommentDetailActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@NutritionCommentDetailActivity.resources.getColor(android.R.color.white))
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
                    textView.setTextColor(this@NutritionCommentDetailActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@NutritionCommentDetailActivity.resources.getColor(android.R.color.white))
                }
            }
        }
        customUserSpinner.setAdapterData(userList, itemBinder)
    }

    private fun handlerNutritionDirectionSearchSuccess(nutritionDirection: NutritionDirectionSearchResponse.DirectionData) {
        nutritionDirectionSearch = nutritionDirection

        if(nutritionDirectionSearch?.keywordList!!.isEmpty()){
            binding.tvNutritionCommentEmpty.visibility = View.VISIBLE
            binding.rvNutritionComment.visibility = View.GONE
        } else {
            binding.tvNutritionCommentEmpty.visibility = View.GONE
            binding.rvNutritionComment.visibility = View.VISIBLE
        }

        keywordList = nutritionDirectionSearch?.keywordList!!

        binding.tvNutritionComment.text = nutritionDirectionSearch?.content
        binding.rvNutritionComment.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            nutritionCommentAdapter = GroupMemberNutritionCommentAdapter(keywordList)
            adapter = nutritionCommentAdapter

        }
    }

    private fun handlerNutritionPictureUserSuccess(nutritionPictureUser: MutableList<NutritionPictureUser>) {
        nutritionUserStatusList = nutritionPictureUser
        nutritionGroupMemberPictureDetailAdater = GroupMemberNutritionPictureDetailAdapter(nutritionUserStatusList)
        binding.rvNutritionImage.adapter = nutritionGroupMemberPictureDetailAdater

        if(nutritionUserStatusList.isEmpty()) {
            binding.ivNutritionImage.visibility = View.INVISIBLE
            binding.rvNutritionImage.visibility = View.INVISIBLE
            binding.nutritionEmptyLayout.visibility = View.VISIBLE
            binding.tvNutritionImageEmpty.visibility = View.VISIBLE

        } else{
            binding.ivNutritionImage.visibility = View.VISIBLE
            binding.rvNutritionImage.visibility = View.VISIBLE
            binding.nutritionEmptyLayout.visibility = View.INVISIBLE
            binding.tvNutritionImageEmpty.visibility = View.INVISIBLE
        }

        nutritionGroupMemberPictureDetailAdater.mListener = object : GroupMemberNutritionPictureDetailAdapter.OnItemClickListener {
            override fun onNutritionGroupMemberStatusClicked(position: Int, nutrition: NutritionPictureUser) {
                // Glide를 사용하여 이미지 로드
                if (!editMode) {
                    Glide.with(binding.ivNutritionImage)
                        .load(nutrition.pictureUrl)
                        .transform(CenterCrop(), RoundedCorners(32)) // 라운드 처리
                        .error(R.drawable.ic_empty_food) // 에러 시 디폴트 이미지 표시
                        .into(binding.ivNutritionImage)

                    binding.tvNutritionTime.text = "촬영시간: " + nutrition?.reportingDate
                }
            }
            override fun onNutritionGroupMemberStatusLongClicked(position: Int, nutrition: NutritionPictureUser,nutritionCheck: Boolean) {
                // 아이템을 길게 눌렀을 때의 이벤트 처리
                // 선택한 포지션 또는 상태를 사용하여 작업 수행
                if (nutritionCheck) {
                    editMode = true
                } else {
                    editMode = false
                }

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
