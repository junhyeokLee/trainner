package com.sports2i.trainer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.data.model.Notice
import com.sports2i.trainer.databinding.ActivityNoticeListBinding
import com.sports2i.trainer.ui.adapter.group.GroupNoticeListAdapter
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.viewmodel.NoticeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeListActivity : BaseActivity<ActivityNoticeListBinding>({ ActivityNoticeListBinding.inflate(it)}) {

    private val noticeViewModel: NoticeViewModel by viewModels()
    private var noticeList: MutableList<Notice> = mutableListOf()
    private var lastIndex = 0

    private lateinit var noticeDetailAdapter: GroupNoticeListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setFunction()
        observe()

        clickBack()

        this@NoticeListActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    private fun init() {

        this.noticeViewModel.getNoticeList(lastIndex)

        this.noticeDetailAdapter = GroupNoticeListAdapter(noticeList)

        binding.rvNotice.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = noticeDetailAdapter
        }

    }

    private fun setFunction() {
        refreshing()

        binding.rvNotice.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPositions(null).maxOrNull()

                // 스크롤이 마지막 아이템에 도달하면 추가 데이터를 불러오기
                if (lastVisibleItemPosition == noticeList.size - 1) {
                    loadMoreData()
                }
            }
        })

        noticeDetailAdapter.mListener = object : GroupNoticeListAdapter.OnItemClickListener {
            override fun onNoticeClicked(position: Int, notice: Notice) {
                val intent = Intent(this@NoticeListActivity, NoticeDetailActivity::class.java)
                intent.putExtra("noticeId", notice.id)
                intent.putExtra("groupId", notice.groupId)
                startActivity(intent)
            }
        }
    }
    private fun loadMoreData() {
        val lastNoticeId = noticeList.lastOrNull()?.id ?: 0
        lastIndex = lastNoticeId
        noticeViewModel.getNoticeList(lastIndex)
    }


    private fun observe() {

        this.noticeViewModel.noticeListState.observe(this) {
            when (it) {
                is NetworkState.Success -> handlerNoticeListSuccess(it.data.data)
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading()
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

    private fun handlerNoticeListSuccess(notices: MutableList<Notice>) {

            val uniqueNotices = notices.filterNot { newNotice ->
                noticeList.any { existingNotice -> existingNotice.id == newNotice.id }
            }
            noticeList.addAll(uniqueNotices)
            noticeDetailAdapter.notifyDataSetChanged()

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

}