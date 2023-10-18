package com.ssrlab.assistant.rv

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
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
import com.ssrlab.assistant.db.*
import com.ssrlab.assistant.db.objects.BotMessage
import com.ssrlab.assistant.db.objects.MessageInfoObject
import com.ssrlab.assistant.db.objects.UserMessage
import com.ssrlab.assistant.db.objects.UserVoiceMessage
import com.ssrlab.assistant.ui.chat.ChatActivity
import com.ssrlab.assistant.utils.helpers.ChatHelper
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject.initializeMediaPlayer
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject.pauseAudio
import com.ssrlab.assistant.utils.helpers.objects.MediaPlayerObject.playAudio

@Suppress("KotlinConstantConditions")
class ChatAdapter(
    private val messageI: ArrayList<MessageInfoObject>,
    private val botMessage: ArrayList<BotMessage>,
    private val userMessage: ArrayList<UserMessage>,
    private val userVoiceMessage: ArrayList<UserVoiceMessage>,
    private val chatActivity: ChatActivity
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val arrayOfButtons = ArrayList<ImageButton>()
    private val playingArray = ArrayList<Boolean>()

    inner class ChatViewHolder(item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        var messageView: View? = null
        when (viewType) {
            1 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_bot, parent, false)
            2 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_user, parent, false)
            3 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_user_voice, parent, false)
            0 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_empty_view, parent, false)
        }

        return ChatViewHolder(messageView!!)
    }

    override fun getItemCount(): Int = messageI.size + 3

    @Suppress("ControlFlowWithEmptyBody")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val view = holder.itemView

        if (position < messageI.size) {
            when (messageI[position].role) {
                1 -> {
                    val textMsg = view.findViewById<TextView>(R.id.rv_bot_msg)
                    val share = view.findViewById<ImageButton>(R.id.rv_bot_share)
                    val clipboard = view.findViewById<ImageButton>(R.id.rv_bot_clipboard)

                    textMsg.text = botMessage.find { it.id == messageI[position].id }?.text
                    share.setOnClickListener {
                        (textMsg.text as String?)?.let { it1 -> chatActivity.shareIntent(it1) }
                    }
                    clipboard.setOnClickListener {
                        val clipboardManager = chatActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("id_${messageI[position].id}_bot", textMsg.text)
                        clipboardManager.setPrimaryClip(clip)

                        Toast.makeText(chatActivity, chatActivity.resources.getText(R.string.text_copied), Toast.LENGTH_SHORT).show()
                        chatActivity.currentFocus?.clearFocus()
                    }

                    val playView = view.findViewById<ConstraintLayout>(R.id.rv_bot_msg_content)
                    val playButton = view.findViewById<ImageButton>(R.id.rv_bot_play)

                    if (position > 0) {
                        val audio = botMessage.find { it.id == messageI[position].id }?.audio?.toUri()!!

                        playView.setOnClickListener { playBotAudio(chatActivity, audio) }
                        playButton.setOnClickListener { playBotAudio(chatActivity, audio) }
                    } else {
                        playView.isClickable = false
                        playButton.visibility = View.GONE
                    }
                }
                2 -> {
                    val textMsg = view.findViewById<TextView>(R.id.rv_user_msg)

                    textMsg.text = userMessage.find { it.id == messageI[position].id }?.text
                }
                3 -> {
                    val playButton = view.findViewById<ImageButton>(R.id.rv_user_voice_button)
                    val duration = view.findViewById<TextView>(R.id.rv_user_voice_duration)
                    val audioFile = userVoiceMessage.find { it.id == messageI[position].id }?.audio

                    arrayOfButtons.add(playButton)
                    playingArray.add(false)

                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(audioFile?.path)
                        val durationValue = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
                        duration.text = ChatHelper().convertToTimerMode(durationValue)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        retriever.release()
                    }

                    playButton.setOnClickListener {
                        if (playingArray[arrayOfButtons.indexOf(playButton)]) {
                            pauseAudio(this@ChatAdapter)
                        } else {
                            pauseAudio(this@ChatAdapter)
                            initializeMediaPlayer(chatActivity, audioFile?.toUri()!!)
                            playAudio(playButton, this@ChatAdapter)

                            playingArray[arrayOfButtons.indexOf(playButton)] = true
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < messageI.size) {
            when (messageI[position].role) {
                1 -> 1
                2 -> 2
                3 -> 3
                else -> 0
            }
        } else 0
    }

    private fun playBotAudio(chatActivity: ChatActivity, audio: Uri) {
        for (i in 0 until playingArray.size) {
            if (playingArray[i]) {
                arrayOfButtons[i].setImageResource(R.drawable.ic_msg_voice_play)
                playingArray[i] = false
            }
        }

        pauseAudio()
        initializeMediaPlayer(chatActivity, audio)
        playAudio()
    }

    fun getPlayingArray() = playingArray
    fun setAdapterValue(atPos: Int, value: Boolean) { playingArray[atPos] = value }
    fun setButtonValue(atPos: Int, value: Int) { arrayOfButtons[atPos].setImageResource(value) }
}