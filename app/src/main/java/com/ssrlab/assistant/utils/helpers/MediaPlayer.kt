package com.ssrlab.assistant.utils.helpers

import android.media.MediaPlayer
import android.net.Uri
import android.widget.TextView
import com.ssrlab.assistant.ui.chat.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

object MediaPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var playerStatus = ""

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    fun initializeMediaPlayer(mainActivity: MainActivity, uri: Uri, view: TextView? = null) {
        val chatHelper = ChatHelper()

        playerStatus = "play"
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer!!.setDataSource(mainActivity, uri)
            mediaPlayer!!.prepare()

            view?.text = chatHelper.convertToTimerMode(mediaPlayer!!.duration)
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