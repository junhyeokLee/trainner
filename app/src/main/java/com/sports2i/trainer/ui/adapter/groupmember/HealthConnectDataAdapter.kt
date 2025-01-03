package com.sports2i.trainer.ui.adapter.groupmember

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.HealthData
import com.sports2i.trainer.data.model.TrainingInfo


class HealthConnectDataAdapter(private val context: Context,private val dataList: MutableList<HealthData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_health_data_item, parent, false)
        return HealthConnectViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HealthConnectViewHolder) {
            val exerciseList = dataList[position]
            holder.bind(exerciseList)
        }
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return dataList.size
    }

    fun addItem(item: HealthData) {
        dataList.add(item)
        notifyDataSetChanged()
    }

    fun addItems(items: List<HealthData>){
        dataList.clear()
        dataList.addAll(items)
        notifyDataSetChanged()
    }

    inner class HealthConnectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_type_name)
        private val value: TextView = itemView.findViewById(R.id.tv_type_value)
        private val unit: TextView = itemView.findViewById(R.id.tv_type_unit)

        fun bind(healthData: HealthData) {

            when(healthData.type){
                "energy_total" -> {
                    name.text = "소비 칼로리"
                    unit.text = "kcal"
                }
                "speed_avg" -> {
                    name.text = "평균 속도"
                    unit.text = "km/h"
                }
                "speed_max" -> {
                    name.text = "최대 속도"
                    unit.text = "km/h"
                }
                "speed_min" -> {
                    name.text = "최저 속도"
                    unit.text = "km/h"
                }
                "distance_total" -> {
                    name.text = "이동 거리"
                    unit.text = "km"
                }
                "bpm_avg" -> {
                    name.text = "평균 심박수"
                    unit.text = "bpm"
                }
                "bpm_max" -> {
                    name.text = "최대 심박수"
                    unit.text = "bpm"
                }
                "bpm_min" -> {
                    name.text = "최저 심박수"
                    unit.text = "bpm"
                }
                else -> {}
            }

            value.text = healthData.value
        }
    }

}
