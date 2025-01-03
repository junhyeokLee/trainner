package com.sports2i.trainer.ui.adapter.groupmember

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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


class GroupMemberNutritionPictureDetailAdapter(
    private val nutritionList: MutableList<NutritionPictureUser>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION // 현재 선택한 포지션
    private var previousSelectedPosition: Int = RecyclerView.NO_POSITION // 이전에 선택한 포지션
    private var selectedImgPosition: Int = RecyclerView.NO_POSITION
    private val selectedPositions = mutableSetOf<Int>()
    private var isCheckboxVisible = false

    var mListener: OnItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_member_nutrition_picture_detail, parent, false)
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

        val nutritionCheck: CheckBox = itemView.findViewById(R.id.iv_check)
        val nutritionImg: ImageView = itemView.findViewById(R.id.iv_nutrition)
        val loadingProgressBar: ProgressBar = itemView.findViewById(R.id.loadingProgressBar)
        val checkLayout : CheckBox = itemView.findViewById(R.id.iv_check_layout)

        fun setDetails(position: Int) {
            val nutritionStatus = nutritionList[position]
            // 체크박스 표시 여부 설정
            nutritionCheck.visibility = if (isCheckboxVisible) View.VISIBLE else View.GONE
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

            // 아이템을 클릭하면 체크 상태를 변경
            nutritionCheck.isChecked = selectedPositions.contains(position)

            nutritionImg.setOnClickListener {
                // 아이템을 클릭하면 체크 상태를 변경하고 선택된 포지션 목록을 업데이트
                if (isCheckboxVisible) {
                    previousSelectedPosition = selectedPosition // 이전 선택 포지션 저장
                    selectedPosition = position // 현재 선택 포지션 업데이트

                    if (selectedPositions.contains(position)) {
                        selectedPositions.remove(position)
                        nutritionCheck.isChecked = false // 체크박스 비활성화
//                        nutritionImg.setBackgroundResource(0)
                    } else {
                        selectedPositions.add(position)
                        nutritionCheck.isChecked = true // 체크박스 활성화
                    }
                }

                selectedImgPosition = position
                notifyDataSetChanged()
                mListener?.onNutritionGroupMemberStatusClicked(position, nutritionStatus)

            }

            nutritionImg.setOnLongClickListener {
                // 롱클릭 시 체크박스 표시 모드로 전환
                if (!isCheckboxVisible) {
                    // 이전에 선택된 모든 아이템의 체크박스 상태를 해제
                    for (position in selectedPositions) {
                        notifyItemChanged(position) // 각 아이템의 체크박스 상태 갱신
                    }
                    selectedPositions.clear() // 선택된 포지션 목록 비움
                    isCheckboxVisible = true
                    notifyDataSetChanged() // 체크박스 상태 갱신
                } else {
                    isCheckboxVisible = false
                    notifyDataSetChanged() // 체크박스 상태 갱신
                }

                // 롱클릭한 위치의 아이템을 자동으로 체크
                if (!selectedPositions.contains(position)) {
                    selectedPositions.add(position)
                    notifyDataSetChanged() // 체크박스 상태 갱신
                }

                mListener?.onNutritionGroupMemberStatusLongClicked(position, nutritionStatus,isCheckboxVisible)
                true
            }

            // 선택된 아이템인 경우 배경 설정
            if (position == selectedImgPosition) {
                nutritionImg.setBackgroundResource(R.drawable.bg_nutrition_clickable)
            } else {
                nutritionImg.setBackgroundResource(0)
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

    // 선택된 아이템 포지션 목록 반환
    fun getSelectedPositions(): Set<Int> {
        return selectedPositions
    }

    interface OnItemClickListener {
        fun onNutritionGroupMemberStatusClicked(position: Int, nutrition: NutritionPictureUser)
        fun onNutritionGroupMemberStatusLongClicked(position: Int, nutrition: NutritionPictureUser,nutritionCheck:Boolean)
    }

}
