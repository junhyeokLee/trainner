package com.sports2i.trainer.ui.adapter.groupmember

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingGroupStatus

class ExerciseTimeAdapter(
    private val context: Context,
    private val dataList: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedPositions = mutableListOf<Int>() // 선택된 아이템 위치를 저장하는 리스트
    var mListener: OnItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_time, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).setDetails(position)
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return  dataList.size
    }

    // 뷰 홀더 클래스 정의
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setDetails(position:Int){
            val trainingStatus = dataList[position]
            val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraintLayout)
            val progressBar: CircularProgressIndicator = itemView.findViewById(R.id.progressBar)
            val percent: TextView = itemView.findViewById(R.id.tv_percent)
            val name: TextView = itemView.findViewById(R.id.tv_name)

            progressBar.progress = trainingStatus.totalAchieveRate!!.toInt()
            val intValue = trainingStatus.totalAchieveRate!!.toDouble().toInt()
            val formattedValue = String.format("%d%%", intValue)

            val backgroundColor =
                if(trainingStatus.totalAchieveRate?.toInt() == 0) R.color.white
                else if(trainingStatus.totalAchieveRate?.toInt()!! <= 99) R.drawable.gradient_top_1to99_bottom
                else if(trainingStatus.totalAchieveRate?.toInt()!! >= 100) R.drawable.gradient_top_to100_bottom
                else R.color.white

            val progressColorResId =
                 if(trainingStatus.totalAchieveRate?.toInt() == 0) R.color.tab_circle
                 else if(trainingStatus.totalAchieveRate?.toInt()!! <= 99) R.color.white
                 else if(trainingStatus.totalAchieveRate?.toInt()!! >= 100) R.color.white
                 else R.color.tab_circle

            val progressTrackColorResId =
                if(trainingStatus.totalAchieveRate?.toInt() == 0) R.color.tab_circle2
                else if(trainingStatus.totalAchieveRate?.toInt()!! <= 99) R.color.light_gray
                else if(trainingStatus.totalAchieveRate?.toInt()!! >= 100) R.color.light_gray
                else R.color.tab_circle2

            val textColorResId =
                if(trainingStatus.totalAchieveRate?.toInt() == 0) R.color.black
                else if(trainingStatus.totalAchieveRate?.toInt()!! <= 99) R.color.white
                else if(trainingStatus.totalAchieveRate?.toInt()!! >= 100) R.color.white
                else R.color.black

            constraintLayout.background = ContextCompat.getDrawable(context, backgroundColor)
            progressBar.trackColor = ContextCompat.getColor(context, progressTrackColorResId)
            progressBar.setIndicatorColor(ContextCompat.getColor(context, progressColorResId))
            percent.setTextColor(ContextCompat.getColor(context, textColorResId))
            name.setTextColor(ContextCompat.getColor(context, textColorResId))

            percent.text = formattedValue
            name.text = trainingStatus.exerciseName

            itemView.setOnClickListener {
                    mListener?.onExerciseClickListener(position,dataList)
            }

        }
    }

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    // 데이터를 업데이트하는 메서드 추가
    fun updateData(newData: List<TrainingGroupStatus.TrainingGroupStatusExercise>) {
        dataList.clear() // 현재 데이터 목록을 지우고
        dataList.addAll(newData) // 새 데이터로 업데이트
        notifyDataSetChanged() // 어댑터에 데이터가 업데이트되었음을 알림
    }

    interface OnItemClickListener {
        fun onExerciseClickListener(position: Int, trainingInfo:MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>)
    }



}
