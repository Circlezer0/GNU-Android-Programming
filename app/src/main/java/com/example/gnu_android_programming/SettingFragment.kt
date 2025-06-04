package com.example.gnu_android_programming

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.gnu_android_programming.database.LedgerDBHelper

class SettingFragment : Fragment() {

    // DB Helper 초기화용
    private lateinit var ledgerDBHelper: LedgerDBHelper

    // 뷰 참조용 변수
    private lateinit var radioGroupNotification: RadioGroup
    private lateinit var radioMuteAll: RadioButton
    private lateinit var radioVibrate: RadioButton
    private lateinit var radioSound: RadioButton

    private lateinit var btnExportJson: Button
    private lateinit var btnImportJson: Button
    private lateinit var btnExportJsonCloud: Button
    private lateinit var btnImportJsonCloud: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_setting.xml 레이아웃만 inflate
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ledgerDBHelper = LedgerDBHelper(requireContext())

        // 1) “알림 관리” 섹션 바인딩
        radioGroupNotification = view.findViewById(R.id.radioGroupNotification)
        radioMuteAll = view.findViewById(R.id.radioMuteAll)
        radioVibrate = view.findViewById(R.id.radioVibrate)
        radioSound = view.findViewById(R.id.radioSound)

        // 기본 선택값(예: “소리”) 미리 체크
        radioSound.isChecked = true

        radioGroupNotification.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioMuteAll -> {
                    // TODO: 모든 알림 음소거 로직
                    Toast.makeText(requireContext(), "모든 알림 음소거 선택", Toast.LENGTH_SHORT).show()
                }
                R.id.radioVibrate -> {
                    // TODO: 진동 알림 로직
                    Toast.makeText(requireContext(), "진동 알림 선택", Toast.LENGTH_SHORT).show()
                }
                R.id.radioSound -> {
                    // TODO: 소리 알림 로직
                    Toast.makeText(requireContext(), "소리 알림 선택", Toast.LENGTH_SHORT).show()
                }
            }
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
