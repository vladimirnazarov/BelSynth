package com.ssrlab.assistant.utils.helpers.objects

import android.media.MediaPlayer
import android.widget.ImageButton
import com.ssrlab.assistant.R
import com.ssrlab.assistant.rv.ChatAdapterNew
import com.ssrlab.assistant.utils.PAUSE
import com.ssrlab.assistant.utils.PLAY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object MediaPlayerObjectNew {

    private var mediaPlayer: MediaPlayer? = null
    private var playerStatus = ""

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    fun initializeMediaPlayer(link: String) {
        playerStatus = PLAY
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer!!.setDataSource(link)
            mediaPlayer!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playAudio(playButton: ImageButton? = null, adapter: ChatAdapterNew? = null) {
        scope.launch {
            when (playerStatus) {
                PAUSE -> {
                    mediaPlayer!!.pause()
                    playerStatus = PLAY

                    playButton?.setImageResource(R.drawable.ic_msg_voice_play)
                }

                PLAY -> {
                    try {
                        mediaPlayer!!.start()
                        playerStatus = PAUSE

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

    fun pauseAudio(adapter: ChatAdapterNew? = null) {
        if (playerStatus == PAUSE) {
            mediaPlayer!!.pause()

            playerStatus = PLAY

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

    fun getAudioDuration(link: String): Int {
        val newMediaPlayer = MediaPlayer()

        try {
            newMediaPlayer.setDataSource(link)
            return newMediaPlayer.duration
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mediaPlayer?.duration ?: 0
    }
}