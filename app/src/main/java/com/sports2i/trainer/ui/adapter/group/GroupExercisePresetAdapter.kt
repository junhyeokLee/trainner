package com.sports2i.trainer.ui.adapter.group

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExercisePreset


class GroupExercisePresetAdapter(
    private val context: Context,
    private val dataList: MutableList<ExercisePreset>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mListener: OnDeleteItemClickListener? = null
    var isEditMode = false
        set(value) {
            field = value
            notifyDataSetChanged() // isEditMode 값 변경 시 어댑터를 갱신합니다.
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_group_exercise_preview, parent, false)
        return GroupExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is com.sports2i.trainer.ui.adapter.group.GroupExercisePresetAdapter.GroupExerciseViewHolder) {
            val trainingInfo = dataList[position]
            holder.bind(trainingInfo)
        }
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class GroupExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.exercise_name_textview)
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.rv_exercise_unit)
        private val deleteImageButton: ImageButton = itemView.findViewById(R.id.ib_delete)

        fun bind(exercisePreset: ExercisePreset) {
            deleteImageButton.visibility = View.GONE

            nameTextView.text = exercisePreset.exerciseList[0].exerciseName
            val exerciseUnitAdapter =
                com.sports2i.trainer.ui.adapter.group.GroupExerciseUnitPreviewAdapter(
                    context,
                    exercisePreset.exerciseList
                )
            recyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            recyclerView.adapter = exerciseUnitAdapter
        }
    }


    fun deleteItemAndSection(position: Int, sectionName: String) {
        if (position >= 0 && position < dataList.size && dataList[position].trainingTime == sectionName) {
            dataList.removeAt(position)
            notifyDataSetChanged()
        }
    }
    fun addData(exercisePreset: ExercisePreset) {

        dataList.add(exercisePreset)
        notifyDataSetChanged()
    }



    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    interface OnDeleteItemClickListener {
        fun onDeleteExercise(position: Int, deletedTimeItemName: String)
    }
}
