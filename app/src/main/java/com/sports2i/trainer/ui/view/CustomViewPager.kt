package com.sports2i.trainer.ui.view
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager2.widget.ViewPager2
import com.sports2i.trainer.ui.adapter.myexercise.ExerciseTimeTabMyAdapter
import com.sports2i.trainer.ui.fragment.myexercise.ExerciseTimeMyFragment

class CustomViewPager(context: Context, attrs: AttributeSet?) {

    private val viewPager: ViewPager2 = ViewPager2(context, attrs)
    private var isSwipeEnabled = true
    private var currentItemFragment: ExerciseTimeMyFragment? = null

    init {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 페이지가 변경되었을 때 현재 페이지의 프래그먼트를 업데이트
                currentItemFragment = (viewPager.adapter as? ExerciseTimeTabMyAdapter)?.getFragment(position)
            }
        })
    }

    fun setAdapter(adapter: ExerciseTimeTabMyAdapter) {
        viewPager.adapter = adapter
    }

    fun getCurrentItem(): Int {
        return viewPager.currentItem
    }

    fun setCurrentItem(item: Int) {
        viewPager.currentItem = item
    }

    fun setSwipeEnabled(enabled: Boolean) {
        isSwipeEnabled = enabled
    }

    fun onTouchEvent(ev: MotionEvent): Boolean {
        return isSwipeEnabled && viewPager.onTouchEvent(ev)
    }

    fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return isSwipeEnabled && viewPager.onInterceptTouchEvent(ev)
    }

    fun getCurrentFragment(): ExerciseTimeMyFragment? {
        return currentItemFragment
    }


}

