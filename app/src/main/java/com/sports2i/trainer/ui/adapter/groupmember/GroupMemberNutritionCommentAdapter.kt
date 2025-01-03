package com.sports2i.trainer.ui.adapter.groupmember

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.NutritionDirectionKeyword

class GroupMemberNutritionCommentAdapter(private val dataList: MutableList<NutritionDirectionKeyword>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mListener: OnItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nutrition_comment_group_member, parent, false)
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
        fun setDetails(position:Int){
            val nutritionDirectionKeyword = dataList[position]
            val nutritionDirection = itemView.findViewById<TextView>(R.id.tv_nutrition_direction)
            val nutritionStatus = itemView.findViewById<TextView>(R.id.tv_nutrition_value)

            nutritionDirection.text = nutritionDirectionKeyword.item
            nutritionStatus.text = nutritionDirectionKeyword.status

        }
    }

    // 데이터 업데이트를 위한 함수
    fun setData(newDataList: List<NutritionDirectionKeyword>) {
        dataList.clear()
        dataList.addAll(newDataList)
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = dataList[position]

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onNutritionDirectionKeywordClicked(position: Int, nutritionDirectionKeyword: NutritionDirectionKeyword)
    }

}
