package com.sports2i.trainer.ui.adapter.myexercise

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
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

class NutritionPictureDetailSearchAdapter(
    private val dataList: MutableList<NutritionPictureUser>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 두 가지 아이템 뷰 타입을 정의
    private val VIEW_TYPE_ADD_PICTURE = 0
    private val VIEW_TYPE_NORMAL = 1

    private var selectedPosition: Int = RecyclerView.NO_POSITION // 현재 선택한 포지션
    private var previousSelectedPosition: Int = RecyclerView.NO_POSITION // 이전에 선택한 포지션
    private var selectedImgPosition: Int = RecyclerView.NO_POSITION
    private val selectedPositions = mutableSetOf<Int>()
    private var isCheckboxVisible = false

    var mListener: OnItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> {
                // 데이터를 표시하는 뷰 홀더 생성
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_group_member_nutrition_picture_detail, parent, false)
                NormalViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 뷰 홀더에 데이터 설정
        when (holder) {
            is NormalViewHolder -> holder.bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 첫 번째 포지션에는 "사진 추가" 레이아웃을 사용하고, 나머지 포지션에는 데이터를 표시하는 레이아웃을 사용
        return VIEW_TYPE_NORMAL
    }

    override fun getItemCount(): Int {
        // "사진 추가" 아이템을 포함하여 아이템 개수 반환
        return dataList.size
    }

    inner class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nutritionCheck: CheckBox = itemView.findViewById(R.id.iv_check)
        val nutritionImg: ImageView = itemView.findViewById(R.id.iv_nutrition)
        val loadingProgressBar: ProgressBar = itemView.findViewById(R.id.loadingProgressBar)
        val checkLayout : CheckBox = itemView.findViewById(R.id.iv_check_layout)
        val evaluationComplete : LinearLayout = itemView.findViewById(R.id.ll_evaluation_complete)

        fun bind(position: Int) {
            val nutritionStatus = dataList[position]
            // 체크박스 표시 여부 설정
            nutritionCheck.visibility = if (isCheckboxVisible) View.VISIBLE else View.GONE
            loadingProgressBar.visibility = View.VISIBLE

            when (nutritionStatus.evaluation) {
                0 -> evaluationComplete.visibility = View.GONE
                else -> evaluationComplete.visibility = View.VISIBLE
            }

            // Glide를 사용하여 이미지 로드
            Glide.with(itemView)
                .load(nutritionStatus.pictureUrl)
                .transform(CenterCrop(), RoundedCorners(32)) // 라운드 처리
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
                    } else {
                        selectedPositions.add(position)
                        nutritionCheck.isChecked = true // 체크박스 활성화
                    }
                }

                selectedImgPosition = position
                notifyDataSetChanged()
                mListener?.onNutritionDetailSearchStatusClicked(position, dataList)
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

                mListener?.onNutritionDetailSearchStatusLongClicked(position, dataList,isCheckboxVisible)
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
    fun setData(newDataList: MutableList<NutritionPictureUser>) {
        dataList.clear()
        dataList.addAll(newDataList)
        Log.e("setData", "${dataList.toString()}")
        notifyDataSetChanged()
    }

    fun addData(newData: NutritionPictureUser) {
        dataList.add(newData)
        Log.e("addData", "${dataList.toString()}")
        notifyDataSetChanged()
    }

//    fun deleteData(position: Int){
//        imageList.removeAt(position)
//        notifyDataSetChanged()
//    }

    fun deleteData(image: NutritionPictureUser) {
        dataList.remove(image)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): NutritionPictureUser? {
        return if (position in 0 until dataList.size) {
            dataList[position]
        } else {
            null
        }
    }

    fun getImageList(): MutableList<NutritionPictureUser> {
        return dataList
    }

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    // 선택된 아이템 포지션 목록 반환
    fun getSelectedPositions(): Set<Int> {
        return selectedPositions
    }

    interface OnItemClickListener {
        fun onNutritionDetailSearchStatusClicked(position: Int, nutritionPictureUser: MutableList<NutritionPictureUser>)
        fun onNutritionDetailSearchStatusLongClicked(position: Int, nutritionPictureUserList: MutableList<NutritionPictureUser>,nutritionCheck:Boolean)
    }

}