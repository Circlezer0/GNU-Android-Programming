package com.example.gnu_android_programming

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.gnu_android_programming.database.AppBackup
import com.example.gnu_android_programming.database.LedgerDao
import com.example.gnu_android_programming.database.ReservationDao
import com.google.gson.Gson

class SettingFragment : Fragment() {
    companion object {
        private const val REQ_EXPORT = 2001
        private const val REQ_IMPORT = 2002
    }

    // 뷰 참조용 변수
    private lateinit var radioGroupNotification: RadioGroup
    private lateinit var radioMuteAll: RadioButton
    private lateinit var radioVibrate: RadioButton
    private lateinit var radioSound: RadioButton

    private lateinit var btnExportJson: Button
    private lateinit var btnImportJson: Button

    // prefs
    private lateinit var prefs: SharedPreferences

    // db
    private lateinit var ledgerDao: LedgerDao
    private lateinit var reservationDao: ReservationDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_setting.xml 레이아웃만 inflate
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext()
            .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        ledgerDao      = LedgerDao(requireContext())
        reservationDao = ReservationDao(requireContext())


        // 1) “알림 관리” 섹션 바인딩
        radioGroupNotification = view.findViewById(R.id.radioGroupNotification)
        radioMuteAll = view.findViewById(R.id.radioMuteAll)
        radioVibrate = view.findViewById(R.id.radioVibrate)
        radioSound = view.findViewById(R.id.radioSound)

        // 저장된 값으로 초깃값 설정
        when (prefs.getString("notification_type", "sound")) {
            "sound"   -> radioSound.isChecked = true
            "vibrate" -> radioVibrate.isChecked = true
            "silent"  -> radioMuteAll.isChecked = true
        }

        radioGroupNotification.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.radioSound   -> "sound"
                R.id.radioVibrate -> "vibrate"
                R.id.radioMuteAll -> "silent"
                else              -> "sound"
            }
            prefs.edit()
                .putString("notification_type", type)
                .apply()
            Toast.makeText(requireContext(),
                when(type) {
                    "sound"   -> "소리 알림 선택"
                    "vibrate" -> "진동 알림 선택"
                    "silent"  -> "무음 알림 선택"
                    else      -> ""
                },
                Toast.LENGTH_SHORT
            ).show()
        }

        // 2) “데이터 관리” 버튼 바인딩
        btnExportJson = view.findViewById(R.id.btnExportJson)
        btnImportJson = view.findViewById(R.id.btnImportJson)

        btnExportJson.setOnClickListener {
            // TODO: JSON으로 내보내기 기능
            exportBackup()
            Toast.makeText(requireContext(), "Json으로 내보내기 클릭", Toast.LENGTH_SHORT).show()
        }

        btnImportJson.setOnClickListener {
            // TODO: JSON에서 데이터 불러오기 기능
            importBackup()
            Toast.makeText(requireContext(), "Json에서 데이터 불러오기 클릭", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportBackup() {
        // 1) JSON 생성
        val backup = AppBackup(
            ledgers      = ledgerDao.getAll(),
            reservations = reservationDao.getAll()
        )
        val json = Gson().toJson(backup)

        // 2) SAF로 “저장 위치 선택” 다이얼로그 띄우기
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "app_backup.json")
        }
        startActivityForResult(intent, REQ_EXPORT)

        // JSON 내용은 onActivityResult 에서 실제 쓰기
        // 임시로 저장해 두자
        tempJson = json
    }

    private fun importBackup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, REQ_IMPORT)
    }

    // 임시 변수
    private var tempJson: String = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data?.data == null) return
        val uri = data.data!!

        when (requestCode) {
            REQ_EXPORT -> {
                // 내보내기: tempJson 을 파일에 쓰기
                requireContext().contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(tempJson.toByteArray())
                    Toast.makeText(requireContext(), "백업 완료!", Toast.LENGTH_SHORT).show()
                }
            }
            REQ_IMPORT -> {
                // 불러오기: 파일에서 JSON 읽고 복원
                requireContext().contentResolver.openInputStream(uri)?.use { `is` ->
                    val json = `is`.bufferedReader().readText()
                    val backup = Gson().fromJson(json, AppBackup::class.java)

                    // 1) 기존 데이터 삭제
                    ledgerDao.deleteAll()
                    reservationDao.deleteAll()

                    // 2) 리턴된 데이터를 다시 삽입
                    backup.ledgers.forEach { ledgerDao.insert(it) }
                    backup.reservations.forEach { reservationDao.insert(it) }

                    Toast.makeText(requireContext(), "복원 완료!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
