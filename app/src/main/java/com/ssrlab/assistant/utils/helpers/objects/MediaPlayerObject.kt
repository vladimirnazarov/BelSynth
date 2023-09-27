package com.ssrlab.assistant.utils.helpers.objects

import android.media.MediaPlayer
import android.net.Uri
import com.ssrlab.assistant.ui.chat.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

object MediaPlayerObject {

    private var mediaPlayer: MediaPlayer? = null
    private var playerStatus = ""

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    fun initializeMediaPlayer(mainActivity: MainActivity, uri: Uri) {
        playerStatus = "play"
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer!!.setDataSource(mainActivity, uri)
            mediaPlayer!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun playAudio() {
        scope.launch {
            when (playerStatus) {

                "pause" -> {
                    mediaPlayer!!.pause()
                    playerStatus = "play"
                } "play" -> {
                try {
                    mediaPlayer!!.start()
                    playerStatus = "pause"
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
            }
        }
    }

    fun pauseAudio() {
        if (playerStatus == "pause") {
            playerStatus = "play"

            mediaPlayer!!.pause()
        }
    }
}