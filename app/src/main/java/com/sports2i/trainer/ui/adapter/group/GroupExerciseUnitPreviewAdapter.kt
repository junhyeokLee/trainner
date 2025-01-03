package com.sports2i.trainer.ui.adapter.group

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingInfo


class GroupExerciseUnitPreviewAdapter(
    private val context: Context,
    private val dataList: MutableList<TrainingInfo.ExerciseList>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

        fun bind(exerciseList: TrainingInfo.ExerciseList) {
            // 타이틀 설정
            unitTextView.text = exerciseList.exerciseUnitName+"("+exerciseList.exerciseUnit+")"

            val formattedValue = if (exerciseList.goalValue % 1 == 0.0) {
                exerciseList.goalValue.toInt().toString() // 소수점 자리가 0이면 정수로 변환하여 문자열로 반환
            } else {
                String.format("%.1f", exerciseList.goalValue) // 그렇지 않으면 소수점 첫째 자리까지 포맷팅하여 반환
            }
            unitValueTextView.text = formattedValue
        }
    }

}
