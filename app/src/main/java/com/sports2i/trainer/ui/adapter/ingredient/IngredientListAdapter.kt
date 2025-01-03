package com.sports2i.trainer.ui.adapter.ingredient

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Ingredient
import com.sports2i.trainer.data.model.Notice
import com.sports2i.trainer.utils.DateTimeUtil

class IngredientListAdapter(private val dataList: MutableList<Ingredient>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mListener: OnItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine, parent, false)
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
            val ingredient = dataList[position]

            val title: TextView = itemView.findViewById(R.id.tv_medicine_title)

            if(dataList.isNullOrEmpty()) title.text = "등록된 데이터가 없습니다."
            else title.text = ingredient.itemName

            itemView.setOnClickListener {
                mListener?.onIngredientClicked(position, ingredient)
            }

        }
    }

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }


    interface OnItemClickListener {
        fun onIngredientClicked(position: Int, ingredient: Ingredient)
    }

}
