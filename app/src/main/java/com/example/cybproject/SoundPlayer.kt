package com.example.cybproject

import android.content.Context
import android.media.MediaPlayer

class SoundPlayer(context: Context) { //컨텍스트 매게변수를 받아 생성
    private val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.sound123) //사운드 설정

    fun playNotificationSound() { //재생
        mediaPlayer.start()
    }

    fun release() { //해제
        mediaPlayer.release()
    }
}
