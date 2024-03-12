package com.example.cybproject

import android.annotation.SuppressLint
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var heighttv: TextView
    lateinit var weighttv: TextView
    lateinit var cmet: EditText
    lateinit var kget: EditText
    lateinit var calculateButton: Button
    private val SPEECH_REQUEST_CODE = 123 // 음성인식 요청 코드
    private var touchCount = 0
    private lateinit var tts: TextToSpeech
    private lateinit var soundPlayer: SoundPlayer // 음성인식할 때 사용자에게 알려주려고 만든 사운드

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "식생활 도우미"
        heighttv = findViewById(R.id.heighttv)
        weighttv = findViewById(R.id.weighttv)
        cmet = findViewById(R.id.cmet)
        kget = findViewById(R.id.kget)
        calculateButton = findViewById(R.id.calculateButton)
        soundPlayer = SoundPlayer(this) // 사운드 플레이어 클래스이 인스턴스 생성
        val gestureDetector = GestureDetector(this, object : SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                touchCount++
                if (touchCount == 2) {
                    startSpeechToText()
                    touchCount = 0
                }
                return super.onDoubleTap(e)
            }
        })

        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        // TTS 초기화
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                tts.speak("화면 아래를 두번 연속 클릭 하신 후 신장과 체중을 순서대로 말씀해주세요", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        calculateButton.setOnClickListener {
            if (cmet.text.toString().isEmpty() || kget.text.toString().isEmpty()) {
                Toast.makeText(this, "신장과 체중을 모두 적어주세요.", Toast.LENGTH_SHORT).show()
                tts.speak("신장과 체중을 모두 적어주세요.", TextToSpeech.QUEUE_FLUSH, null, null)

            } else {
                try {
                    val cm = cmet.text.toString().toDouble()
                    val kg = kget.text.toString().toDouble()

                    val intent = Intent(this, CenterActivity::class.java)
                    intent.putExtra("cm", cm)
                    intent.putExtra("kg", kg)
                    startActivity(intent)

                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "숫자만 입력해주세요.", Toast.LENGTH_SHORT).show()
                    tts.speak("숫자만 입력해주세요. 다시 한번 화면을 두번 클릭해주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }

        }
    }

    private fun startSpeechToText() {
        soundPlayer.playNotificationSound() // 알림소리 재생
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH) // 음성인식 수행 요청
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) // 음성인식 언어모델 설정
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault()) // 기기의 현재 언어를 언어로 설정
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "음성을 입력하세요.") // 음성인식시 표시될 메세지
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE) // 음성인식 실행
        } catch (e: Exception) { // 음성인식 오류
            Toast.makeText(this, "음성 인식을 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) { // 음성인식 성공적으로 받아왔는지 확인
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) // 음성인식 결과 가져오기
            if (!results.isNullOrEmpty()) {
                val spokenText = results.joinToString(separator = " ") // 문자열 결과들을 공백을 기준으로 하나의 문자열로 합침
                val values = spokenText.split(" ") //공백 기준으로 분할 후 문자열 배열로 만듬
                if (values.size >= 2) { // 분할 된 배열의 크기가 2이이상인지 확인
                    val height = values[0] // 키
                    val weight = values[1] // 몸무게
                    cmet.setText(height) // 에딧텍스트에 값 넣기
                    kget.setText(weight) // 에딧텍스트에 값 넣기
                    tts.stop() // TTS 중지
                    calculateButton.performClick() // 버튼 자동 클릭
                }
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

