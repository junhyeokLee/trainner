package com.sports2i.trainer.ui.adapter.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupUser

class GroupUserAdapter(private val dataList: MutableList<GroupUser>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedPositions = mutableListOf<Int>() // 선택된 아이템 위치를 저장하는 리스트

    var mListener: OnItemClickListener? = null


    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_user, parent, false)
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
            val users = dataList[position]
            val groupUserName: CheckedTextView = itemView.findViewById(R.id.ct_group_user_name)
            groupUserName.text = users.userName
            groupUserName.isChecked = selectedPositions.contains(position)

            val textColorRes = if (selectedPositions.contains(position)) R.color.white else R.color.primary
            groupUserName.setTextColor(ContextCompat.getColor(itemView.context, textColorRes))

            groupUserName.setOnClickListener{
                groupUserName.isChecked = !groupUserName.isChecked
                val textColorRes = if (groupUserName.isChecked) R.color.white else R.color.primary
                groupUserName.setTextColor(ContextCompat.getColor(it.context, textColorRes))
                mListener?.onUserChecked(position, users)
            }
        }
    }

    // 데이터 업데이트를 위한 함수
    fun setData(newDataList: List<GroupUser>) {
        dataList.clear()
        dataList.addAll(newDataList)
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = dataList[position]

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    // toggleSelection 함수 추가
     fun checkItem(position: Int) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position)
        } else {
            selectedPositions.add(position)
        }
        notifyItemChanged(position)
    }

    fun checkAllItem(isChecked: Boolean) {
        if (isChecked) {
            selectedPositions.clear()
            selectedPositions.addAll(dataList.indices)
        } else {
            selectedPositions.clear()
        }
        notifyDataSetChanged()
    }

    fun getSelectedData(): MutableList<GroupUser> {
        val selectedData = mutableListOf<GroupUser>()
        for (position in selectedPositions) {
            selectedData.add(dataList[position])
        }
        return selectedData
    }


    interface OnItemClickListener {
        fun onUserChecked(position: Int, grouUser: GroupUser)
    }

}
