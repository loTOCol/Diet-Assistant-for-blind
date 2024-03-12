package com.example.cybproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import android.speech.tts.TextToSpeech
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.util.*

class CalorieActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    lateinit var nextButton : Button
    private lateinit var tts: TextToSpeech
    private val SPEECH_REQUEST_CODE = 123
    private var touchCount = 0
    private lateinit var soundPlayer: SoundPlayer // 음성인식할 때 사용자에게 알려주려고 만든 사운드

    lateinit var tanCalTextView: TextView
    lateinit var danCalTextView: TextView
    lateinit var jiCalTextView: TextView
    lateinit var tanCalTextView1: TextView
    lateinit var danCalTextView1: TextView
    lateinit var jiCalTextView1: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calorie)

        // Intent에서 value와 표준 체중 값을 가져옴
        val value = intent.getIntExtra("value", 0)
        val stdWeight = intent.getDoubleExtra("stdWeight", 0.0)

        // 일일활동량 계산
        val calorie = value * stdWeight

        // 1일 요구량 표시
        val calorieTextView2 = findViewById<TextView>(R.id.totalCalorie2)
        calorieTextView2.text = calorie.toString()

        // 탄수화물 섭취 열량 계산
        val tanCalorie = calorie * 0.55 // 55% 비율 적용
        val tanCalorieMax = calorie * 0.65 // 65% 비율 적용
        tanCalTextView = findViewById<TextView>(R.id.tancal)
        tanCalTextView.text = "${tanCalorie.toInt()}kcal ~ ${tanCalorieMax.toInt()}kcal"

        // 단백질 섭취 열량 계산
        val danCalorie = calorie * 0.07 // 7% 비율 적용
        val danCalorieMax = calorie * 0.20 // 20% 비율 적용
        danCalTextView = findViewById<TextView>(R.id.dancal)
        danCalTextView.text = "${danCalorie.toInt()}kcal ~ ${danCalorieMax.toInt()}kcal"

        // 지방 섭취 열량 계산
        val jiCalorie = calorie * 0.15 // 15% 비율 적용
        val jiCalorieMax = calorie * 0.30 // 30% 비율 적용
        jiCalTextView = findViewById<TextView>(R.id.jical)
        jiCalTextView.text = "${jiCalorie.toInt()}kcal ~ ${jiCalorieMax.toInt()}kcal"


        // 탄수화물 섭취량 계산
        val tanCalorie1 = tanCalorie/4 // 4kcal 비율 적용
        val tanCalorieMax1 = tanCalorieMax/4 // 4kcal 비율 적용
        tanCalTextView1 = findViewById<TextView>(R.id.tancal1)
        tanCalTextView1.text = "${tanCalorie1.toInt()}g ~ ${tanCalorieMax1.toInt()}g"

        // 단백질 섭취량 계산
        val danCalorie1 = danCalorie/4 // 4kcal 비율 적용
        val danCalorieMax1 = danCalorieMax/4 // 4kcal 비율 적용
        danCalTextView1 = findViewById<TextView>(R.id.dancal1)
        danCalTextView1.text = "${danCalorie1.toInt()}g ~ ${danCalorieMax1.toInt()}g"

        // 지방 섭취량 계산
        val jiCalorie1 = jiCalorie/9 // 9kcal 비율 적용
        val jiCalorieMax1 = jiCalorieMax/9 // 9kcal 비율 적용
        jiCalTextView1 = findViewById<TextView>(R.id.jical1)
        jiCalTextView1.text = "${jiCalorie1.toInt()}g ~ ${jiCalorieMax1.toInt()}g"

        nextButton = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this, RealcenterActivity::class.java)
            startActivity(intent)
        }

        soundPlayer = SoundPlayer(this)
        val gestureDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
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
                tts.speak("당신의 1일 에너지 요구량은"+calorie+"입니다. 탄수화물, 단백질, 지방별로 섭취 열량과 섭취량을 알고 싶으면 화면 위쪽을 두번 클릭 후 탄수화물, 단백질, 지방을 각각 말씀해주세요. 다음 화면으로 넘어가고 싶다면 다음이라고 말씀해주세요. ",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        }
    }
    private fun startSpeechToText() {
        soundPlayer.playNotificationSound()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "음성을 입력하세요.")
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "음성 인식을 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            // 음성 인식 결과를 처리합니다.
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val spokenText = results[0].toLowerCase(Locale.getDefault())
                if (spokenText.contains("탄수화물")) {
                    // 탄수화물 정보를 처리
                    val combinedText1 = "${tanCalTextView.text} 사이의 칼로리를 섭취하고 ${tanCalTextView1.text} 사이의 탄수화물을 섭취합니다."
                    tts.speak(combinedText1, TextToSpeech.QUEUE_FLUSH, null, null)
                } else if (spokenText.contains("단백질")) {
                    // 단백질 정보를 처리
                    val combinedText2 = "${danCalTextView.text} 사이의 칼로리를 섭취하고${danCalTextView1.text} 사이의 단백질을 섭취합니다."
                    tts.speak(combinedText2, TextToSpeech.QUEUE_FLUSH, null, null)
                } else if (spokenText.contains("지방")) {
                    // 지방 정보를 처리
                    val combinedText3 = "${jiCalTextView.text} 사이의 칼로리를 섭취하고${jiCalTextView1.text} 사이의 지방을 섭취합니다."
                    tts.speak(combinedText3, TextToSpeech.QUEUE_FLUSH, null, null)
                } else if (spokenText.contains("다음")) {
                    val intent = Intent(this, RealcenterActivity::class.java)
                    startActivity(intent)
                } else {
                    // 인식된 명령이 없는 경우
                    tts.speak("죄송합니다. 다시 말씀해주세요.", TextToSpeech.QUEUE_FLUSH, null, null)
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