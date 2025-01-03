package com.sports2i.trainer.ui.adapter.groupmember

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExerciseRecentAchievementGraph
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.ui.widget.GraphTrainingOverall


class TrainingRecentAchievementGraphAdapter(
    private val context: Context,
    private val dataList: MutableList<ExerciseRecentAchievementGraph>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recent_achievement_graph, parent, false)
        return TrainingRecentAchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TrainingRecentAchievementViewHolder) {
            Log.e("TrainingRecentAchievementViewHolder", "onBindViewHolder")
            val trainingInfo = dataList[position]
            holder.bind(trainingInfo)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class TrainingRecentAchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_units)
        private val graphLayout: LinearLayoutCompat = itemView.findViewById(R.id.recent_achievement_graph_layout)

        fun bind(recentAchievement: ExerciseRecentAchievementGraph) {
            Log.e("recentAchievement", recentAchievement.toString())

            nameTextView.text = recentAchievement.trainingUnitName+"(${recentAchievement.trainingUnit})"

            fun updateGraphList(selectedList: List<Graph>) {
                graphLayout.removeAllViews()
                graphLayout.addView(GraphTrainingOverall(context, 1, "date", selectedList))
            }

            val graphList = createExerciseGraph(recentAchievement)
            updateGraphList(graphList)
        }
    }


    fun addData(trainingExercise: ExerciseRecentAchievementGraph) {
        dataList.add(trainingExercise)
        notifyDataSetChanged()
    }

    fun addListData(groupedExerciseList: List<ExerciseRecentAchievementGraph>) {
        dataList.clear()
        dataList.addAll(groupedExerciseList)
        notifyDataSetChanged()
    }


    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }


    private fun createExerciseGraph(trainingInfoResponse: ExerciseRecentAchievementGraph): List<Graph> {
        val unitName = trainingInfoResponse.trainingUnitName!!
        val unit = trainingInfoResponse.trainingUnit!!
//        val totalAchieveRates = trainingInfoResponse.totalAchieveRates ?: emptyList()
        val achieveRates = trainingInfoResponse.achieveRates ?: emptyList()

        // 각 date에 대한 Graph 생성
        val graphList = achieveRates.mapIndexed { index, achieveRates ->
            val date = trainingInfoResponse.trainingDates.getOrElse(index) { "" }
            Graph(unitName, date, achieveRates.toInt())
        }
        return graphList
    }
}
