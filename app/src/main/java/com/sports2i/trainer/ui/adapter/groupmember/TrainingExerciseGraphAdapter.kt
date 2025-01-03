package com.sports2i.trainer.ui.adapter.groupmember

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExerciseGraph
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.TrainingExercise
import com.sports2i.trainer.data.model.TrainingInfo
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.ui.widget.GraphTrainingOverall


class TrainingExerciseGraphAdapter(
    private val context: Context,
    private val dataList: MutableList<ExerciseGraph>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_group_exercise_graph, parent, false)
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
        private val graphLayout: LinearLayoutCompat = itemView.findViewById(R.id.exercise_graph_layout)

        fun bind(trainingExercise: ExerciseGraph) {

            nameTextView.text = trainingExercise.exerciseName
            fun updateGraphList(selectedList: List<Graph>) {
                graphLayout.removeAllViews()
                graphLayout.addView(GraphTrainingOverall(context, 3, "date", selectedList))
            }

            val graphList = createExerciseGraph(trainingExercise)

            updateGraphList(graphList)
        }
    }


    fun addData(trainingExercise: ExerciseGraph) {
        dataList.add(trainingExercise)
        notifyDataSetChanged()
    }

    fun addListData(groupedExerciseList: List<ExerciseGraph>) {
        dataList.clear()
        dataList.addAll(groupedExerciseList)
        notifyDataSetChanged()
    }

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }


    private fun createExerciseGraph(trainingInfoResponse: ExerciseGraph): List<Graph> {
        val exerciseId = trainingInfoResponse.exerciseName!!
        val totalAchieveRates = trainingInfoResponse.exerciseAchieveRate ?: emptyList()

        // 각 date에 대한 Graph 생성
        val graphList = totalAchieveRates.mapIndexed { index, totalAchieveRate ->
            val date = trainingInfoResponse.trainingDates.getOrElse(index) { "" }
//            Graph(exerciseId, date, (totalAchieveRate * 100).toInt())
            Graph(exerciseId, date, (totalAchieveRate).toInt())
        }

        return graphList
    }
}
