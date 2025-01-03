package com.sports2i.trainer.ui.adapter.group

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingGroupStatus
import com.sports2i.trainer.data.model.TrainingInfo

class GroupTrainingStatusDetailItemAdapter(
    private val dataList: MutableList<TrainingGroupStatus.TrainingGroupStatusExercise>,
    private val isEditMode: Boolean
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedPositions = mutableListOf<Int>() // 선택된 아이템 위치를 저장하는 리스트

    var mListener: OnItemClickListener? = null
    var mDeleteListener : OnDeleteItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_training_status_detail_item, parent, false)
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

        val totalAchieveRate: TextView = itemView.findViewById(R.id.tv_percent)
        val exerciseName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraintLayout)
        val ibDelete: ImageButton = itemView.findViewById(R.id.ib_delete)
        val trainingTime: TextView = itemView.findViewById(R.id.tv_training_time)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mListener?.onTrainingStatusClicked(position, dataList[position])
                }
            }
            ibDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mDeleteListener?.onDeleteExercise(position, dataList[position])
                    selectedPositions.sortDescending() // 내림차순 정렬
                    dataList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, dataList.size) // 변경된 아이템 범위를 알림
                }
            }
        }

        fun setDetails(position: Int) {
            if (position >= 0 && position < dataList.size) {
                val trainingStatus = dataList[position]
//                ibDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE
                // 편집 모드일때
                if(isEditMode) {
                    ibDelete.visibility = View.VISIBLE
                    trainingTime.visibility = View.VISIBLE
                    totalAchieveRate.visibility = View.GONE
                    when(trainingStatus.trainingTime){
                        "T1" -> trainingTime.text = itemView.context.getString(R.string.dawn)
                        "T2" -> trainingTime.text = itemView.context.getString(R.string.morning)
                        "T3" -> trainingTime.text = itemView.context.getString(R.string.afternoon)
                        "T4" -> trainingTime.text = itemView.context.getString(R.string.dinner)
                        "T5" -> trainingTime.text = itemView.context.getString(R.string.night)
                    }
                }
                else{
                    ibDelete.visibility = View.GONE
                    trainingTime.visibility = View.GONE
                    totalAchieveRate.visibility = View.VISIBLE
                }


                val intValue = trainingStatus.totalAchieveRate.toDouble().toInt()
                val totalAchieveRateFormattedValue = String.format("%d%%", intValue)
                totalAchieveRate.text = totalAchieveRateFormattedValue
                exerciseName.text = trainingStatus.exerciseName

                val backgroundColorResId =
                    if(trainingStatus.totalAchieveRate?.toInt() == 0) R.color.white
                    else if(trainingStatus.totalAchieveRate?.toInt()!! <= 99) R.drawable.gradient_top_1to99_bottom
                    else if(trainingStatus.totalAchieveRate?.toInt()!! >= 100) R.drawable.gradient_top_to100_bottom
                    else R.color.white

                val deleteBtnColorResId =
                    if(trainingStatus.totalAchieveRate?.toInt() == 0) R.color.gray3
                    else if(trainingStatus.totalAchieveRate?.toInt()!! <= 99) R.color.white
                    else if(trainingStatus.totalAchieveRate?.toInt()!! >= 100) R.color.white
                    else R.color.black

                val textColorResId =
                    if(trainingStatus.totalAchieveRate?.toInt() == 0) R.color.black
                    else if(trainingStatus.totalAchieveRate?.toInt()!! <= 99) R.color.white
                    else if(trainingStatus.totalAchieveRate?.toInt()!! >= 100) R.color.white
                    else R.color.black


                constraintLayout.background = ContextCompat.getDrawable(itemView.context, backgroundColorResId as Int)

                totalAchieveRate.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        textColorResId
                    )
                )

                trainingTime.setTextColor(ContextCompat.getColor(itemView.context,textColorResId))

                exerciseName.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        textColorResId
                    )
                )

                ibDelete.setBackgroundTintList(
                    ColorStateList.valueOf(
                        itemView.resources.getColor(
                            deleteBtnColorResId
                        )
                    )
                )

            } else {
                Log.e("TAG", "position is out of range")
            }
        }
    }

    fun getSelectedData(): MutableList<TrainingGroupStatus.TrainingGroupStatusExercise> {
        val selectedData = mutableListOf<TrainingGroupStatus.TrainingGroupStatusExercise>()
        for (position in selectedPositions) {
            selectedData.add(dataList[position])
        }
        return selectedData
    }

    interface OnItemClickListener {
        fun onTrainingStatusClicked(position: Int, exercise: TrainingGroupStatus.TrainingGroupStatusExercise)
    }

    interface OnDeleteItemClickListener {
        fun onDeleteExercise(position: Int, exercise: TrainingGroupStatus.TrainingGroupStatusExercise)
    }

}
