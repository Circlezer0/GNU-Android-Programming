package com.example.gnu_android_programming

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.gnu_android_programming.home.HomeFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        // ActionBarDrawerToggle로 햄버거 아이콘 연결
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 네비게이션 메뉴 클릭 리스너
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val fragment = when (menuItem.itemId) {
                R.id.nav_home -> HomeFragment() // 가계부 페이지
                R.id.nav_reservation -> ReservationFragment() // 예약 페이지
                R.id.nav_setting -> SettingFragment() // 설정 페이지
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }
            drawerLayout.closeDrawers()
            true
        }

        // 앱 처음 실행시 홈 프래그먼트 표시
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }
    }

    // 뒤로가기(Back) 버튼 눌렀을 때 Drawer 열려 있으면 닫기
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
