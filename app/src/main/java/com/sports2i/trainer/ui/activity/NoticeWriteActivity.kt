package com.sports2i.trainer.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.Notice
import com.sports2i.trainer.data.model.NoticeInsert
import com.sports2i.trainer.databinding.ActivityNoticeWriteBinding
import com.sports2i.trainer.ui.view.CustomSpinner
import com.sports2i.trainer.ui.view.ItemBinder
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.OnSingleClickListener
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.NoticeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeWriteActivity : BaseActivity<ActivityNoticeWriteBinding>({ActivityNoticeWriteBinding.inflate(it)}) {

    private val noticeViewModel: NoticeViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()
    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()
    private var selectedGroupId: String = ""
    private var selectedGroup: GroupInfo? = null
    private var noticeDetail: Notice? = null
    private var editMode: Boolean = false

    private lateinit var groupCustomSpinner: CustomSpinner // CustomSpinner 정의

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setFunction()
        observe()

        clickBack()

        this@NoticeWriteActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)

        this.groupCustomSpinner = binding.spinnerGroup // CustomSpinner 초기화

        noticeDetail = intent.getParcelableExtra("notice")
        editMode = intent.getBooleanExtra("editMode", false)

        // "전체(ALL)" 아이템을 추가합니다.
        val allGroupInfo = GroupInfo("ALL", "전체","전체")

        groupInfoList.add(0, allGroupInfo)

        if (noticeDetail != null) {
            binding.etNoticeTitle.setText(noticeDetail?.title)
            binding.etNoticeContent.setText(noticeDetail?.contents)
            selectedGroupId = noticeDetail?.groupId.toString()
        }

        if (!editMode) {
            binding.btnNoticeWrite.text = resources.getString(R.string.enroll)
        } else {
            binding.btnNoticeWrite.text = resources.getString(R.string.update)
        }
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

        binding.btnNoticeWrite.setOnClickListener(object :OnSingleClickListener(){
            override fun onSingleClick(v: View) {

            val title = binding.etNoticeTitle.text.toString()
            val content = binding.etNoticeContent.text.toString()

            selectedGroupId = (groupCustomSpinner.selectedItem as GroupInfo).groupId

            if(!editMode){
                if(title.isNotEmpty() || title.isNotBlank()) {
                    noticeViewModel.requestNoticeEnroll(
                        NoticeInsert(
                            selectedGroupId,
                            title,
                            content
                        )
                    )
                    showNoticeEnrollDialog(resources.getString(R.string.enroll_notice))
                } else {
                    showEmptyDialog("제목을 작성해주세요")
                }
            }
            else {
                noticeViewModel.updateNotice(noticeDetail!!.id, NoticeInsert(selectedGroupId,title,content))
                showNoticeEnrollDialog(resources.getString(R.string.update_notice))
            }

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

        groupInfoList.add(0, allGroupInfo)

        // ItemBinder 인터페이스를 구현하여 데이터를 바인딩합니다.
        val itemBinder = object : ItemBinder<GroupInfo> {
            override fun bindItem(view: View, item: GroupInfo, isDropDown: Boolean) {
                val textView = view.findViewById<TextView>(R.id.tv_spinner)
                textView.text = item.groupNameShort
                if (isDropDown) {
                    textView.setTextColor(this@NoticeWriteActivity.resources.getColor(android.R.color.black))
                } else {
                    textView.setTextColor(this@NoticeWriteActivity.resources.getColor(android.R.color.white))
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
                    break
                }
            }
        }

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

    private fun showNoticeEnrollDialog(message: String){
        val alertDialogBuilder = AlertDialog.Builder(this@NoticeWriteActivity)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            val resultIntent = Intent()
            resultIntent.putExtra("noticeId", noticeDetail?.id)
            resultIntent.putExtra("groupId", selectedGroupId)
            resultIntent.putExtra("noticeInsert",true )
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
            back()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showEmptyDialog(message: String){
        val alertDialogBuilder = AlertDialog.Builder(this@NoticeWriteActivity)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

}