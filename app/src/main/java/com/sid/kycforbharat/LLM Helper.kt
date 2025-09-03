package com.sid.kycforbharat

import android.content.Context
import android.speech.tts.TextToSpeech
import org.json.JSONObject
import java.io.IOException
import java.util.*

object LLMHelper {
    private var tts: TextToSpeech? = null
    private var messages: JSONObject? = null

    fun initialize(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale("en", "IN"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    }
                }
            }
        }
        if (messages == null) {
            messages = loadMessages(context)
        }
    }

    private fun loadMessages(context: Context): JSONObject {
        val jsonString: String
        try {
            jsonString = context.assets.open("messages.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return JSONObject()
        }
        return JSONObject(jsonString)
    }

    fun getPrompt(key: String): String {
        return messages?.optString(key) ?: "I am sorry, I can't find that instruction."
    }

    fun speak(context: Context, text: String) {
        if (tts == null) {
            initialize(context)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "llm_prompt")
    }
}
