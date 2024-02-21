package com.ssrlab.assistant.client.chat

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.client.chat.model.ChatMessagesInterface
import com.ssrlab.assistant.db.objects.messages.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class ChatMessagesClient(private val context: Context): ChatMessagesInterface {

    private var chatClient = OkHttpClient.Builder().build()
    private val fireAuth = FirebaseAuth.getInstance()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun loadMessages(chatId: String, onSuccess: (ArrayList<Message>) -> Unit, onFailure: (String) -> Unit) {
        loadMessagesBack(chatId, onSuccess, onFailure)
    }

    private fun loadMessagesBack(chatId: String, onSuccess: (ArrayList<Message>) -> Unit, onFailure: (String) -> Unit) {
        checkUid({ uid ->
            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/message/$chatId")
                .addHeader("x-user-id", uid)
                .build()

            chatClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    val jArray = JSONArray(responseBody)

                    try {
                        if (jArray.length() != 0) {
                            for (i in 0 until jArray.length()) {
                                
                            }
                        }
                    } catch (e: JSONException) {
                        onFailure(e.message.toString())
                    }
                }
            })
        }, {

        })
    }

    private fun checkUid(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val uid = fireAuth.currentUser?.uid ?: "null"

        if (uid != "null") onSuccess(uid)
        else onFailure()
    }
}