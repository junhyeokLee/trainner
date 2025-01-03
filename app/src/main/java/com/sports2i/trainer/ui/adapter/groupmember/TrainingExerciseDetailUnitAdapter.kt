package com.sports2i.trainer.ui.adapter.groupmember

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingInfoResponse


class TrainingExerciseDetailUnitAdapter(
    private val context: Context,
    private val dataList: MutableList<TrainingInfoResponse>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_group_exercise_preview_item, parent, false)
        return GroupExerciseUnitPrevieViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GroupExerciseUnitPrevieViewHolder) {
            val exerciseList = dataList[position]
            holder.bind(exerciseList)
        }
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class GroupExerciseUnitPrevieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val unitTextView: TextView = itemView.findViewById(R.id.tv_unit)
        private val unitValueTextView: TextView = itemView.findViewById(R.id.tv_unit_value)
        private val unitMeasureTextView: TextView = itemView.findViewById(R.id.tv_unit_measured_value)

        fun bind(exercise: TrainingInfoResponse) {
            // 타이틀 설정
            unitTextView.text = exercise.exerciseUnitName+"("+exercise.exerciseUnit+")"


            val formattedValue = if (exercise.goalValue!!.toDouble() % 1 == 0.0) { // 소수점 자리가 0이면 정수로 변환하여 문자열로 반환
                String.format("%d", exercise.goalValue!!.toDouble().toInt())
            } else {
                String.format("%.1f", exercise.goalValue!!.toDouble()) // 그렇지 않으면 소수점 첫째 자리까지 포맷팅하여 반환
            }

            val formattedMesureValue = if (exercise.measuredValue!!.toDouble() % 1 == 0.0) { // 소수점 자리가 0이면 정수로 변환하여 문자열로 반환
                String.format("%d", exercise.measuredValue!!.toDouble().toInt())
            } else {
                String.format("%.1f", exercise.measuredValue!!.toDouble()) // 그렇지 않으면 소수점 첫째 자리까지 포맷팅하여 반환
            }


            unitMeasureTextView.text = formattedMesureValue
            unitValueTextView.text = formattedValue

            itemView.setOnClickListener {
                mListener?.onItemClick(position,dataList)
            }
        }
    }

    fun addListData(trainingExerciseList: List<TrainingInfoResponse>) {
        dataList.clear()
        dataList.addAll(trainingExerciseList)
        notifyDataSetChanged()
    }

    fun addData(trainingExercise: TrainingInfoResponse) {
        dataList.add(trainingExercise)
        notifyDataSetChanged()
    }

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, trainingExerciseList: List<TrainingInfoResponse>)
    }
}
