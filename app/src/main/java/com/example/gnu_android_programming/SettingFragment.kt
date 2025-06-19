package com.example.gnu_android_programming

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.gnu_android_programming.database.LedgerDao

class SettingFragment : Fragment() {

    // DB Helper 초기화용
    private lateinit var ledgerDao: LedgerDao

    // 뷰 참조용 변수
    private lateinit var radioGroupNotification: RadioGroup
    private lateinit var radioMuteAll: RadioButton
    private lateinit var radioVibrate: RadioButton
    private lateinit var radioSound: RadioButton

    private lateinit var btnExportJson: Button
    private lateinit var btnImportJson: Button
    private lateinit var btnExportJsonCloud: Button
    private lateinit var btnImportJsonCloud: Button

    // prefs
    private lateinit var prefs: SharedPreferences

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

        ledgerDao = LedgerDao(requireContext())

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
        btnExportJsonCloud = view.findViewById(R.id.btnExportJsonCloud)
        btnImportJsonCloud = view.findViewById(R.id.btnImportJsonCloud)

        btnExportJson.setOnClickListener {
            // TODO: JSON으로 내보내기 기능
            Toast.makeText(requireContext(), "Json으로 내보내기 클릭", Toast.LENGTH_SHORT).show()
        }

        btnImportJson.setOnClickListener {
            // TODO: JSON에서 데이터 불러오기 기능
            Toast.makeText(requireContext(), "Json에서 데이터 불러오기 클릭", Toast.LENGTH_SHORT).show()
        }

        btnExportJsonCloud.setOnClickListener {
            // TODO: 클라우드로 JSON 내보내기 기능
            Toast.makeText(requireContext(), "클라우드로 Json 내보내기 클릭", Toast.LENGTH_SHORT).show()
        }

        btnImportJsonCloud.setOnClickListener {
            // TODO: 클라우드 JSON에서 데이터 불러오기 기능
            Toast.makeText(requireContext(), "클라우드 JSON에서 데이터 불러오기 클릭", Toast.LENGTH_SHORT).show()
        }
    }
}
