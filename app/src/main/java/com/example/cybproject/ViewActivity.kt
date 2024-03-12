package com.example.cybproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import java.util.*

class ViewActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var helpButton: Button
    private lateinit var tts: TextToSpeech
    private var clickCount = 0 //화면 클릭 횟수

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        startButton = findViewById(R.id.start_button)
        helpButton = findViewById(R.id.help_button)

        // TTS 초기화
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                val ptmessage = resources.getString(R.string.ptmessage)
                tts.speak(ptmessage, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        //화면 클릭시
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                clickCount++
                if (clickCount == 2) { // 2번터치
                    tts.stop() // TTS 중지
                    val intent = Intent(this@ViewActivity, MainActivity::class.java)
                    startActivity(intent)
                    clickCount = 0
                }
                return super.onDoubleTap(e)
            }

            override fun onLongPress(e: MotionEvent) { // 롱클릭시
                tts.stop() // TTS 중지
                val intent = Intent(this@ViewActivity, HelpActivity::class.java)
                startActivity(intent)
                super.onLongPress(e)
            }
        })

        //전체화면에 터치 이벤트 리스너 설정
        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        startButton.setOnClickListener {
            tts.stop() // TTS 중지
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        helpButton.setOnClickListener {
            tts.stop() // TTS 중지
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // TTS 초기화 및 메시지 읽어주기, 다시 실행
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                val ptmessage = resources.getString(R.string.ptmessage)
                tts.speak(ptmessage, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    override fun onPause() { //중지
        super.onPause()
        tts.stop() // 화면이 백그라운드로 가면 TTS 중지
    }

    override fun onDestroy() { //종료
        super.onDestroy()
        tts.shutdown()
    }
}
