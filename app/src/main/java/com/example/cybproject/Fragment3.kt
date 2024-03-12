package com.example.cybproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cybproject.data.FoodDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class Fragment3 : Fragment() {

    private lateinit var database: FoodDatabase
    private lateinit var tts: TextToSpeech
    private val SPEECH_REQUEST_CODE = 123
    private var touchCount = 0
    private lateinit var soundPlayer: SoundPlayer // 음성인식할 때 사용자에게 알려주려고 만든 사운드

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_fragment3, container, false)
        soundPlayer = SoundPlayer(requireContext())

        //tts 초기화
        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                tts.speak(
                    "가운데 화면을 2번 클릭 후 전체 삭제라고 말씀하시면 음식 기록이 전부 삭제됩니다.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        }

        database = FoodDatabase.getInstance(requireContext())

        // 전체 삭제 버튼 클릭 이벤트 처리
        val deleteAllButton: Button = view.findViewById(R.id.deleteAllButton)
        deleteAllButton.setOnClickListener {
            deleteAllFoodRecords()
        }

        // 화면 터치 이벤트 리스너 등록
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                touchCount++
                if (touchCount == 2) {
                    startSpeechRecognition() // 음성 인식 시작
                    touchCount = 0 // 터치 카운트 초기화
                }
            }
            true
        }

        return view
    }

    private fun startSpeechRecognition() {
        soundPlayer.playNotificationSound()
        // 음성 인식을 위한 인텐트 생성
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀해주세요") // 사용자에게 보여줄 프롬프트 메시지

        // 음성 인식 액티비티 시작
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0)?.toLowerCase(Locale.getDefault())
            if (spokenText == "전체 삭제") {
                deleteAllFoodRecords() // 전체 삭제 기능 호출
            } else {
                when (spokenText?.toLowerCase()) {
                    "달력" -> {
                        val fragment1 = Fragment1()
                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, fragment1) // fragment_container에 Fragment1을 대체
                        transaction.commit()
                    }
                    "식단" -> {
                        val fragment2 = Fragment2()
                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, fragment2) // fragment_container에 Fragment2를 대체
                        transaction.commit()
                    }
                    "설정" -> {
                        val fragment3 = Fragment3()
                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, fragment3) // fragment_container에 Fragment3을 대체
                        transaction.commit()
                    }
                    else -> {
                        // 숫자로 변환할 수 없는 값일 경우, TTS로 알림 제공
                        tts.speak("잘못된 입력입니다.", TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun deleteAllFoodRecords() {
        GlobalScope.launch(Dispatchers.IO) {
            database.foodRecordDao().deleteAll()
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "모든 음식 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                tts.speak(
                    "모든 음식 기록이 삭제되었습니다.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )

            }
        }
    }

    override fun onPause() {
        super.onPause()
        tts.stop() // 화면이 백그라운드로 가면 TTS 중지
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
