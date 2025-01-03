package com.sports2i.trainer.ui.widget.viewholder

import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.interfaces.TypeListener

class TypeViewHolder(itemView: View, private val typeListener: TypeListener): RecyclerView.ViewHolder(itemView) {

    fun bind(position: Int) {
        typeListener.onType(itemView.rootView as LinearLayoutCompat, position)
    }
}