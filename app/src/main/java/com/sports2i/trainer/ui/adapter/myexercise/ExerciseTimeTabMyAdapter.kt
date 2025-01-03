package com.sports2i.trainer.ui.adapter.myexercise

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sports2i.trainer.ui.fragment.myexercise.ExerciseTimeMyFragment

class ExerciseTimeTabMyAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val fragments = ArrayList<ExerciseTimeMyFragment>()
    private val tabTitles = ArrayList<String>() // 탭에 표시될 타이틀들을 저장할 리스트

    private var userId: String = ""
    private var dateTime: String = ""

    fun addFragment(fragment: ExerciseTimeMyFragment, title: String) {
        fragments.add(fragment)
        tabTitles.add(title) // 탭 타이틀을 추가
    }

    fun updateFragment(position: Int, fragment: ExerciseTimeMyFragment) {
        fragments[position] = fragment
        notifyDataSetChanged() // 탭 아이템 업데이트 후 데이터셋 변경 알림
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    // 탭 타이틀을 가져오는 함수
    fun getTabTitle(position: Int): String {
        return tabTitles[position]
    }

    // 해당 위치의 프래그먼트를 가져오는 함수
    fun getFragment(position: Int): ExerciseTimeMyFragment? {
        return if (position >= 0 && position < fragments.size) {
            fragments[position]
        } else {
            null
        }
    }


}
