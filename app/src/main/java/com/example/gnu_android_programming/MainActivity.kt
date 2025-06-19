package com.example.gnu_android_programming

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.gnu_android_programming.home.HomeFragment
import com.example.gnu_android_programming.reservation.ReservationFragment
import com.google.android.material.navigation.NavigationView
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.RequiresApi
import com.example.gnu_android_programming.database.AppDatabaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var dbHelper: AppDatabaseHelper
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // DB 헬퍼 초기화
        dbHelper = AppDatabaseHelper(this)
        // 여기서 onCreate/onUpgrade 가 호출되어 테이블이 모두 준비됨
        db = dbHelper.writableDatabase


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

        createNotificationChannels()
    }

    // 뒤로가기(Back) 버튼 눌렀을 때 Drawer 열려 있으면 닫기
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun createNotificationChannels() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Android O 이전 버전에서는 채널을 만들 필요 없음
            return
        }

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 소리 채널
        val soundChannel = NotificationChannel(
            "channel_sound",
            "소리 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "소리와 진동이 모두 동작"
            enableVibration(true)
        }
        nm.createNotificationChannel(soundChannel)

        // 진동만 채널
        val vibrateChannel = NotificationChannel(
            "channel_vibrate",
            "진동 전용 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "진동만 발생"
            enableVibration(true)
            setSound(null, null)
        }
        nm.createNotificationChannel(vibrateChannel)

        // 무음 채널
        val silentChannel = NotificationChannel(
            "channel_silent",
            "무음 알림",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "소리/진동 모두 없음"
            enableVibration(false)
            setSound(null, null)
        }
        nm.createNotificationChannel(silentChannel)
    }
}
