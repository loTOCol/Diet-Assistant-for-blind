package com.example.cybproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cybproject.data.FoodDatabase
import com.example.cybproject.data.FoodRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class FoodRecordActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private val SPEECH_REQUEST_CODE = 123
    private var touchCount = 0
    private lateinit var soundPlayer: SoundPlayer // 음성인식할 때 사용자에게 알려주려고 만든 사운드
    private lateinit var database: FoodDatabase //db 변수
    lateinit var editfo : EditText

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_record)

        val selectedDate = intent.getStringExtra("selectedDate")
        editfo = findViewById(R.id.editfo)

        // TTS 초기화
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                tts.speak("화면 가운데를 두번 클릭해 음식을 기록해주세요.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
        }

        val saveButton: Button = findViewById(R.id.button)
        saveButton.setOnClickListener {
            val foodName = editfo.text.toString().trim()
            if (foodName.isNotEmpty()) {
                saveFoodRecord(selectedDate, foodName)
                Toast.makeText(this, "음식이 기록되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "음식 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
        soundPlayer = SoundPlayer(this)
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
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

        database = FoodDatabase.getInstance(this) // 클래스의 인스턴수 가져와 database에 할당
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun saveFoodRecord(date: String?, foodName: String) {
        val foodRecord = FoodRecord(date = date!!, foodName = foodName) // 날짜와 음식이름 기록

        GlobalScope.launch(Dispatchers.IO) { // 백그라운드 스레드에서 작업 수행
            database.foodRecordDao().insert(foodRecord) //INSERT 실행
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

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0)
            spokenText?.let {
                editfo.setText(it)

                val saveButton: Button = findViewById(R.id.button)
                saveButton.performClick()
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



