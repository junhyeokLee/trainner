package com.sports2i.trainer.utils

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller

class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
    }

    override fun getHorizontalSnapPreference(): Int {
        return SNAP_TO_START
    }

    // 스크롤 속도를 조절하는 메서드
    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        // 스크롤 속도를 조절할 수 있는 값을 반환
        return 0.3f // 조절이 필요한 경우 원하는 값을 사용
    }

    override fun calculateDtToFit(viewStart: Int, viewEnd: Int, boxStart: Int, boxEnd: Int, snapPreference: Int): Int {
        return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
    }
}