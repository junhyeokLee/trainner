package com.sports2i.trainer.ui.adapter.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.DeleteTrainingGroupStatus
import com.sports2i.trainer.data.model.TrainingGroupStatus
import com.sports2i.trainer.data.model.TrainingOverallSearch
import com.sports2i.trainer.viewmodel.TrainingViewModel

class GroupTrainingStatusDetailAdapter(
    private val dataList: MutableList<TrainingOverallSearch>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedPositions = mutableListOf<Int>() // 선택된 아이템 위치를 저장하는 리스트
    private lateinit var groupTrainingStatusDetailItemAdapter: GroupTrainingStatusDetailItemAdapter

    var mListener: OnItemClickListener? = null

    var isEditMode = false
        set(value) {
            field = value
            notifyDataSetChanged() // isEditMode 값 변경 시 어댑터를 갱신합니다.
        }

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_training_status_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).setDetails(position)
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return dataList.size
    }

    // 뷰 홀더 클래스 정의
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setDetails(position: Int) {
            val trainingGroupStatus = dataList[position]
            val groupUserImg: TextView = itemView.findViewById(R.id.tv_user_img)
            val groupUserName: TextView = itemView.findViewById(R.id.tv_user_name)
            val rvTrainingStatus: RecyclerView = itemView.findViewById(R.id.rv_training_status_detail)
            val emptyTrainingData:TextView = itemView.findViewById(R.id.tv_empty_training_data)

            val userName = trainingGroupStatus.userName

            if (userName.length > 3) { // 3글자 이상 ...
                groupUserName.text = userName.substring(0, 3) + "..."
            } else {
                groupUserName.text = userName
            }

            groupUserImg.text = trainingGroupStatus.userName?.substring(0, 1)

            if(isEditMode){
                // 편집 모드일때 전체 보이게
                // exerciseList time과 id 중복 제거를 위해 distinctBy 사용
                val filteredExerciseList = trainingGroupStatus.exerciseList!!.distinctBy { "${it.trainingTime}${it.exerciseId}" }
                if (filteredExerciseList.size == 0 || filteredExerciseList.isEmpty()) emptyTrainingData.visibility = View.VISIBLE
                else emptyTrainingData.visibility = View.GONE
                groupTrainingStatusDetailItemAdapter = GroupTrainingStatusDetailItemAdapter(
                    filteredExerciseList as MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>,
                    isEditMode
                )
            }else {
                // exerciseList 중복 제거를 위해 distinctBy 사용 편집 모드가 아닐때 합쳐보이게
                val filteredExerciseList = trainingGroupStatus.exerciseList!!.distinctBy { it.exerciseId }
                if (filteredExerciseList.size == 0 || filteredExerciseList.isEmpty()) emptyTrainingData.visibility = View.VISIBLE
                else emptyTrainingData.visibility = View.GONE
                groupTrainingStatusDetailItemAdapter = GroupTrainingStatusDetailItemAdapter(
                    filteredExerciseList as MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>,
                    isEditMode
                )
            }

            rvTrainingStatus.apply {
                layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                adapter = groupTrainingStatusDetailItemAdapter
            }


            groupTrainingStatusDetailItemAdapter.mDeleteListener = object : GroupTrainingStatusDetailItemAdapter.OnDeleteItemClickListener {
                override fun onDeleteExercise(position: Int, exercise: TrainingGroupStatus.TrainingGroupStatusExercise) {
                    val userId = trainingGroupStatus.userId
                    val trainingDate = exercise.trainingDate
                    val trainingTime = exercise.trainingTime
                    val exerciseId = exercise.exerciseId
                    val exerciseUnitId = exercise.exerciseUnitId

                    // 선택된 운동의 exerciseId와 일치하는 모든 아이템을 삭제할 아이템 목록 생성
                    val exerciseList: MutableList<DeleteTrainingGroupStatus.DeleteTrainingGroupStatusExercise> = mutableListOf()
                    trainingGroupStatus.exerciseList.forEach { item ->
                        if (item.exerciseId == exerciseId) {
                            exerciseList.add(DeleteTrainingGroupStatus.DeleteTrainingGroupStatusExercise(item.exerciseId, item.exerciseUnitId))
                        }
                    }

                    val deleteTrainingGroupStatusList: MutableList<DeleteTrainingGroupStatus> = mutableListOf()
                    val deleteTrainingGroupStatus = DeleteTrainingGroupStatus(userId, trainingDate, trainingTime, exerciseList)
                    deleteTrainingGroupStatusList.add(deleteTrainingGroupStatus)

                    mListener?.onDeleteExercise(position, deleteTrainingGroupStatusList)
                }
            }

            groupTrainingStatusDetailItemAdapter.mListener = object : GroupTrainingStatusDetailItemAdapter.OnItemClickListener {
                override fun onTrainingStatusClicked(position: Int, exercise: TrainingGroupStatus.TrainingGroupStatusExercise) {
                    mListener?.onTrainingGroupStatusClicked(position, trainingGroupStatus)
                }
            }
        }
    }

    // 데이터 업데이트를 위한 함수
    fun setData(newDataList: List<TrainingOverallSearch>) {
        dataList.clear()
        dataList.addAll(newDataList)
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = dataList[position]

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    fun getSelectedData(): MutableList<TrainingOverallSearch> {
        val selectedData = mutableListOf<TrainingOverallSearch>()
        for (position in selectedPositions) {
            selectedData.add(dataList[position])
        }
        return selectedData
    }


    interface OnItemClickListener {
        fun onTrainingGroupStatusClicked(position: Int, trainingGroupStatus: TrainingOverallSearch)

        fun onDeleteExercise(position: Int, deleteTrainingExercise:  MutableList<DeleteTrainingGroupStatus>)
    }

}
