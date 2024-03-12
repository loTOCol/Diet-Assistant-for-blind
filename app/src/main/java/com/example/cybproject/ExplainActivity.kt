package com.example.cybproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.speech.tts.TextToSpeech
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import java.util.*

class ExplainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private val SPEECH_REQUEST_CODE = 123
    private var touchCount = 0
    private lateinit var soundPlayer: SoundPlayer // 음성인식할 때 사용자에게 알려주려고 만든 사운드

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explain)

        val avgkgtextview = findViewById<TextView>(R.id.avgkgtextview)


        // 키 몸무게 가져오기
        val cm = intent.getDoubleExtra("cm", 0.0)
        val kg = intent.getDoubleExtra("kg", 0.0)

        // 표준체중 계산
        val stdWeight = (cm - 100) * 0.9

        // 표준체중 표시
        avgkgtextview.text = stdWeight.toString()

        // 라디오 버튼 클릭 시 선택한 값을 다음화면에 넘겨줌
        fun onRadioButtonClicked(view: View) {
            // 클릭한 라디오 버튼의 ID를 가져옴
            val id = view.id
            val intent = Intent(this, CalorieActivity::class.java)
            when (id) {
                R.id.radio_button25 -> intent.putExtra("value", 25)
                R.id.radio_button30 -> intent.putExtra("value", 30)
                R.id.radio_button35 -> intent.putExtra("value", 35)
                R.id.radio_button40 -> intent.putExtra("value", 40)
                R.id.radio_button45 -> intent.putExtra("value", 45)
            }
            // 표준체중 다음화면에 넘기기
            intent.putExtra("stdWeight", stdWeight)
            startActivity(intent)
        }

        // 각 라디오 버튼에 OnClickListener 등록
        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            onRadioButtonClicked(radioButton)
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
                val explains = resources.getString(R.string.explains)
                tts.speak(explains,
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
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
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
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) // 음성으로 인식된 텍스트 가져오기
            // spokenText 값을 기반으로 라디오 버튼 선택하기

            val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
            when (spokenText) {
                "25" -> radioGroup.check(R.id.radio_button25)
                "30" -> radioGroup.check(R.id.radio_button30)
                "35" -> radioGroup.check(R.id.radio_button35)
                "40" -> radioGroup.check(R.id.radio_button40)
                "45" -> radioGroup.check(R.id.radio_button45)
                else -> tts.speak("올바른 값을 말씀해주세요", TextToSpeech.QUEUE_FLUSH, null, null)
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
