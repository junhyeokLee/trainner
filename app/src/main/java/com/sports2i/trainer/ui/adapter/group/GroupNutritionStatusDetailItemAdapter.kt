package com.sports2i.trainer.ui.adapter.group

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Nutrition
import com.sports2i.trainer.data.model.TrainingGroupStatus
import com.sports2i.trainer.data.model.TrainingInfo

class GroupNutritionStatusDetailItemAdapter(
    private val dataList: MutableList<Nutrition.NutritionPicture>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mListener: OnItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_nutrition_status_detail_item, parent, false)
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
        fun setDetails(position: Int) {
            val nutritionStatus = dataList[position]
            val nutritionImg: ImageView = itemView.findViewById(R.id.iv_nutrition)
            val loadingProgressBar: ProgressBar = itemView.findViewById(R.id.loadingProgressBar)

            // 로딩 표시를 표시
            loadingProgressBar.visibility = View.VISIBLE

            // Glide를 사용하여 이미지 로드
            Glide.with(itemView)
                .load(nutritionStatus.pictureUrl)
                .transform(CenterCrop(), RoundedCorners(12)) // 라운드 처리
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

            // 그림자 효과 추가 (API 21 이상에서만 지원)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val elevation = 6f // 그림자 크기
                nutritionImg.elevation = elevation
            }


            itemView.setOnClickListener {
                mListener?.onTrainingStatusClicked(position,nutritionStatus)
            }
        }
    }



    interface OnItemClickListener {
        fun onTrainingStatusClicked(position: Int, nutrition: Nutrition.NutritionPicture)
    }

}
