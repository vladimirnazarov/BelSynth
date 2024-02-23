package com.ssrlab.assistant.client.chat

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.R
import com.ssrlab.assistant.client.chat.model.ChatsInfoInterface
import com.ssrlab.assistant.db.objects.chat.ChatInfoObject
import com.ssrlab.assistant.utils.REQUEST_TIME_OUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ChatsInfoClient(private val context: Context): ChatsInfoInterface {

    private var chatsInfoClient = OkHttpClient.Builder()
        .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
        .writeTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
        .build()

    private val fireAuth = FirebaseAuth.getInstance()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    /** Common chat info **/
    override fun getAllChats(onSuccess: (ArrayList<ChatInfoObject>) -> Unit, onFailure: (String) -> Unit) {
        checkUid({ uid ->
            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/chats")
                .addHeader("x-user-id", uid)
                .build()

            chatsInfoClient.newCall(request).enqueue(object: Callback {
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
        }, {
            val errorMessage = ContextCompat.getString(context, R.string.null_uid)
            onFailure(errorMessage)
        })
    }

    private fun getChatInfoById(chatId: String, onSuccess: (ChatInfoObject) -> Unit, onFailure: (String) -> Unit) {
        checkUid({ uid ->
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
        }, {
            val errorMessage = ContextCompat.getString(context, R.string.null_uid)
            onFailure(errorMessage)
        })
    }

    /** Control chats **/
    override fun createChat(name: String, role: String, botName: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        checkUid({ uid ->
            val mediaType = "application/json".toMediaType()
            val body = "{\"name\":\"$name\",\"role\":\"$role\",\"bot_name\":\"$botName\"}".toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/chat")
                .post(body)
                .addHeader("x-user-id", uid)
                .addHeader("Content-Type", "application/json")
                .build()

            chatsInfoClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    val jObject = responseBody?.let { JSONObject(it) }

                    val chatId = jObject?.getString("chat_id") ?: ""
                    if (chatId != "") onSuccess(chatId)
                    else {
                        val errorMessage = ContextCompat.getString(context, R.string.something_went_wrong)
                        onFailure(errorMessage)
                    }
                }
            })
        }, {
            val errorMessage = ContextCompat.getString(context, R.string.null_uid)
            onFailure(errorMessage)
        })
    }

    override fun editChat(name: String, role: String, botName: String, chatId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        checkUid({ uid ->
            val mediaType = "application/json".toMediaType()
            val body =  "{\"name\":\"$name\",\"role\":\"$role\",\"bot_name\":\"$botName\"}".toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/chat/$chatId")
                .put(body)
                .addHeader("x-user-id", uid)
                .addHeader("Content-Type", "application/json")
                .build()

            chatsInfoClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    val jObject = responseBody?.let { JSONObject(it) }

                    try {
                        val message = jObject?.getString("message")
                        if (message == "Chat edited successfully") onSuccess()
                        else {
                            val errorMessage = ContextCompat.getString(context, R.string.something_went_wrong)
                            onFailure(errorMessage)
                        }
                    } catch (e: JSONException) {
                        onFailure(e.message.toString())
                    }
                }
            })
        }, {
            val errorMessage = ContextCompat.getString(context, R.string.null_uid)
            onFailure(errorMessage)
        })
    }

    override fun deleteChat(chatId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        checkUid({ uid ->
            val mediaType = "text/plain".toMediaType()
            val body = "".toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/chat/$chatId")
                .addHeader("x-user-id", uid)
                .method("DELETE", body)
                .build()

            chatsInfoClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    val jObject = responseBody?.let { JSONObject(it) }

                    try {
                        val message = jObject?.getString("message") ?: ""
                        if (message == "Chat deleted successfully") onSuccess()
                        else {
                            val errorMessage = ContextCompat.getString(context, R.string.something_went_wrong)
                            onFailure(errorMessage)
                        }
                    } catch (e: JSONException) {
                        onFailure(e.message.toString())
                    }
                }
            })
        }, {
            val errorMessage = ContextCompat.getString(context, R.string.null_uid)
            onFailure(errorMessage)
        })
    }

    /** Control back **/
    private fun checkUid(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val uid = fireAuth.currentUser?.uid ?: "null"

        if (uid != "null") onSuccess(uid)
        else onFailure()
    }
}