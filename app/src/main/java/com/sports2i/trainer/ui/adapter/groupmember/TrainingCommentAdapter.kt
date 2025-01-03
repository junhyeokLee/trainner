package com.sports2i.trainer.ui.adapter.groupmember

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.TrainingComment
import com.sports2i.trainer.utils.Preferences
import java.text.ParseException
import java.text.SimpleDateFormat


internal class TrainingCommentAdapter(
    private val context: Context,
    private val dataList:MutableList<TrainingComment>
) : RecyclerView.Adapter<TrainingCommentAdapter.ViewHolder>() {


    var mListener: OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trainingComment = dataList[position]
        Preferences.init(context, Preferences.DB_USER_INFO)

        holder.onBindData(trainingComment,position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    internal inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        var userName: TextView = itemView.findViewById(R.id.tv_user_name)
        var userImg : TextView = itemView.findViewById(R.id.tv_user_img)
        var contents : TextView = itemView.findViewById(R.id.tv_comment)
        var commentDate : TextView = itemView.findViewById(R.id.tv_comment_date)
        var commentDelete : TextView = itemView.findViewById(R.id.tv_comment_delete)

        fun onBindData(trainingComment: TrainingComment,position: Int) {

            userName.text = trainingComment.writerName
            userImg.text = trainingComment.writerName.substring(0,1)
            contents.text = trainingComment.contents

            // SimpleDateFormat을 사용하여 날짜 형식 지정
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")

            try {
                val date = inputFormat.parse(trainingComment.createdDate)
                val formattedDate = outputFormat.format(date)
                commentDate.text = formattedDate
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if(Preferences.string(Preferences.KEY_USER_ID).equals(trainingComment.writer)) commentDelete.visibility = View.VISIBLE
            else commentDelete.visibility = View.GONE


            commentDelete.setOnClickListener {
            Log.e("TAG", "onBindData: " )
                mListener?.onDeleteComment(position,trainingComment)
            }
        }
    }


    fun insertComment(trainingCommentList: MutableList<TrainingComment>) {
        val startPosition = dataList.size
        dataList.addAll(trainingCommentList)
        notifyItemRangeInserted(startPosition, trainingCommentList.size)
    }

    fun deleteComment(position: Int) {
        dataList.removeAt(position)
        notifyItemRemoved(position)
    }

    interface OnItemClickListener {
        fun onDeleteComment(position: Int,deletedComment: TrainingComment)
    }
}
