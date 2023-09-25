package com.ssrlab.assistant.client

import android.util.Log
import com.ssrlab.assistant.utils.REQUEST_TIME_OUT
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object MessageClient {

    private var client: OkHttpClient? = null

    fun sendMessage(message: String, speaker: String, role: String, onResponse: (String, String) -> Unit, onFailure: (String) -> Unit) {
        if (client == null) {
            client = OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .build()
        }

        val mediaType = "application/json".toMediaType()
        val body = "{\"text\":\"$message\",\"voice_type\":\"$speaker\",\"role\":\"$role\"}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://ml1.ssrlab.by/api/android/text")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message!!)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val jsonObject = responseBody?.let { JSONObject(it) }

                    val audio = jsonObject?.getString("audio")
                    val text = jsonObject?.getString("text")

                    if (audio != null && text != null) onResponse(audio, text)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    if (responseBody != null) {
                        Log.e("JSON Exception", responseBody)
                    }
                }
            }
        })
    }

    fun sendMessage(audioFile: File, speaker: String, role: String, onResponse: (String, String) -> Unit, onFailure: (String) -> Unit) {
        if (client == null) {
            client = OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .build()
        }

        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("audio_file", audioFile.name, audioFile.asRequestBody("application/octet-stream".toMediaType()))
            .addFormDataPart("voice_type", speaker)
            .addFormDataPart("role", role)
            .build()
        val request = Request.Builder()
            .url("https://ml1.ssrlab.by/api/android/voice")
            .post(body)
            .build()

        client?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message!!)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val jsonObject = responseBody?.let { JSONObject(it) }

                    val audio = jsonObject?.getString("audio")
                    val text = jsonObject?.getString("text")

                    if (audio != null && text != null) onResponse(audio, text)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    if (responseBody != null) {
                        Log.e("JSON Exception", responseBody)
                    }
                }
            }
        })
    }

    fun getAudio(link: String, file: File) {

    }
}