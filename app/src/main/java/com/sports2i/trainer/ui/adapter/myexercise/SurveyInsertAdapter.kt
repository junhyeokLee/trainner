package com.sports2i.trainer.ui.adapter.myexercise

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.ExerciseGraph
import com.sports2i.trainer.data.model.ExercisePreset
import com.sports2i.trainer.data.model.Graph
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.SurveyInsert
import com.sports2i.trainer.data.model.SurveyPreset
import com.sports2i.trainer.data.model.TrainingExercise
import com.sports2i.trainer.data.model.TrainingInfo
import com.sports2i.trainer.data.model.TrainingInfoResponse
import com.sports2i.trainer.ui.widget.GraphTrainingOverall


class SurveyInsertAdapter(
    private val context: Context,
    private val dataList: MutableList<SurveyPreset>,
    private val surveyInsertList: MutableList<SurveyInsert>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_survey_insert, parent, false)
        return SurveyPresetViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SurveyPresetViewHolder) {
            val surveyPreset = dataList[position]
            val surveyInsert = surveyInsertList[position]
            holder.bind(surveyPreset,surveyInsert)
        }
    }
    override fun getItemCount(): Int {
        return dataList.size
    }



    inner class SurveyPresetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_title)
        private val linearLayout: LinearLayout = itemView.findViewById(R.id.linearProgress)
         val progressBar: SeekBar = itemView.findViewById(R.id.progressSeekBar)

        fun bind(surveyPreset: SurveyPreset,surveyInsert: SurveyInsert) {
            nameTextView.text = surveyPreset.surveyItemName

            val textViewList = mutableListOf<TextView>()

            for (i in 1..10) {
                val textView = TextView(itemView.context)
                textView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                textView.text = i.toString()
                textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray2))
                textView.gravity = Gravity.CENTER
                textView.textSize = 12f
                textViewList.add(textView)
                linearLayout.addView(textView)
            }

            progressBar.progress = 5 // 초기 값 설정
            surveyInsert.surveyValue = progressBar.progress

            // Update text color based on initial progress
            for (i in 0 until textViewList.size) {
                textViewList[i].setTextColor(
                    if (i + 1 <= progressBar.progress) {
                        ContextCompat.getColor(context, R.color.primary)
                    } else {
                        ContextCompat.getColor(context, R.color.gray2)
                    }
                )
            }


            progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    surveyInsert.surveyValue = progressBar.progress
                    // Update text color based on progress
                    for (i in 0 until textViewList.size) {
                        textViewList[i].setTextColor(
                            if (i + 1 <= progress) {
                                // Set the desired color for values less than or equal to progress
                                ContextCompat.getColor(itemView.context, R.color.primary)
                            } else {
                                // Set the color for values greater than progress
                                ContextCompat.getColor(itemView.context, R.color.gray2)
                            }
                        )
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }

    fun getSurveyInsert(): MutableList<SurveyInsert> {
        return surveyInsertList
    }

    fun addData(surveyPreset: SurveyPreset) {
        dataList.add(surveyPreset)
        notifyDataSetChanged()
    }

    fun addListData(surveyPresetList: List<SurveyPreset>) {
        dataList.clear()
        dataList.addAll(surveyPresetList)
        notifyDataSetChanged()
    }

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }
}
