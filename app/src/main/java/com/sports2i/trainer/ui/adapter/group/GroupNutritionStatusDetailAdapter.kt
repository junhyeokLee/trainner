package com.sports2i.trainer.ui.adapter.group

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.Nutrition

class GroupNutritionStatusDetailAdapter(
    private val nutritionList: MutableList<Nutrition>
    ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedPositions = mutableListOf<Int>() // 선택된 아이템 위치를 저장하는 리스트
    private lateinit var groupTrainingStatusDetailItemAdapter: GroupNutritionStatusDetailItemAdapter

    var mListener: OnItemClickListener? = null

    // 뷰 홀더 생성 및 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_nutrition_status_detail, parent, false)
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
            val nutrition = nutritionList[position]
            val groupUserImg: TextView = itemView.findViewById(R.id.tv_user_img)
            val groupUserName: TextView = itemView.findViewById(R.id.tv_user_name)
            val rvTrainingStatus: RecyclerView = itemView.findViewById(R.id.rv_nutrition_status)
            val emptyTrainingData:TextView = itemView.findViewById(R.id.tv_empty_nutrition_data)

            val userName = nutrition.userName

            if (userName.length > 3) { // 3글자 이상 ...
                groupUserName.text = userName.substring(0, 3) + "..."
            } else {
                groupUserName.text = userName
            }

            groupUserImg.text = userName.substring(0, 1)

            val nutritionPictureList = nutrition.nutritionList

            if(nutritionPictureList.size == 0 || nutritionPictureList.isEmpty()) emptyTrainingData.visibility = View.VISIBLE
            else emptyTrainingData.visibility = View.GONE

            groupTrainingStatusDetailItemAdapter = GroupNutritionStatusDetailItemAdapter(nutrition.nutritionList)


            groupTrainingStatusDetailItemAdapter.mListener = object : GroupNutritionStatusDetailItemAdapter.OnItemClickListener{
                override fun onTrainingStatusClicked(position: Int, nutritionPicture: Nutrition.NutritionPicture) {
                    mListener?.onNutritionGroupStatusClicked(position,nutrition,nutritionPicture)
                    Log.e("TAG", "onTrainingStatusClicked: " +nutrition.userId)
                }
            }

            rvTrainingStatus.apply {
                layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                adapter = groupTrainingStatusDetailItemAdapter
            }


        }
    }

    // 데이터 업데이트를 위한 함수
    fun setData(newDataList: List<Nutrition>) {
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
        fun onNutritionGroupStatusClicked(position: Int, nutrition: Nutrition,nutritionPicture: Nutrition.NutritionPicture)
    }

}
