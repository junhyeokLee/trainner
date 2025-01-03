package com.sports2i.trainer.ui.fragment.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.TrainingOverall
import com.sports2i.trainer.databinding.FragmentGroupTrainingStatusBinding
import com.sports2i.trainer.ui.activity.NutritionCommentDetailActivity
import com.sports2i.trainer.ui.activity.TrainingConfirmActivity
import com.sports2i.trainer.ui.adapter.group.GroupTrainingStatusAdapter
import com.sports2i.trainer.ui.fragment.BaseFragment
import com.sports2i.trainer.utils.Preferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupTrainingStatusFragment: BaseFragment<FragmentGroupTrainingStatusBinding>() {

    private var strTrainingDate: String? = null
    private var status = 0
    private var trainingStatusList: MutableList<TrainingOverall> = mutableListOf()
    private var selectedGroupInfo: GroupInfo? = null
    private lateinit var groupTrainingStatusAdapter: GroupTrainingStatusAdapter
    private var moreFlag: Boolean = false // 더보기 버튼 클릭 여부
    private var trainingStatusDefaultSize = 6
    private var fragmentContext: Context? = null
    private var isFragmentActive = false

    companion object {
        fun newInstance(
            status: Int,
            trainingStatusList: MutableList<TrainingOverall>,
            selectedGroupInfo: GroupInfo,
            strTrainingDate: String
        ): GroupTrainingStatusFragment {
            Log.e("newInstance", "filteredTrainingList: " + trainingStatusList.toString())
            val fragment = GroupTrainingStatusFragment()
            fragment.status = status
            fragment.trainingStatusList = trainingStatusList ?: mutableListOf()
            fragment.selectedGroupInfo = selectedGroupInfo
            fragment.strTrainingDate = strTrainingDate
            return fragment
        }
    }

    private val fragmentGroupTrainingStatusBinding
        get() = binding!!

    // GroupTrainingStatusAdapter.OnItemClickListener 리스너를 정의하는 변수 추가
    private val trainingStatusClickListener =
        object : GroupTrainingStatusAdapter.OnItemClickListener {
            override fun onTrainingStatusClicked(position: Int, trainingOverall: TrainingOverall) {
                if (status == 0) {

                    if(checkAdminOrTrainerAuthority())
                    {
                            val intent = Intent(context, TrainingConfirmActivity::class.java)
                            intent.putExtra("selectedGroup", selectedGroupInfo)
                            intent.putExtra("userId", trainingOverall.userId)
                            intent.putExtra("dateTime", strTrainingDate)
                            startActivity(intent)
                    }
                    else{
//                            val intent = Intent(context, TrainingConfirmTraineeActivity::class.java)
//                            intent.putExtra("selectedGroup", selectedGroupInfo)
//                            intent.putExtra("userId", trainingOverall.userId)
//                            intent.putExtra("dateTime", strTrainingDate)
//                            startActivity(intent)
                    }
                }
                else if (status == 1) {
                    if(checkAdminOrTrainerAuthority()) {
                        val intent = Intent(context, NutritionCommentDetailActivity::class.java)
                        intent.putExtra("selectedGroup", selectedGroupInfo)
                        intent.putExtra("userId", trainingOverall.userId)
                        intent.putExtra("dateTime", strTrainingDate)
                        startActivity(intent)
                    } else{
//                        val intent = Intent(context, NutritionCommentDetailTraineeActivity::class.java)
//                        intent.putExtra("selectedGroup", selectedGroupInfo)
//                        intent.putExtra("userId", trainingOverall.userId)
//                        intent.putExtra("dateTime", strTrainingDate)
//                        startActivity(intent)
                    }
                }
            }
        }


    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGroupTrainingStatusBinding {
        return FragmentGroupTrainingStatusBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContent()
        setFunction()
    }

    override fun setContent() {
        Preferences.init(this.requireContext(), Preferences.DB_USER_INFO)

        groupTrainingStatusAdapter = GroupTrainingStatusAdapter(requireContext(), trainingStatusList, trainingStatusDefaultSize, status)

        if(trainingStatusList.size <= 6) fragmentGroupTrainingStatusBinding.tvMore.visibility = View.GONE
        else fragmentGroupTrainingStatusBinding.tvMore.visibility = View.VISIBLE

        groupTrainingStatusAdapter.mListener = trainingStatusClickListener
        fragmentGroupTrainingStatusBinding.rvTrainingStatus.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = groupTrainingStatusAdapter
        }
    }

    override fun setFunction() {
        fragmentGroupTrainingStatusBinding.tvMore.setOnClickListener {
            moreFlag = !moreFlag
            loadData() // 더보기 버튼을 누를 때마다 데이터를 로드합니다.
            saveCurrentState()
        }
    }

    private fun saveCurrentState() {
        Preferences.put(Preferences.KEY_GROUP_MORE_VIEW_CLICCKED, moreFlag)
    }
    private fun loadSavedState() {
        moreFlag = Preferences.boolean(Preferences.KEY_GROUP_MORE_VIEW_CLICCKED)
        loadData()
    }

    private fun loadData() {
        if (!moreFlag) {
            groupTrainingStatusAdapter = GroupTrainingStatusAdapter(
                requireContext(),
                trainingStatusList,
                trainingStatusDefaultSize,
                status
            )
            fragmentGroupTrainingStatusBinding.tvMore.text = "더보기"
        } else {
            groupTrainingStatusAdapter = GroupTrainingStatusAdapter(
                requireContext(),
                trainingStatusList,
                trainingStatusList.size,
                status
            )
            fragmentGroupTrainingStatusBinding.tvMore.text = "접기"
        }

        // listener를 설정.
        groupTrainingStatusAdapter.mListener = trainingStatusClickListener
        fragmentGroupTrainingStatusBinding.rvTrainingStatus.adapter = groupTrainingStatusAdapter
        fragmentGroupTrainingStatusBinding.rvTrainingStatus.adapter?.notifyDataSetChanged()

    }

    fun refreshData() {
        loadSavedState()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
        isFragmentActive = true
    }

    override fun onDetach() {
        super.onDetach()
        fragmentContext = null
        isFragmentActive = false
    }

    override fun onPause() {
        super.onPause()
        isFragmentActive = false
    }


    override fun onResume() {
        super.onResume()
        isFragmentActive = true
    }


}