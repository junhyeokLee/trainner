package com.sports2i.trainer.ui.adapter.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sports2i.trainer.R
import com.sports2i.trainer.interfaces.TypeListener
import com.sports2i.trainer.ui.widget.viewholder.TypeViewHolder

class TypeAdapter(val size: Int, private val typeListener: TypeListener): RecyclerView.Adapter<TypeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TypeViewHolder((LayoutInflater.from(parent.context).inflate(R.layout.item_type, parent, false)), this.typeListener)

    override fun getItemCount(): Int = this.size

    override fun onBindViewHolder(holder: TypeViewHolder, position: Int) {
        holder.bind(position)
    }
}