package com.hermesdiary.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.hermesdiary.app.databinding.ActivityMainBinding
import com.hermesdiary.app.ui.HomePagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: HomePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HomePagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false  // 禁用滑动，用底部导航切换
        binding.viewPager.offscreenPageLimit = 5  // 保持所有 Fragment 存活

        // 底部导航切换
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_diary -> binding.viewPager.currentItem = 0
                R.id.nav_albums -> binding.viewPager.currentItem = 1
                R.id.nav_videos -> binding.viewPager.currentItem = 2
                R.id.nav_podcast -> binding.viewPager.currentItem = 3
                R.id.nav_settings -> binding.viewPager.currentItem = 4
            }
            true
        }

        // 默认选中日记
        binding.bottomNav.selectedItemId = R.id.nav_diary
    }
}
