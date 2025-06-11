package com.example.gnu_android_programming

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gnu_android_programming.database.ReservationDBHelper
import com.example.gnu_android_programming.reservation.PushSettingData

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "reservation_channel"
        private const val CHANNEL_NAME = "예약 알람"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val resId = intent.getLongExtra("reservationId", -1L)
        if (resId == -1L) return

        val db = ReservationDBHelper(context)
        val reservation = db.getReservationById(resId) ?: return

        // 1) 채널 생성 (앱 최초 실행 시 한 번만 해도 좋습니다)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "예약 하루 전, 지정된 분 전 알림을 제공합니다."
            }
            nm.createNotificationChannel(channel)
        }

        // 2) 알림에 들어갈 상세 텍스트 구성
        val relMin = (reservation.pushSetting as? PushSettingData)?.relativeMin ?: 0
        val details = """
            거래 ${relMin}분 전이에요!
            예약자: ${reservation.customerName}
            예약날짜: ${reservation.reservationDateTime}
            거래날짜: ${reservation.transactionDateTime}
            거래장소: ${reservation.transactionLocation}
        """.trimIndent()

        // 3) BigTextStyle 을 사용해 여러 줄 표시
        val style = NotificationCompat.BigTextStyle()
            .bigText(details)

        // 4) 알림 빌드
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("예약 알림")
            .setStyle(style)
            .setAutoCancel(true)
            .build()

        // 5) 알림 표시
        NotificationManagerCompat.from(context).notify(resId.toInt(), notif)
    }
}
