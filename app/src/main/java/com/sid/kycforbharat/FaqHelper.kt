package com.sid.kycforbharat

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import com.sid.kycforbharat.databinding.ActivityFaqBinding
import org.json.JSONArray
import java.util.*

class FaqHelper : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityFaqBinding
    private lateinit var tts: TextToSpeech
    private var faqText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)


        val input = assets.open("faq.json")
        val json = input.bufferedReader().use { it.readText() }
        val arr = JSONArray(json)

        val builder = StringBuilder()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            builder.append("Q: ").append(obj.getString("q")).append("\n")
            builder.append("A: ").append(obj.getString("a")).append("\n\n")
        }
        faqText = builder.toString()
        binding.tvFaq.text = faqText


        tts.speak(faqText, TextToSpeech.QUEUE_FLUSH, null, "FAQ_ID")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}
