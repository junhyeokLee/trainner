package com.sports2i.trainer.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

// Kotlin, RecyclerView 버전

class DisallowInterceptRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

            when (ev?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    // 스크롤이 마지막 아이템에 도달했거나 첫 번째 아이템으로 돌아왔을 때만 부모 뷰페이저에게 터치 이벤트를 전달
//                    if (isAtEndOfList()) {
//                        Log.e("호출","호출")
//                        parent.requestDisallowInterceptTouchEvent(false)
//                    } else {
                        parent.requestDisallowInterceptTouchEvent(true)
//                    }
                }
            }
        return super.dispatchTouchEvent(ev)
    }

    private fun isAtEndOfList(): Boolean {
        val layoutManager = layoutManager as? StaggeredGridLayoutManager ?: return false
        val lastVisibleItemPositions = layoutManager.findLastCompletelyVisibleItemPositions(null)
        val itemCount = adapter?.itemCount ?: 0
        return lastVisibleItemPositions.any { it == itemCount - 1 }
    }

//    private fun isAtFirstItem(): Boolean {
//        val layoutManager = layoutManager as? StaggeredGridLayoutManager ?: return false
//        val firstVisibleItemPositions = layoutManager.findFirstCompletelyVisibleItemPositions(null)
//        return firstVisibleItemPositions.any { it == 0 }
//    }

}
