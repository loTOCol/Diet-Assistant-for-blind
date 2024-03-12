package com.example.cybproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow
import android.speech.tts.TextToSpeech
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import java.util.*

class CenterActivity : AppCompatActivity() {
    lateinit var bmiresult : TextView
    lateinit var resultimage : ImageView
    lateinit var nextbutton : Button
    lateinit var tts: TextToSpeech
    @SuppressLint("SetTextI18n", "MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center)

        // MainActivity에서 전달된 신장과 체중 정보 가져오기
        val cm = intent.getDoubleExtra("cm", 0.0)
        val kg = intent.getDoubleExtra("kg", 0.0)

        // BMI 계산하기
        val bmi = kg / (cm / 100.0).pow(2.0)

        bmiresult = findViewById<TextView>(R.id.bmiresult)
        resultimage = findViewById(R.id.resultimage)
        nextbutton = findViewById(R.id.nextbutton)

        val bmiString = String.format("%.1f", bmi) // 소수점 한 자리까지만 표시
        val resultText = when {
            bmi >= 35.0 -> {
                resultimage.setImageResource(R.drawable.weight6)
                "초고도 비만"
            }
            bmi >= 30.0 -> {
                resultimage.setImageResource(R.drawable.weight5)
                "고도 비만"
            }
            bmi >= 25.0 -> {
                resultimage.setImageResource(R.drawable.weight4)
                "비만"
            }
            bmi >= 23.0 -> {
                resultimage.setImageResource(R.drawable.weight3)
                "과체중"
            }
            bmi >= 18.5 -> {
                resultimage.setImageResource(R.drawable.weight2)
                "정상체중"
            } else -> {
                resultimage.setImageResource(R.drawable.weight1)
                "저체중"
            }
        }
        bmiresult.text = "BMI: ${bmiString}\n$resultText"

        // TTS 초기화
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                tts.speak("당신의 현재 신장은"+cm+"이며 체중은"+kg+"입니다. 당신의 현재 비엠아이는"+bmiString+"이며"+resultText +"입니다. 화면 가운데를 두번 클릭시 다음화면으로 넘어갑니다.", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val intent = Intent(this@CenterActivity, ExplainActivity::class.java)
                intent.putExtra("cm", cm)
                intent.putExtra("kg", kg)
                startActivity(intent)
                return super.onDoubleTap(e)
            }
        })

        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        nextbutton.setOnClickListener {
            tts.stop() // TTS 중지
            val intent = Intent(this, ExplainActivity::class.java)
            intent.putExtra("kg", kg)
            intent.putExtra("cm", cm)
            startActivity(intent)
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


