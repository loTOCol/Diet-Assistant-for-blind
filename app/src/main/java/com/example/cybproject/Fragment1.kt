package com.example.cybproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import java.util.*
class Fragment1 : Fragment() {

    private lateinit var tts: TextToSpeech
    private val SPEECH_REQUEST_CODE = 123
    private var touchCount = 0
    private lateinit var soundPlayer: SoundPlayer // 음성인식할 때 사용자에게 알려주려고 만든 사운드

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_fragment1, container, false)
        soundPlayer = SoundPlayer(requireContext())
        val calendarView: CalendarView = view.findViewById(R.id.calendarView)
        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                tts.speak("달력을 사용해 이번 달에 먹은 음식을 기록할 수 있습니다. 이번달에 일수를 말씀하시면 음식입력 화면으로 넘어갑니다. 화면 가운데보다 살짝 아래를 두번 클릭해주세요",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null)
            }
        }

        // 날짜 선택 이벤트 리스너 등록
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth" // 선택된 날짜
            openFoodRecordActivity(selectedDate) // 음식 기록 액티비티 열기
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
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) // 음성으로 인식된 텍스트

            if (!spokenText.isNullOrEmpty() && spokenText.isDigitsOnly()) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, spokenText.toInt()) // 일자를 설정

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1 // 월은 1부터 시작하므로 1을 더해줌
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

                val selectedDate = "$year-$month-$dayOfMonth"

                openFoodRecordActivity(selectedDate) // 음식 기록 액티비티 열기
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
    }




    private fun openFoodRecordActivity(date: String) {
        val intent = Intent(requireContext(), FoodRecordActivity::class.java)
        intent.putExtra("selectedDate", date)
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        tts.speak("현재 화면에서 날짜를 다시 선택할 수 있으며 자신이 기록한 음식 목록을 알고 싶으면 화면을 두번 클릭 후 식단이라고 말씀하시면 됩니다. 또한 설정에 들어가서 자신이 기록한 음식을 한번에 삭제가능합니다.", TextToSpeech.QUEUE_FLUSH, null, null)
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

