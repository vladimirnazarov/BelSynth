package com.ssrlab.assistant.rv

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.ssrlab.assistant.R
import com.ssrlab.assistant.client.chat.ChatMessagesClient
import com.ssrlab.assistant.db.objects.Message
import com.ssrlab.assistant.ui.chat.ChatActivity
import com.ssrlab.assistant.utils.BOT
import com.ssrlab.assistant.utils.CLIPBOARD_TEXT
import com.ssrlab.assistant.utils.NULL
import com.ssrlab.assistant.utils.USER
import com.ssrlab.assistant.utils.helpers.text.ChatHelper
import com.ssrlab.assistant.utils.helpers.other.MediaPlayerObject
import java.io.File

class ChatAdapter(
    private val messages: ArrayList<Message>,
    private val chatActivity: ChatActivity,
    private val chatMessagesClient: ChatMessagesClient
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolderNew>() {

    private val arrayOfButtons = ArrayList<ImageButton>()
    private val playingArray = ArrayList<Boolean>()

    private val mediaObject = MediaPlayerObject

    inner class ChatViewHolderNew(item: View) : RecyclerView.ViewHolder(item)

    /**
     * 0 - Empty block
     * 1 - Bot
     * 2 - User text
     * 3 - User voice
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolderNew {
        var messageView: View? = null
        when (viewType) {
            0 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_empty_view, parent, false)
            1 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_bot, parent, false)
            2 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_user, parent, false)
            3 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_user_voice, parent, false)
        }

        return ChatViewHolderNew(messageView!!)
    }

    override fun getItemCount(): Int = messages.size + 2

    override fun onBindViewHolder(holder: ChatViewHolderNew, position: Int) {
        val view = holder.itemView

        if (position < messages.size) {
            when (messages[position].role) {
                BOT -> {
                    val textMsg = view.findViewById<TextView>(R.id.rv_bot_msg)
                    val share = view.findViewById<ImageButton>(R.id.rv_bot_share)
                    val clipboard = view.findViewById<ImageButton>(R.id.rv_bot_clipboard)
                    val playView = view.findViewById<ConstraintLayout>(R.id.rv_bot_msg_content)
                    val playButton = view.findViewById<ImageButton>(R.id.rv_bot_play)

                    textMsg.text = messages[position].text
                    share.setOnClickListener {
                        (textMsg.text as String?)?.let { it1 -> chatActivity.shareIntent(it1) }
                    }

                    clipboard.setOnClickListener {
                        val clipboardManager = chatActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(CLIPBOARD_TEXT, textMsg.text)
                        clipboardManager.setPrimaryClip(clip)

                        Toast.makeText(chatActivity, chatActivity.resources.getText(R.string.text_copied), Toast.LENGTH_SHORT).show()
                        chatActivity.currentFocus?.clearFocus()
                    }

                    if (messages[position] == messages[0]) {
                        playView.isClickable = false
                        playButton.visibility = View.GONE
                    } else {
                        val audioLink = messages[position].audio

                        playView.setOnClickListener { playAudio(link = audioLink) }
                        playButton.setOnClickListener { playAudio(link = audioLink) }
                    }
                }

                USER -> {
                    if (messages[position].audio == NULL) {
                        val textMsg = view.findViewById<TextView>(R.id.rv_user_msg)
                        textMsg.text = messages[position].text
                    } else {
                        val playButton = view.findViewById<ImageButton>(R.id.rv_user_voice_button)
                        val duration = view.findViewById<TextView>(R.id.rv_user_voice_duration)

                        arrayOfButtons.add(playButton)
                        playingArray.add(false)

                        val audioPath = messages[position].audio
                        val audioLink = if (audioPath.startsWith("\"") && audioPath.endsWith("\"")) {
                            audioPath.substring(1, audioPath.length - 1)
                        } else audioPath

                        duration.text = ChatHelper().convertToTimerMode(mediaObject.getAudioDuration(audioLink))

                        playButton.setOnClickListener {
                            if (playingArray[arrayOfButtons.indexOf(playButton)]) {
                                mediaObject.pauseAudio(this@ChatAdapter)
                            } else {
                                playAudio(playButton, audioLink)
                                playingArray[arrayOfButtons.indexOf(playButton)] = true
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 0 - Empty block
     * 1 - Bot
     * 2 - User text
     * 3 - User voice
     */
    override fun getItemViewType(position: Int): Int {
        return if (position >= messages.size) 0
        else when (messages[position].role) {
            BOT -> 1
            USER -> {
                if (messages[position].audio == NULL) 2
                else 3
            }

            else -> 0
        }
    }

    fun playAudio(playButton: ImageButton? = null, link: String) {
        for (i in 0 until playingArray.size) {
            if (playingArray[i]) {
                arrayOfButtons[i].setImageResource(R.drawable.ic_msg_voice_play)
                playingArray[i] = false
            }
        }

        val path = File("${chatActivity.cacheDir}/temp/")
        if (!path.exists()) path.mkdirs()

        val file = File(path, "temp_playable.mp3")
        chatMessagesClient.getAudio(link, file, {
            mediaObject.pauseAudio()
            mediaObject.initializeMediaPlayer(chatActivity, file.toUri())

            if (playButton != null) mediaObject.playAudio(playButton, this@ChatAdapter)
            else mediaObject.playAudio()
        }) {
            chatActivity.getChatViewModel().showErrorMessage(it)
        }
    }

    fun getPlayingArray() = playingArray
    fun setAdapterValue(atPos: Int, value: Boolean) { playingArray[atPos] = value }
    fun setButtonValue(atPos: Int, value: Int) { arrayOfButtons[atPos].setImageResource(value) }
}