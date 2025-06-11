package com.example.gnu_android_programming

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.gnu_android_programming.reservation.ReservationData
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("ScheduleExactAlarm")
fun scheduleReservationAlarm(context: Context, reservation: ReservationData) {
    val am = context.getSystemService(AlarmManager::class.java)
    // Android 12 이상에서 마땅히 권한이 있어야만 exact alarm 가능
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
        // 시스템 설정의 "정밀 알람 허용" 화면으로 이동
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return
    }

    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val date = fmt.parse(reservation.transactionDateTime) ?: return
    val relMs = (reservation.pushSetting?.relativeMin ?: 0) * 60_000L
    val triggerTime = date.time - relMs
    if (triggerTime < System.currentTimeMillis()) return

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("reservationId", reservation.id)
    }
    val pi = PendingIntent.getBroadcast(
        context,
        reservation.id!!.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
}

fun cancelReservationAlarm(context: Context, reservationId: Long) {
    val intent = Intent(context, AlarmReceiver::class.java)
    val pi = PendingIntent.getBroadcast(
        context,
        reservationId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.cancel(pi)
}