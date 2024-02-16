package com.ssrlab.assistant.client.chat

import android.util.Log
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.ssrlab.assistant.utils.REQUEST_TIME_OUT
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
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
                        onFailure(responseBody)
                    }
                }

                response.close()
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

        val inputPath = audioFile.path
        val outputPath = "${audioFile.parent}/convertedAudio.mp3"

        convertToMp3(inputPath, outputPath) { success ->
            if (success) {
                val file = File(outputPath)

                val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("audio_file", file.name, file.asRequestBody("application/octet-stream".toMediaType()))
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
                                onFailure(responseBody)
                            }
                        }

                        response.close()
                    }
                })
            } else {
                onFailure("Unable to convert audio, try again")
            }
        }
    }

    fun getAudio(link: String, file: File, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (client == null) {
            client = OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
                .build()
        }

        val request = Request.Builder()
            .url(link)
            .build()

        client?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message!!)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("File response", "Response error")
                    response.body?.string()?.let { onFailure(it) }
                }
                else {
                    val fos = FileOutputStream(file)
                    fos.write(response.body?.bytes())
                    fos.close()

                    onSuccess()
                }

                response.close()
            }
        })
    }

    private fun convertToMp3(inputPath: String, outputPath: String, callback: (Boolean) -> Unit) {
        val command = arrayOf(
            "-i", inputPath, "-y",
            "-vn", //disable video
            "-ar", "44100", //frequency
            "-ac", "2", //channels
            "-b:a", "32k", //bitrate
            outputPath
        )

        FFmpeg.executeAsync(command) { _, returnCode ->
            if (returnCode == RETURN_CODE_SUCCESS) {
                callback(true)
            } else {
                callback(false)
            }
        }
    }
}