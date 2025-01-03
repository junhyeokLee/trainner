package com.sports2i.trainer.ui.fragment.groupmember

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.TrainingGroupStatus
import com.sports2i.trainer.databinding.FragmentExerciseTimeBinding
import com.sports2i.trainer.ui.adapter.groupmember.ExerciseTimeAdapter
import com.sports2i.trainer.ui.fragment.BaseFragment
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.TrainingViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ExerciseTimeFragment : BaseFragment<FragmentExerciseTimeBinding>() {

    // trainingTime을 저장하는 변수 추가
    private var trainingTime: String? = null
    private var trainingStatus: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise> = mutableListOf()
    private var groupInfo: GroupInfo? = null
    private var strUserId: String? = null
    var exerciseItemClickListener: OnExerciseItemClickListener? = null

    companion object {
        fun newInstance(trainingStatus: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>, groupInfo: GroupInfo, strUserId:String): ExerciseTimeFragment {
            val fragment = ExerciseTimeFragment()
            fragment.trainingStatus = trainingStatus
            fragment.groupInfo = groupInfo
            fragment.strUserId = strUserId
            return fragment
        }
    }

    private val trainingViewModel: TrainingViewModel by viewModels()
    private lateinit var exerciseTimeAdapter: ExerciseTimeAdapter
    private var trainingStatusList: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise> = mutableListOf()
    private var strTrainingDate: String? = null
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

        exerciseTimeAdapter = ExerciseTimeAdapter(requireContext(), mutableListOf())
        fragmentExerciseTimeBinding.rvTrainingStatus.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = exerciseTimeAdapter
        }

        val groupedTrainingStatus = trainingStatus!!.groupBy { it.exerciseId }
        val nonDuplicateTrainingStatus = groupedTrainingStatus.map { it.value.first() }

        if(nonDuplicateTrainingStatus.isNullOrEmpty()) {
            fragmentExerciseTimeBinding.tvEmptyData.visibility = View.VISIBLE
            fragmentExerciseTimeBinding.rvTrainingStatus.visibility = View.GONE
        } else {
            fragmentExerciseTimeBinding.tvEmptyData.visibility = View.GONE
            fragmentExerciseTimeBinding.rvTrainingStatus.visibility = View.VISIBLE
        }

        exerciseTimeAdapter.updateData(nonDuplicateTrainingStatus)

        exerciseTimeAdapter.mListener = object : ExerciseTimeAdapter.OnItemClickListener {
            override fun onExerciseClickListener(position:Int,trainingInfo:MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>) {
                exerciseItemClickListener?.onExerciseItemClicked(trainingInfo[position])
            }
        }
    }

    override fun setFunction() {}

    override fun refreshing() {
        super.refreshing()
    }

    // 사용자 ID와 트레이닝 날짜를 설정하는 함수 추가
//    fun setUserData(userId: String, trainingDate: String, groupInfo: GroupInfo) {
//            this.strUserId = userId
//            this.strTrainingDate = trainingDate
//            this.groupInfo = groupInfo
//            this.trainingViewModel!!.getTrainingStatus(userId, trainingDate)
//
//    }
//
//    fun setData(userId:String, groupInfo: GroupInfo,trainingDate: String){
//        this.strUserId = userId
//        this.groupInfo = groupInfo
//        this.strTrainingDate = trainingDate
//
//        Log.e("TAG", "setData: $strUserId")
//    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        trainingStatusList.clear()
    }

    interface OnExerciseItemClickListener {
        fun onExerciseItemClicked(trainingInfo: TrainingGroupStatus.TrainingGroupStatusExercise)
    }

}
