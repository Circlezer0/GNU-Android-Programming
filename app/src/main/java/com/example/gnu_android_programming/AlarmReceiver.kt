package com.example.gnu_android_programming

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gnu_android_programming.database.ReservationDao
import com.example.gnu_android_programming.reservation.PushSettingData

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "reservation_channel"
        private const val CHANNEL_NAME = "예약 알람"
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val resId = intent.getLongExtra("reservationId", -1L)
        if (resId == -1L) return

        // 1) SharedPreferences 에서 유저 설정 읽기
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val alarmType = prefs.getString("notification_type", "sound")!!

        // 2) 채널 ID 매핑
        val channelId = when (alarmType) {
            "sound"   -> "channel_sound"
            "vibrate" -> "channel_vibrate"
            "silent"  -> "channel_silent"
            else      -> "channel_sound"
        }

        val db = ReservationDao(context)
        val reservation = db.getById(resId) ?: return

        // 3) 알림 내용 구성
        val relMin = (reservation.pushSetting as? PushSettingData)?.relativeMin ?: 0
        val details = """
            거래 ${relMin}분 전이에요!
            예약자: ${reservation.customerName}
            예약날짜: ${reservation.reservationDateTime}
            거래날짜: ${reservation.transactionDateTime}
            거래장소: ${reservation.transactionLocation}
        """.trimIndent()
        val style = NotificationCompat.BigTextStyle().bigText(details)

        // 4) Builder 생성
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("예약 알림")
            .setStyle(style)
            .setAutoCancel(true)

        // 5) Android O 미만 버전은 Builder 에서 직접 사운드/진동 제어
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            when (alarmType) {
                "sound" -> builder.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
                "vibrate"-> builder.setDefaults(Notification.DEFAULT_VIBRATE)
                "silent" -> {
                    builder.setSound(null)
                    builder.setVibrate(longArrayOf(0L))
                }
            }
        }

        // 6) 알림 발행
        NotificationManagerCompat.from(context).notify(resId.toInt(), builder.build())
    }
}
