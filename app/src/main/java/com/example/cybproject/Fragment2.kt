package com.example.cybproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cybproject.data.FoodDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import android.view.MotionEvent
import java.util.*

class Fragment2 : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodRecordAdapter: FoodRecordAdapter

    private lateinit var tts: TextToSpeech
    private val SPEECH_REQUEST_CODE = 123
    private var touchCount = 0
    private lateinit var soundPlayer: SoundPlayer // 음성인식할 때 사용자에게 알려주려고 만든 사운드

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_fragment2, container, false)
        soundPlayer = SoundPlayer(requireContext())
        recyclerView = view.findViewById(R.id.foodRecordRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        foodRecordAdapter = FoodRecordAdapter(listOf()) // 초기 데이터는 빈 리스트로 설정합니다.
        recyclerView.adapter = foodRecordAdapter

        //tts 초기화
        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
                tts.speak(
                    "가운데 화면을 두번 클릭 후 기록이라 말씀하시면 식단에 기록된 음식과 기록 날짜를 들으실 수 있습니다.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }
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
            if (spokenText == "기록") {
                // 기록되어있는 음식과 날짜를 가져와서 TTS로 말해줍니다.
                GlobalScope.launch(Dispatchers.IO) {
                    val database = FoodDatabase.getInstance(requireContext())
                    val foodRecords = database.foodRecordDao().getAll()
                    val speechText = StringBuilder()

                    if (foodRecords.isNotEmpty()) {
                        speechText.append("기록된 음식은 다음과 같습니다:")
                        for (record in foodRecords) {
                            val foodName = record.foodName
                            val date = record.date
                            speechText.append("$foodName, $date.")
                        }
                    } else {
                        speechText.append("기록된 음식이 없습니다.")
                    }

                    withContext(Dispatchers.Main) {
                        tts.speak(speechText.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
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




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 데이터베이스에서 음식 기록을 가져와서 어댑터에 설정합니다.
        val database = FoodDatabase.getInstance(requireContext())

        GlobalScope.launch(Dispatchers.IO) {
            val foodRecords = database.foodRecordDao().getAll()
            withContext(Dispatchers.Main) {
                foodRecordAdapter.updateData(foodRecords)
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
