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

class HelpActivity : AppCompatActivity() {
    private lateinit var beforeButton: Button
    private lateinit var tts: TextToSpeech

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        // TTS 초기화
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                val helpMessage = resources.getString(R.string.helpmessage)
                tts.speak(helpMessage, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val intent = Intent(this@HelpActivity, MainActivity::class.java)
                startActivity(intent)
                return super.onDoubleTap(e)
            }
        })

        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        beforeButton = findViewById(R.id.beforebutton)
        beforeButton.setOnClickListener {
            tts.stop() // TTS 중지
            val intent = Intent(this, ViewActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // TTS 초기화 및 메시지 읽어주기
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                val helpMessage = resources.getString(R.string.helpmessage)
                tts.speak(helpMessage, TextToSpeech.QUEUE_FLUSH, null, null)
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
