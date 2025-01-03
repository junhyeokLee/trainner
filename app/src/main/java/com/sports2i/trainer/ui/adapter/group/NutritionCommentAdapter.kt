package com.sports2i.trainer.ui.adapter.group

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sports2i.trainer.R
import com.sports2i.trainer.ui.view.AnimatedExpandableListView
import java.util.*

class NutritionCommentAdapter(
    private val context: Context,
    private val groupData: List<String>,
    private val childData: List<List<String>>
) : AnimatedExpandableListView.AnimatedExpandableListAdapter() {

    private val nutritionData: MutableMap<Pair<Int, Int>, Pair<String, Boolean>> = HashMap()
    private var checkBoxListener: OnCheckBoxStateChangedListener? = null

    interface OnCheckBoxStateChangedListener {
        fun onCheckBoxStateChanged(groupPosition: Int, childPosition: Int, type: String, isChecked: Boolean)
    }

    fun setOnCheckBoxStateChangedListener(listener: OnCheckBoxStateChangedListener) {
        checkBoxListener = listener
    }

    override fun getGroupCount(): Int {
        return groupData.size
    }

//    override fun getChildrenCount(groupPosition: Int): Int {
//        return 1
//    }

    override fun getGroup(groupPosition: Int): Any {
        return groupData[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return childData[groupPosition][0]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        return createGroupView(groupPosition, isExpanded, convertView)
    }

//    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
//        return createChildView(groupPosition, childPosition, convertView)
//    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getRealChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        return createChildView(groupPosition, childPosition, convertView)
    }

    override fun getRealChildrenCount(groupPosition: Int): Int {
        return 1
    }

    private fun createGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?): View {
        val groupText = getGroup(groupPosition) as String
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_nutrition_comment, null)
        val groupTextView = view.findViewById<TextView>(R.id.tv_nutrition_direction)
        val expandIndicator = view.findViewById<ImageView>(R.id.iv_nutirition_indicator)
        val layoutNutritionDirection = view.findViewById<LinearLayout>(R.id.layout_nutrition_direction)
        val tvNutritionValue = view.findViewById<TextView>(R.id.tv_nutrition_value)

        val imageResourceId: Int = if (isExpanded) R.drawable.expand_item_down else R.drawable.expand_item_up
        expandIndicator.setBackgroundResource(imageResourceId)

        val key = Pair(groupPosition, 0)

        if (nutritionData.containsKey(key)) {
            val (nutritionValue, isChecked) = nutritionData[key]!!
            if (isChecked) {
                layoutNutritionDirection.setBackgroundResource(R.drawable.gradient_survey_item)
                groupTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                expandIndicator.backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
                tvNutritionValue.text = nutritionValue
            } else {
                layoutNutritionDirection.setBackgroundResource(R.color.white)
                groupTextView.setTextColor(ContextCompat.getColor(context, R.color.black))
                expandIndicator.backgroundTintList = ContextCompat.getColorStateList(context, R.color.black)
                tvNutritionValue.text = ""
            }
        } else {
            // Handle the case when nutritionData doesn't contain the key
        }

        groupTextView.text = groupText

        return view
    }

    private fun createChildView(groupPosition: Int, childPosition: Int, convertView: View?): View {
        val childText = getChild(groupPosition, childPosition) as String
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_nutrition_comment_item, null)

        val childExcess = view.findViewById<CheckBox>(R.id.checkboxExcess)
        val childDeficient = view.findViewById<CheckBox>(R.id.checkboxDeficient)

        val key = Pair(groupPosition, childPosition)

        childExcess.tag = "과다"
        childDeficient.tag = "부족"

        val (childType, isChecked) = nutritionData[key] ?: Pair("", false)

        childExcess.text = "과다"
        childExcess.isChecked = isChecked && childType == "과다"
        childDeficient.text = "부족"
        childDeficient.isChecked = isChecked && childType == "부족"

        childExcess.setOnCheckedChangeListener { buttonView, isChecked ->
            val type = buttonView.tag as String
            val newChecked = isChecked
            nutritionData[key] = Pair(type, newChecked)
            checkBoxListener?.onCheckBoxStateChanged(groupPosition, childPosition, type, newChecked)
            notifyDataSetChanged()
        }

        childDeficient.setOnCheckedChangeListener { buttonView, isChecked ->
            val type = buttonView.tag as String
            val newChecked = isChecked
            nutritionData[key] = Pair(type, newChecked)
            checkBoxListener?.onCheckBoxStateChanged(groupPosition, childPosition, type, newChecked)
            notifyDataSetChanged()
        }

        return view
    }

}
