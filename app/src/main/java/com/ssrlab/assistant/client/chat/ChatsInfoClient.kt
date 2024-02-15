package com.ssrlab.assistant.client.chat

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.R
import com.ssrlab.assistant.db.objects.info.ChatInfoObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ChatsInfoClient(private val context: Context) {

    private var chatsInfoClient = OkHttpClient.Builder().build()
    private val fireAuth = FirebaseAuth.getInstance()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun getAllChats(onSuccess: (ArrayList<ChatInfoObject>) -> Unit, onFailure: (String) -> Unit) {
        getAllChatsBack(onSuccess, onFailure)
    }

    private fun getAllChatsBack(onSuccess: (ArrayList<ChatInfoObject>) -> Unit, onFailure: (String) -> Unit) {
        val uid = fireAuth.currentUser?.uid ?: "null"

        if (uid != "null") {
            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/chats")
                .addHeader("x-user-id", uid)
                .build()

            chatsInfoClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    val jObject = responseBody?.let { JSONObject(it) }

                    val chatInfoArray = arrayListOf<ChatInfoObject>()

                    val chatIdArray = jObject?.getJSONArray("chat_ids")
                    if (chatIdArray?.length() != 0) {
                        for (i in 0 until chatIdArray?.length()!!) {
                            getChatInfoById(chatIdArray[i] as String, {
                                chatInfoArray.add(it)
                            }, { errorMessage ->
                                onFailure(errorMessage)
                            })
                        }

                        while (chatInfoArray.size != chatIdArray.length()) {
                            scope.launch { delay(100) }
                        }

                        onSuccess(chatInfoArray)
                    } else {
                        val errorMessage = ContextCompat.getString(context, R.string.array_is_empty)
                        onFailure(errorMessage)
                    }
                }
            })
        } else {
            val errorMessage = ContextCompat.getString(context, R.string.null_uid)
            onFailure(errorMessage)
        }
    }

    private fun getChatInfoById(chatId: String, onSuccess: (ChatInfoObject) -> Unit, onFailure: (String) -> Unit) {
        val uid = fireAuth.currentUser?.uid ?: "null"

        if (uid != "null") {
            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/chat/$chatId")
                .addHeader("x-user-id", uid)
                .build()

            chatsInfoClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    val jObject = responseBody?.let { JSONObject(it) }

                    if (jObject != null) {
                        val chatInfoObject = ChatInfoObject(
                            jObject.getInt("id"),
                            jObject.getString("chat_id"),
                            jObject.getString("name"),
                            jObject.getString("bot_name"),
                            jObject.getString("role")
                        )

                        onSuccess(chatInfoObject)
                    } else {
                        val errorMessage = ContextCompat.getString(context, R.string.chat_info_is_empty)
                        onFailure(errorMessage)
                    }
                }
            })
        } else {
            val errorMessage = ContextCompat.getString(context, R.string.null_uid)
            onFailure(errorMessage)
        }
    }
}