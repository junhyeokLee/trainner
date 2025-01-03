package com.sports2i.trainer.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import com.sports2i.trainer.databinding.ItemDropDownOptionBinding

interface ItemBinder<T> {
    fun bindItem(view: View, item: T, isDropDown: Boolean)
}

class CustomSpinnerAdapter<T>(
    context: Context,
    @LayoutRes private val resId: Int,
    private val itemList: List<T>,
    private val itemBinder: ItemBinder<T>
) : ArrayAdapter<T>(context, resId, itemList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemDropDownOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinder.bindItem(binding.root, itemList[position], isDropDown = false)
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemDropDownOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinder.bindItem(binding.root, itemList[position], isDropDown = true)
        return binding.root
    }
}


