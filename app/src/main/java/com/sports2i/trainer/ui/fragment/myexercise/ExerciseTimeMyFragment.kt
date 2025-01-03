package com.sports2i.trainer.ui.fragment.myexercise

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.TrainingGroupStatus
import com.sports2i.trainer.databinding.FragmentExerciseTimeBinding
import com.sports2i.trainer.ui.adapter.groupmember.ExerciseTimeAdapter
import com.sports2i.trainer.ui.fragment.BaseFragment
import com.sports2i.trainer.ui.view.DisallowInterceptRecyclerView
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ExerciseTimeMyFragment : BaseFragment<FragmentExerciseTimeBinding>() {

    // trainingTime을 저장하는 변수 추가
    private var trainingTime: String? = null
    private var trainingStatus: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise> = mutableListOf()
    var exerciseItemClickListener: OnExerciseItemClickListener? = null

    companion object {
        fun newInstance(trainingStatus: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>): ExerciseTimeMyFragment {
            val fragment = ExerciseTimeMyFragment()
            fragment.trainingStatus = trainingStatus
            return fragment
        }
    }

    private val trainingViewModel: TrainingViewModel by viewModels()
    private lateinit var exerciseTimeAdapter: ExerciseTimeAdapter
    private var trainingStatusList: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise> = mutableListOf()
    private var strUserId: String? = null
    private var strTrainingDate: String? = null
    private var groupInfo: GroupInfo? = null
    private lateinit var recyclerView: DisallowInterceptRecyclerView
    private val fragmentExerciseTimeBinding
        get() = binding!!

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentExerciseTimeBinding {
        return FragmentExerciseTimeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContent()
        setFunction()
//        networkStatus()
    }

    override fun setContent() {

        Preferences.init(requireContext(), Preferences.DB_USER_INFO)

        recyclerView = fragmentExerciseTimeBinding.rvTrainingStatus
        trainingStatusList.clear()
        trainingStatusList.addAll(trainingStatus)

        val groupedTrainingStatus = trainingStatusList!!.groupBy { it.exerciseId }
        val nonDuplicateTrainingStatus = groupedTrainingStatus.map { it.value.first() }

        exerciseTimeAdapter = ExerciseTimeAdapter(requireContext(), nonDuplicateTrainingStatus as MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>)
        recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = exerciseTimeAdapter
        }

        if(trainingStatus.isNullOrEmpty()) {
            fragmentExerciseTimeBinding.tvEmptyData.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            fragmentExerciseTimeBinding.tvEmptyData.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
//        exerciseTimeAdapter.updateData(nonDuplicateTrainingStatus)

        exerciseTimeAdapter.mListener = object : ExerciseTimeAdapter.OnItemClickListener {
            override fun onExerciseClickListener(position:Int,exerciseInfo:MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>) {
                exerciseItemClickListener?.onExerciseItemClicked(exerciseInfo[position])
            }
        }
    }

    override fun setFunction() {}

    override fun refreshing() {
        super.refreshing()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        trainingStatusList.clear()
    }

    fun onViewPagerSwipeStateChanged(enabled: Boolean) {
        // 리사이클러뷰의 스크롤을 제어하는 로직을 여기에 추가하세요.
        recyclerView.isNestedScrollingEnabled = enabled
    }
    interface OnExerciseItemClickListener {
        fun onExerciseItemClicked(trainingInfo: TrainingGroupStatus.TrainingGroupStatusExercise)
    }

}
