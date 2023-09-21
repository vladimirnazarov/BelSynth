package com.ssrlab.assistant.rv

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssrlab.assistant.R
import com.ssrlab.assistant.db.ChatMessage

@Suppress("KotlinConstantConditions")
class ChatAdapter(
    private val messages: ArrayList<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        var messageView: View? = null
        when (viewType) {
            0 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_bot, parent, false)
            1 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_user, parent, false)
            2 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_user_voice, parent, false)
            3 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_empty_view, parent, false)
        }

        return ChatViewHolder(messageView!!)
    }

    override fun getItemCount(): Int = messages.size + 3

    @Suppress("ControlFlowWithEmptyBody")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        var message: ChatMessage? = null
        if (position <= messages.size - 1) message = messages[position]
        val view = holder.itemView

        if (position >= messages.size) { }
        else if (position % 2 == 0) {

            val textMsg = view.findViewById<TextView>(R.id.rv_bot_msg)
            val share = view.findViewById<ImageButton>(R.id.rv_bot_share)
            val clipboard = view.findViewById<ImageButton>(R.id.rv_bot_clipboard)

            textMsg.text = message!!.text
            share.setOnClickListener { Log.d("share", "${message.id} share") }
            clipboard.setOnClickListener { Log.d("clipboard", "${message.id} clipboard") }

        } else if (position % 2 != 0 && message!!.audioSend == null) {

            val textMsg = view.findViewById<TextView>(R.id.rv_user_msg)

            textMsg.text = message.text

        } else if (position % 2 != 0 && message!!.audioSend != null) {

            val playButton = view.findViewById<ImageButton>(R.id.rv_user_voice_button)

            playButton.setOnClickListener { Log.d("play", "${message.id} play") }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= messages.size) 3
        else if ((position) % 2 == 0) 0
        else {
            if (messages[position].audioSend == null) 1
            else 2
        }
    }
}