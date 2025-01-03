package com.sports2i.trainer.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import com.sports2i.trainer.R
import com.sports2i.trainer.utils.Preferences

class CustomSpinner(context: Context, attrs: AttributeSet?) : AppCompatSpinner(context, attrs) {
    private lateinit var adapter: CustomSpinnerAdapter<*>

    init {
        // 기본 값 설정
        Preferences.init(context, Preferences.DB_USER_INFO)

        val popupBackgroundResId = R.drawable.bg_custom_spinner
        val dropDownVerticalOffset = resources.getDimensionPixelSize(R.dimen.default_drop_down_vertical_offset)

        setPopupBackgroundResource(popupBackgroundResId)
        setDropDownVerticalOffset(dropDownVerticalOffset)
    }

    fun <T> setAdapterData(dataList: List<T>, itemBinder: ItemBinder<T>) {
        adapter = CustomSpinnerAdapter(context, R.layout.item_drop_down_default_option, dataList, itemBinder)
        adapter.setDropDownViewResource(R.layout.item_drop_down_option)
        super.setAdapter(adapter)
    }

    fun setSelectedItem(position: Int) {
        setSelection(position)
    }

    fun notifyData() {
        adapter.notifyDataSetChanged()
    }
}
