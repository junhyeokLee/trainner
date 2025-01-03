package com.sports2i.trainer.ui.adapter.group

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.data.model.TrainingOverall

class GroupTrainingStatusAdapter(
    private val context: Context,
    private val dataList: MutableList<TrainingOverall>,
    private var size:Int,
    private var status:Int
    ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedPositions = mutableListOf<Int>() // 선택된 아이템 위치를 저장하는 리스트

    var mListener: OnItemClickListener? = null


    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_training_status, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).setDetails(position)
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return minOf(size, dataList.size)
    }

    // 뷰 홀더 클래스 정의
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setDetails(position: Int) {
            val trainingStatus = dataList[position]
            val groupUserImg: TextView = itemView.findViewById(R.id.tv_user_img)
            val groupUserName: TextView = itemView.findViewById(R.id.tv_user_name)
            val exercisePersent: TextView = itemView.findViewById(R.id.tv_exercise_percent)
            val tvDetail: TextView = itemView.findViewById(R.id.tv_detail)

            groupUserName.text = trainingStatus.userName
            groupUserImg.text = trainingStatus.userName?.substring(0, 1)

            if (status == 0) {
                exercisePersent.text =
                    if (trainingStatus.performanceIndex != null) {
                        val intValue = trainingStatus.performanceIndex.toDouble().toInt()
                        val formattedValue = String.format("%d%%", intValue)
                        formattedValue
                    } else {
                        "0%"
                    }
                tvDetail.text = itemView.context.getString(R.string.training_confirm)
            } else if (status == 1) {
                if (trainingStatus.nutritionIndex != null) {
                    if (trainingStatus.nutritionIndex <= 2) {
                        exercisePersent.setTextColor(ContextCompat.getColor(context, R.color.nutirition_bad_color))
                    } else if (trainingStatus.nutritionIndex < 4 && trainingStatus.nutritionIndex > 2) {
                        exercisePersent.setTextColor(ContextCompat.getColor(context, R.color.nutirition_normal_color))
                    } else if (trainingStatus.nutritionIndex >= 4) {
                        exercisePersent.setTextColor(ContextCompat.getColor(context, R.color.nutirition_good_color))
                    }
                }

                exercisePersent.text = if (trainingStatus.nutritionIndex != null) trainingStatus.nutritionIndex.toString() else "0"

                tvDetail.text = itemView.context.getString(R.string.diet_view)
            }
            itemView.setOnClickListener {
                mListener?.onTrainingStatusClicked(position, trainingStatus)
                Log.e("TAG", "setDetails: ")
            }
        }
    }

    // 데이터 업데이트를 위한 함수
    fun setData(newDataList: List<TrainingOverall>) {
        dataList.clear()
        dataList.addAll(newDataList)
        notifyDataSetChanged()
    }

    fun refreshData(){
        notifyDataSetChanged()
    }

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }


    interface OnItemClickListener {
        fun onTrainingStatusClicked(position: Int, trainingOverall: TrainingOverall)
    }

}
