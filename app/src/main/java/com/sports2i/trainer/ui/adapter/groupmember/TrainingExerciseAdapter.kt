package com.sports2i.trainer.ui.adapter.groupmember

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.TrainingExercise
import com.sports2i.trainer.data.model.TrainingInfo
import com.sports2i.trainer.data.model.TrainingInfoResponse


class TrainingExerciseAdapter(
    private val context: Context,
    private val dataList: MutableList<TrainingExercise>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mListener: OnItemClickListener? = null
    var isEditMode = false
        set(value) {
            field = value
            notifyDataSetChanged() // isEditMode 값 변경 시 어댑터를 갱신합니다.
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_group_exercise_preview, parent, false)
        return TrainingExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TrainingExerciseViewHolder) {
            val trainingInfo = dataList[position]
            holder.bind(trainingInfo)
        }
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class TrainingExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.exercise_name_textview)
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.rv_exercise_unit)
        private val deleteImageButton: ImageButton = itemView.findViewById(R.id.ib_delete)

        fun bind(trainingExercise: TrainingExercise) {
            deleteImageButton.visibility = View.GONE

            nameTextView.text = trainingExercise.exerciseList[0].exerciseName
            val exerciseUnitAdapter = TrainingExerciseUnitAdapter(context, trainingExercise.exerciseList)
            recyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            recyclerView.adapter = exerciseUnitAdapter

            itemView.setOnClickListener {
                mListener?.onItemClick(position,trainingExercise)
            }

            exerciseUnitAdapter.mListener = object : TrainingExerciseUnitAdapter.OnItemClickListener {
                override fun onItemClick() {
                    mListener?.onItemClick(position,trainingExercise)
                }
            }

        }
    }


    fun deleteItemAndSection(position: Int, sectionName: String) {
        if (position >= 0 && position < dataList.size && dataList[position].trainingTime == sectionName) {
            dataList.removeAt(position)
            notifyDataSetChanged()
        }
    }
    fun addData(trainingExercise: TrainingExercise) {

        dataList.add(trainingExercise)
        notifyDataSetChanged()
    }



    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, trainingExercise: TrainingExercise)
    }
    interface OnDeleteItemClickListener {
        fun onDeleteExercise(position: Int, deletedTimeItemName: String)
    }
}
