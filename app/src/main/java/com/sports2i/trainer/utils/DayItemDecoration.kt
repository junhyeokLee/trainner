package com.sports2i.trainer.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DaysItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = space
        outRect.right = space
    }
}
