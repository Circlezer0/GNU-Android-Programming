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

/**
 * 예약 알림을 지정한 시각에 맞춰 정확하게 전송되도록 예약하는 함수
 * @param context 컨텍스트 객체 (보통 Activity나 Application)
 * @param reservation 예약 정보 객체
 */
@SuppressLint("ScheduleExactAlarm")
fun scheduleReservationAlarm(context: Context, reservation: ReservationData) {
    // 시스템에서 AlarmManager 서비스를 가져옴
    val am = context.getSystemService(AlarmManager::class.java)

    // Android 12(S) 이상에서는 정확한 알람(exact alarm) 사용 시 권한이 필요함
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
        // 사용자가 시스템 설정에서 "정밀 알람 허용"을 수동으로 설정해야 하므로 해당 화면으로 이동
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}") // 현재 앱의 패키지 정보 포함
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return
    }

    // 거래 날짜/시간을 파싱하기 위한 포맷터 설정
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    // 문자열 형태의 거래 시간 정보를 Date 객체로 변환
    val date = fmt.parse(reservation.transactionDateTime) ?: return

    // 사용자 설정에 따라 알림을 몇 분 전에 보낼지 계산 (밀리초 단위로 변환)
    val relMs = (reservation.pushSetting?.relativeMin ?: 0) * 60_000L

    // 최종적으로 알람이 울릴 시간 계산 (거래시간 - 설정된 분 수)
    val triggerTime = date.time - relMs

    // 현재 시간보다 과거라면 알림을 설정하지 않음
    if (triggerTime < System.currentTimeMillis()) return

    // 알람이 울릴 때 호출될 리시버와 예약 ID를 담은 인텐트 생성
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("reservationId", reservation.id) // 알림 수신 시 어떤 예약인지 식별
    }

    // PendingIntent 생성 (예약 ID를 requestCode로 사용하여 고유하게 설정)
    val pi = PendingIntent.getBroadcast(
        context,
        reservation.id!!.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 지정된 시간에 정확하게 알람을 울리도록 설정 (Doze 모드에서도 가능하게 AllowWhileIdle 사용)
    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
}

/**
 * 등록된 예약 알림을 취소하는 함수
 * @param context 컨텍스트 객체
 * @param reservationId 취소할 예약의 ID
 */
fun cancelReservationAlarm(context: Context, reservationId: Long) {
    // 알람을 등록할 때 사용했던 인텐트와 동일하게 구성
    val intent = Intent(context, AlarmReceiver::class.java)

    // 동일한 requestCode와 인텐트로 PendingIntent 생성
    val pi = PendingIntent.getBroadcast(
        context,
        reservationId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // AlarmManager 인스턴스를 가져와서 해당 알람을 취소
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.cancel(pi) // 해당 PendingIntent에 해당하는 알람 제거
}
