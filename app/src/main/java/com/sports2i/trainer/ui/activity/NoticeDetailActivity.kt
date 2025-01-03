package com.sports2i.trainer.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.Notice
import com.sports2i.trainer.databinding.ActivityNoticeDetailBinding
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.OnSingleClickListener
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.NoticeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeDetailActivity : BaseActivity<ActivityNoticeDetailBinding>({ActivityNoticeDetailBinding.inflate(it)}) {

    private val noticeViewModel: NoticeViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()
    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()
    private var selectedGroup: GroupInfo? = null
    private var selectedGroupId: String = ""
    private var selectedNoticeId : Int? = null
    private var noticeDetail: Notice? = null
    private var noticeEditConfirm:Boolean = false

    private lateinit var groupCustomSpinner: CustomSpinner // CustomSpinner 정의

    val callBackActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedNoticeId = result.data?.getIntExtra("noticeId",0)
            selectedGroupId = result.data?.getStringExtra("groupId").toString()
            noticeViewModel.getNoticeDetail(selectedNoticeId!!)
            observe()

            noticeEditConfirm = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setFunction()
        observe()

        clickBack()

        this@NoticeDetailActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)

        selectedNoticeId = intent.getIntExtra("noticeId",0)
        selectedGroupId = intent.getStringExtra("groupId").toString()

        noticeViewModel.getNoticeDetail(selectedNoticeId!!)

        when(Preferences.string(Preferences.KEY_AUTHORITY)){
            "TRAINEE" -> {
                binding.btnDeleteNotice.visibility = View.GONE
                binding.btnEditNotice.visibility = View.GONE
            }
            else -> {
                binding.btnDeleteNotice.visibility = View.VISIBLE
                binding.btnEditNotice.visibility = View.VISIBLE
            }
        }


        this.groupCustomSpinner = binding.spinnerGroup // CustomSpinner 초기화

    }

    private fun setFunction() {
        refreshing()

        groupCustomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroupInfo = parent?.getItemAtPosition(position) as GroupInfo
                selectedGroupId = selectedGroupInfo.groupId

            }
        }

        binding.btnEditNotice.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View) {
                val intent = Intent(this@NoticeDetailActivity, NoticeWriteActivity::class.java)
                intent.putExtra("notice", noticeDetail)
                intent.putExtra("editMode", true)
                callBackActivityResultLauncher.launch(intent)

            }
        })

        binding.btnDeleteNotice.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View) {
                val builder = AlertDialog.Builder(this@NoticeDetailActivity)
                builder.setTitle("공지사항")
                builder.setMessage("공지사항을 삭제하시겠습니까?")
                builder.setPositiveButton("삭제") { dialog, which ->
                    noticeViewModel.deleteNotice(selectedNoticeId!!)
                }
                builder.setNegativeButton("취소") { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()
            }
        })
    }

    private fun observe() {

        this.groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))

        this.groupViewModel.groupInfoState.observe(this) {
            when (it) {
                is NetworkState.Success -> handlerGroupInfoSuccess(it.data.data)
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading()
            }
        }

        this.noticeViewModel.noticeInsertState.observe(this) {
            when (it) {
                is NetworkState.Success -> handlerNoticeDetailSuccess(it.data.data)
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading()
            }
        }

        this.noticeViewModel.noticeDeleteState.observe(this) {
            when (it) {
                is NetworkState.Success -> {
                    val resultIntent = Intent()
                    resultIntent.putExtra("noticeDelete",true )
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                    back()
                }
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun refreshing() {
//        binding.refreshLayout?.setOnRefreshListener {
////            refresh()
//            binding.refreshLayout?.isRefreshing = false
//        }
    }

    private fun handlerGroupInfoSuccess(groupInfos: MutableList<GroupInfo>) {
        groupInfoList = groupInfos

        // "전체(ALL)" 아이템을 추가합니다.
        val allGroupInfo = GroupInfo("ALL", "전체","전체")
        if (!groupInfoList.any { it.groupId == allGroupInfo.groupId }) {
            groupInfoList.add(0, allGroupInfo)
        }

        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort
                if (isDropDown) {
                    textView.setTextColor(this@NoticeDetailActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@NoticeDetailActivity.resources.getColor(android.R.color.white))
                }
            }
        }

        groupCustomSpinner.setAdapterData(groupInfoList, itemBinder)

// 선택한 그룹이 "ALL"이 아닐 때 해당 ID에 맞는 스피너 항목을 선택합니다.
        if (selectedGroupId != "ALL") {
            for (groupInfo in groupInfoList) {
                if (groupInfo.groupId == selectedGroupId) {
                    selectedGroup = groupInfo
                    binding.spinnerGroup.setSelection(groupInfoList.indexOf(groupInfo))
                    binding.spinnerGroup.isEnabled = false // 전체가 아니면 클릭 막음
                    break
                }
            }
        }
    }



    private fun handlerNoticeDetailSuccess(notice: Notice) {
        noticeDetail = notice

        binding.tvNoticeTitle.text = notice.title
        binding.tvNoticeContent.text = notice.contents
        binding.tvNoticeDate.text = notice.createdDate

        binding.tvNoticeContent.movementMethod = ScrollingMovementMethod.getInstance()


    }



    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun handlerLoading() {
//        Global.progressON(requireContext())
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            if(noticeEditConfirm){
                val resultIntent = Intent()
                resultIntent.putExtra("noticeEdit",true )
                setResult(Activity.RESULT_OK, resultIntent)
            }

            finish()
            back()
        }
    }
    private fun clickBack() {
        binding.layoutBack.setOnClickListener {
            if(noticeEditConfirm){
                val resultIntent = Intent()
                resultIntent.putExtra("noticeEdit",true )
                setResult(Activity.RESULT_OK, resultIntent)
            }

            finish()
            back()
        }
    }

}