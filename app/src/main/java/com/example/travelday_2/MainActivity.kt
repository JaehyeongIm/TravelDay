package com.example.travelday_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.travelday_2.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTabLayout()

    }

    private fun initTabLayout() {
        val tabCount=3
        val viewPagerAdapter=ViewPagerAdapter(supportFragmentManager, lifecycle, tabCount)
        binding.viewPager2.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout,binding.viewPager2){tab, position->
            when(position){
                0->tab.text="준비물"
                1->tab.text="일정"
                2->tab.text="커뮤니티"
            }
        }.attach()
        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.checklist)
        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.calendar)
        binding.tabLayout.getTabAt(2)?.setIcon(R.drawable.community)
        binding.viewPager2.setCurrentItem(1, true)
    }}