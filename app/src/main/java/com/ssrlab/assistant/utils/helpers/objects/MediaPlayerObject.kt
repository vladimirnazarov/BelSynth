package com.ssrlab.assistant.utils.helpers.objects

import android.media.MediaPlayer
import android.net.Uri
import android.widget.ImageButton
import com.ssrlab.assistant.R
import com.ssrlab.assistant.rv.ChatAdapter
import com.ssrlab.assistant.ui.chat.ChatActivity
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

    fun initializeMediaPlayer(chatActivity: ChatActivity, uri: Uri) {
        playerStatus = "play"
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer!!.setDataSource(chatActivity, uri)
            mediaPlayer!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun playAudio(playButton: ImageButton? = null, adapter: ChatAdapter? = null) {
        scope.launch {
            when (playerStatus) {

                "pause" -> {
                    mediaPlayer!!.pause()
                    playerStatus = "play"

                    playButton?.setImageResource(R.drawable.ic_msg_voice_play)

                } "play" -> {
                try {
                    mediaPlayer!!.start()
                    playerStatus = "pause"

                    playButton?.setImageResource(R.drawable.ic_msg_voice_stop)

                    if (playButton != null) mediaPlayer!!.setOnCompletionListener {
                        playButton.setImageResource(R.drawable.ic_msg_voice_play)
                        for (i in adapter!!.getPlayingArray().indices) {
                            if (adapter.getPlayingArray()[i]) {
                                adapter.setAdapterValue(i, false)
                                adapter.setButtonValue(i, R.drawable.ic_msg_voice_play)
                            }
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            }
        }
    }

    fun pauseAudio(adapter: ChatAdapter? = null) {
        if (playerStatus == "pause") {
            mediaPlayer!!.pause()

            playerStatus = "play"

            if (adapter != null) {
                for (i in adapter.getPlayingArray().indices) {
                    if (adapter.getPlayingArray()[i]) {
                        adapter.setAdapterValue(i, false)
                        adapter.setButtonValue(i, R.drawable.ic_msg_voice_play)
                    }
                }
            }
        }
    }
}