package com.ssrlab.assistant.client.chat

import android.content.Context
import androidx.core.content.ContextCompat
import com.ssrlab.assistant.R
import com.ssrlab.assistant.client.CommonClient
import com.ssrlab.assistant.db.objects.ChatInfoObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ChatsInfoClient(private val context: Context): CommonClient() {

    /** Common chat info **/
    fun getAllChats(onSuccess: (ArrayList<ChatInfoObject>) -> Unit, onFailure: (String?) -> Unit) {
        checkUid({ uid ->
            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/chats")
                .addHeader("x-user-id", uid)
                .build()

            val dialog = initDialog(context)
            dialog.show()

            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    dialog.dismiss()
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    val jObject = responseBody?.let { JSONObject(it) }

                    val chatInfoArray = arrayListOf<ChatInfoObject>()

                    try {
                        val chatsArray = jObject?.getJSONArray("chats")
                        val chatIdArray = arrayListOf<String>()

                        if (chatsArray != null) {
                            for (i in 0 until chatsArray.length())
                                chatIdArray.add(chatsArray.getJSONObject(i).getString("chat_id"))
                        }

                        while (chatIdArray.size != chatsArray!!.length()) scope.launch { delay(100) }

                        if (chatIdArray.size != 0) {
                            for (i in 0 until chatIdArray.size) {
                                getChatInfoById(chatIdArray[i], {
                                    chatInfoArray.add(it)
                                }, { errorMessage ->
                                    onFailure(errorMessage)
                                })
                            }

                            while (chatInfoArray.size != chatIdArray.size) {
                                scope.launch { delay(100) }
                            }

                            dialog.dismiss()
                            onSuccess(chatInfoArray)
                        } else {
                            dialog.dismiss()
                            onFailure(null)
                        }

                    } catch (e: JSONException) {
                        dialog.dismiss()
                        onFailure(e.message.toString())
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

            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    
                    try {
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
                            val errorMessage =
                                ContextCompat.getString(context, R.string.chat_info_is_empty)
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

    /** Control chats **/
    fun createChat(name: String, role: String, botName: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        checkUid({ uid ->
            val mediaType = "application/json".toMediaType()
            val body = "{\"name\":\"$name\",\"role\":\"$role\",\"bot_name\":\"$botName\"}".toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/chat")
                .post(body)
                .addHeader("x-user-id", uid)
                .addHeader("Content-Type", "application/json")
                .build()

            val dialog = initDialog(context)
            dialog.show()

            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()

                    try {
                        val jObject = responseBody?.let { JSONObject(it) }

                        val chatId = jObject?.getString("chat_id") ?: ""
                        if (chatId != "") {
                            dialog.dismiss()
                            onSuccess(chatId)
                        }
                        else {
                            dialog.dismiss()

                            val errorMessage = ContextCompat.getString(context, R.string.something_went_wrong)
                            onFailure(errorMessage)
                        }
                    } catch (e: JSONException) {
                        dialog.dismiss()
                        onFailure(e.message.toString())
                    }
                }
            })
        }, {
            val errorMessage = ContextCompat.getString(context, R.string.null_uid)
            onFailure(errorMessage)
        })
    }

    fun getMessagesCount(chatId: String, onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        checkUid({ uid ->
            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/chat/msg_count/$chatId")
                .addHeader("x-user-id", uid)
                .build()

            val dialog = initDialog(context)
            dialog.show()

            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    dialog.dismiss()
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    val jObject = responseBody?.let { JSONObject(it) }

                    try {
                        val count = jObject?.getInt("msg_count") ?: 0

                        dialog.dismiss()
                        onSuccess(count)
                    } catch (e: JSONException) {
                        dialog.dismiss()
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