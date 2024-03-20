package com.ssrlab.assistant.db.client

import com.google.gson.Gson
import com.ssrlab.assistant.db.objects.Message
import com.ssrlab.assistant.ui.chat.ChatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

class DatabaseClient(activity: ChatActivity) {

    private val absolutePath = File("${activity.filesDir}/messages")

    init {
        makeDirectory(absolutePath)
    }

    fun writeToFile(chatId: String, messages: ArrayList<Message>) {
        val file = createFile(chatId)

        val gson = Gson()
        val jArray = gson.toJson(messages)

        try {
            file.writeText(jArray)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun readFile(chatId: String): ArrayList<Message>? {
        val file = File(absolutePath, "$chatId/chat_data.json")
        return withContext(Dispatchers.IO) {
            if (file.exists()) {
                val messages = ArrayList<Message>()
                val jsonArray = JSONArray(file.readText())

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    val text = jsonObject.getString("text")
                    val role = jsonObject.getString("role")
                    val audio = jsonObject.getString("audio")
                    val message = Message(text, role, audio)
                    messages.add(message)
                }

                messages
            } else {
                null
            }
        }
    }

    private fun createFile(chatId: String): File {
        val filePath = File(absolutePath, chatId)
        if (!filePath.exists()) filePath.mkdirs()
        return File(filePath, "chat_data.json")
    }

    private fun makeDirectory(filePath: File, withRemoval: Boolean = false) {
        if (!filePath.exists()) filePath.mkdirs()
        else if (filePath.exists() && withRemoval) {
            filePath.delete()
            makeDirectory(filePath)
        }
    }
}