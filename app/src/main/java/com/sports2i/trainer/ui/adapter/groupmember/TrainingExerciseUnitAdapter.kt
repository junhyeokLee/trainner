package com.sports2i.trainer.ui.adapter.groupmember

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingExercise
import com.sports2i.trainer.data.model.TrainingInfo


class TrainingExerciseUnitAdapter(
    private val context: Context,
    private val dataList: MutableList<TrainingInfo.ExerciseList>
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
        private val layoutExercise: LinearLayout = itemView.findViewById(R.id.item_group_exercise)
        private val unitTextView: TextView = itemView.findViewById(R.id.tv_unit)
        private val unitValueTextView: TextView = itemView.findViewById(R.id.tv_unit_value)
        private val unitMeasureTextView: TextView = itemView.findViewById(R.id.tv_unit_measured_value)

        fun bind(exercise: TrainingInfo.ExerciseList) {
            // 타이틀 설정
            unitTextView.text = exercise.exerciseUnitName+"("+exercise.exerciseUnit+")"

                val formattedValue = if (exercise.goalValue % 1 == 0.0) {
                    exercise.goalValue.toInt().toString() // 소수점 자리가 0이면 정수로 변환하여 문자열로 반환
                } else {
                    String.format("%.1f", exercise.goalValue) // 그렇지 않으면 소수점 첫째 자리까지 포맷팅하여 반환
                }

                val formattedMesureValue = if (exercise.measuredValue % 1 == 0.0) {
                    exercise.measuredValue.toInt().toString() // 소수점 자리가 0이면 정수로 변환하여 문자열로 반환
                } else {
                    String.format("%.1f", exercise.measuredValue) // 그렇지 않으면 소수점 첫째 자리까지 포맷팅하여 반환
                }

            unitMeasureTextView.text = formattedMesureValue
            unitValueTextView.text = formattedValue

            layoutExercise.setOnClickListener {
                mListener?.onItemClick()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick()
    }

}
