package com.example.gnu_android_programming.reservation

import java.io.Serializable

/**
 * 예약 정보를 담는 데이터 클래스
 * @property id 예약 고유 ID (DB에서 자동 생성되며, 새 예약 생성 시에는 null일 수 있음)
 * @property customerName 고객 이름
 * @property customerContact 고객 연락처 (전화번호 등)
 * @property reservationDateTime 예약 등록 시간 (yyyy-MM-dd HH:mm 형식의 문자열)
 * @property transactionDateTime 실제 거래 예정 시간 (yyyy-MM-dd HH:mm 형식의 문자열)
 * @property reservationType 예약 유형 ("픽업" 또는 "배달" 중 하나)
 * @property transactionLocation 거래 장소 (예: 상점 주소, 배달 주소 등)
 * @property items 예약된 품목 리스트 (각 품목은 ReservationItemData 객체로 구성됨)
 * @property totalAmount 총 금액 (모든 품목의 가격 합계)
 * @property pushSetting 푸시 알림 설정 (null일 경우 알림 없음)
 */
data class ReservationData(
    var id: Long? = null, // 예약 고유 식별자 (SQLite auto-increment ID 등)
    var customerName: String, // 고객 이름
    var customerContact: String, // 고객 연락처
    var reservationDateTime: String, // 예약 생성 일시
    var transactionDateTime: String, // 실제 거래 예정 일시
    var reservationType: String, // 예약 유형: "픽업" 또는 "배달"
    var transactionLocation: String, // 거래 장소 (오프라인 또는 배달지)
    var items: List<ReservationItemData>, // 예약 품목 목록
    var totalAmount: Int, // 총 거래 금액
    var pushSetting: PushSettingData? // 알림 설정 정보 (null이면 알림 없음)
) : Serializable // 인텐트 등에서 객체 전달 가능하도록 직렬화
