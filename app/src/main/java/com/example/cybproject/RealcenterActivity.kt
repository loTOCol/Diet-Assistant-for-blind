package com.example.cybproject

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView


class RealcenterActivity : AppCompatActivity() {


    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realcenter)

        // title 제거
        supportActionBar?.hide()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.b_calendar -> {
                    // '달력' 메뉴 클릭 시 Fragment1로 전환
                    val fragment1 = Fragment1()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment1).commit()
                    menuItem.icon?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                    true
                }
                R.id.b_graph -> {
                    // '식단' 메뉴 클릭 시 Fragment2로 전환
                    val fragment2 = Fragment2()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment2).commit()
                    true
                }
                R.id.b_setting -> {
                    // '설정' 메뉴 클릭 시 Fragment3로 전환
                    val fragment3 = Fragment3()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment3).commit()
                    true
                }
                else -> false
            }
        }
        // 프레그먼트 1 버튼을 선택한 상태로 시작
        bottomNavigationView.selectedItemId = R.id.b_calendar


    }
}