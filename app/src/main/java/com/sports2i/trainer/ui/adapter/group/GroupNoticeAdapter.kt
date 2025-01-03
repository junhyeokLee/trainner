package com.sports2i.trainer.ui.adapter.group

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Notice
import com.sports2i.trainer.utils.DateTimeUtil

class GroupNoticeAdapter(private val dataList: MutableList<Notice>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mListener: OnItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notice, parent, false)
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
            val notice = dataList[position]
            val noticeGroupId: TextView = itemView.findViewById(R.id.tv_group_id)
            val noticeTitle: TextView = itemView.findViewById(R.id.tv_notice_title)
            val noticeCommentCount: TextView = itemView.findViewById(R.id.tv_notice_comment_count)
            val noticeWriter: TextView = itemView.findViewById(R.id.tv_notice_writer)
            val noticeDate: TextView = itemView.findViewById(R.id.tv_notice_date)

            if(notice.groupId.equals("ALL")){
                noticeGroupId.text = "[전체]"
            }else{
                noticeGroupId.text = "["+notice.groupId+"]"
            }
            noticeTitle.text = notice.title
            noticeCommentCount.text = "["+notice.numOfComments.toString()+"]"
            noticeWriter.text = notice.writerName

            noticeDate.text = DateTimeUtil.getMonthDayFormattedDate(notice.createdDate!!)


            itemView.setOnClickListener {
                mListener?.onNoticeClicked(position, notice)
            }
        }


    }

    // 데이터 업데이트를 위한 함수
    fun setData(newDataList: List<Notice>) {
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
        fun onNoticeClicked(position: Int, notice: Notice)
    }

}
