package com.sports2i.trainer.ui.adapter.group

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sports2i.trainer.ui.fragment.group.GroupTrainingStatusFragment
import com.sports2i.trainer.ui.fragment.groupmember.ExerciseTimeFragment
import okhttp3.internal.notify
import okhttp3.internal.notifyAll


class GroupStatusTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val fragments = ArrayList<Fragment>() // 다양한 프래그먼트를 저장할 리스트
    private val tabTitles = ArrayList<String>() // 탭에 표시될 타이틀들을 저장할 리스트

    // 프래그먼트 추가 메서드
    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        tabTitles.add(title)
    }

    // 프래그먼트 업데이트 메서드 (필요한 경우)
    fun updateFragment(position: Int, fragment: Fragment) {
        fragments[position] = fragment
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    // 탭 타이틀 가져오기
    fun getTabTitle(position: Int): String {
        return tabTitles[position]
    }

    // 해당 위치의 프래그먼트 가져오기
    fun getFragment(position: Int): Fragment? {
        return if (position >= 0 && position < fragments.size) {
            fragments[position]
        } else {
            null
        }
    }

    fun clearFragment() {
        fragments.clear()
        notifyDataSetChanged()
    }

    fun notifyData() {
        notifyDataSetChanged()
    }

}

