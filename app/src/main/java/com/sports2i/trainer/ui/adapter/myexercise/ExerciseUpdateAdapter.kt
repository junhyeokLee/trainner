package com.sports2i.trainer.ui.adapter.myexercise

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingInfoResponse

class ExerciseUpdateAdapter(
    private val context: Context,
    private val dataList: MutableList<TrainingInfoResponse>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_exercise_update, parent, false)
        return ExerciseUpdateViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ExerciseUpdateViewHolder) {
            val exerciseUpdateList = dataList[position]
            holder.bind(exerciseUpdateList)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ExerciseUpdateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val unit_unitName: TextView = itemView.findViewById(R.id.tv_unit_unit_name)
        val mesureValue: AppCompatEditText = itemView.findViewById(R.id.et_mesure_value)
        val goalValue: TextView = itemView.findViewById(R.id.tv_goal_value)

        init {
            // TextWatcher를 ViewHolder 생성 시에 등록
            mesureValue.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // EditText 값이 변경될 때 호출되는 부분
                    // 수정된 값을 TrainingInfoResponse 객체에 적용
                    val updatedValue = s.toString().toDoubleOrNull()
                    dataList[adapterPosition].measuredValue = updatedValue ?: 0.0
                }
            })
        }

        fun bind(exerciseUpdate: TrainingInfoResponse) {
            unit_unitName.text = "${exerciseUpdate?.exerciseUnitName}(${exerciseUpdate?.exerciseUnit})"

            val formattedValue = if (exerciseUpdate.goalValue!!.toDouble() % 1 == 0.0) { // 소수점 자리가 0이면 정수로 변환하여 문자열로 반환
                String.format("%d", exerciseUpdate.goalValue!!.toDouble().toInt())
            } else {
                String.format("%.1f", exerciseUpdate.goalValue!!.toDouble()) // 그렇지 않으면 소수점 첫째 자리까지 포맷팅하여 반환
            }

            val formattedMesureValue = if (exerciseUpdate.measuredValue!!.toDouble() % 1 == 0.0) { // 소수점 자리가 0이면 정수로 변환하여 문자열로 반환
                String.format("%d", exerciseUpdate.measuredValue!!.toDouble().toInt())
            } else {
                String.format("%.1f", exerciseUpdate.measuredValue!!.toDouble()) // 그렇지 않으면 소수점 첫째 자리까지 포맷팅하여 반환
            }


            goalValue.text = formattedValue
            mesureValue.setText(formattedMesureValue ?: "0")
//            goalValue.text = exerciseUpdate?.goalValue?.toInt().toString()
//            mesureValue.setText(exerciseUpdate?.measuredValue?.toInt().toString() ?: "0")



        }
    }

    fun getDataList(): List<TrainingInfoResponse> {
        val updatedList = mutableListOf<TrainingInfoResponse>()

        for (item in dataList) {
            val measuredValue = if (item.measuredValue.toString().isNullOrBlank()) {
                0
            } else {
                item.measuredValue
            }

            val updatedItem = item.copy(measuredValue = measuredValue as Double?)
            updatedList.add(updatedItem)
        }

        return updatedList
    }

    fun addData(exerciseUpdate: TrainingInfoResponse) {
        dataList.add(exerciseUpdate)
        notifyDataSetChanged()
    }

    fun addListData(exerciseUpdateList: List<TrainingInfoResponse>) {
        dataList.clear()
        dataList.addAll(exerciseUpdateList)
        notifyDataSetChanged()
    }

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }
}
