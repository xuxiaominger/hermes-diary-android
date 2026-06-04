package com.hermesdiary.app.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DiaryFragment()
            1 -> AlbumsFragment()
            2 -> VideosFragment()
            3 -> PodcastFragment()
            4 -> SettingsFragment()
            else -> DiaryFragment()
        }
    }
}
