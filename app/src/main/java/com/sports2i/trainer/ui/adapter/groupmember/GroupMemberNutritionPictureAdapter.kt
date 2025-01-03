package com.sports2i.trainer.ui.adapter.groupmember

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.NutritionPictureUser

class GroupMemberNutritionPictureAdapter(
    private val nutritionList: MutableList<NutritionPictureUser>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedPositions = mutableListOf<Int>() // 선택된 아이템 위치를 저장하는 리 스트

    var mListener: OnItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_member_nutrition_picture, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).setDetails(position)
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return nutritionList.size
    }

    // 뷰 홀더 클래스 정의
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setDetails(position: Int) {
            val nutritionStatus = nutritionList[position]
            val nutritionImg: ImageView = itemView.findViewById(R.id.iv_nutrition)
            val loadingProgressBar: ProgressBar = itemView.findViewById(R.id.loadingProgressBar)
            // 로딩 표시를 표시
            loadingProgressBar.visibility = View.VISIBLE

            // Glide를 사용하여 이미지 로드
            Glide.with(itemView)
                .load(nutritionStatus.pictureUrl)
                .transform(CenterCrop(), RoundedCorners(28)) // 라운드 처리
                .error(R.drawable.ic_x_24) // 에러 시 디폴트 이미지 표시
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // 이미지 로딩 실패 시 로딩 표시 숨김
                        loadingProgressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // 이미지 로딩 성공 시 로딩 표시 숨김
                        loadingProgressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(nutritionImg)


            itemView.setOnClickListener {
                mListener?.onNutritionGroupMemberStatusClicked(position, nutritionStatus)
                Log.e("TAG", "setDetails: ")
            }
        }
    }

    // 데이터 업데이트를 위한 함수
    fun setData(newDataList: List<NutritionPictureUser>) {
        nutritionList.clear()
        nutritionList.addAll(newDataList)
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = nutritionList[position]

    fun clearData() {
        nutritionList.clear()
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onNutritionGroupMemberStatusClicked(position: Int, nutrition: NutritionPictureUser)
    }

}
