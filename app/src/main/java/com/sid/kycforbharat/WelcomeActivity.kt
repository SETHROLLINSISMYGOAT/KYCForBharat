package com.sid.kycforbharat

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sid.kycforbharat.databinding.ActivityWelcomeBinding
import java.util.*

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private var tts: TextToSpeech? = null
    private var selectedLang: String = "English"
    private var voiceAssistEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val langs = listOf("English", "हिन्दी")
        binding.spLanguage.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, langs).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spLanguage.setSelection(0)
        binding.spLanguage.setOnItemSelectedListener(object: android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedLang = langs[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        binding.cbVoiceAssist.setOnCheckedChangeListener { _, checked ->
            voiceAssistEnabled = checked
            if (checked) {
                initTts(selectedLang)
            } else {
                tts?.stop(); tts?.shutdown(); tts = null
            }
        }

        binding.btnStart.setOnClickListener {
            startActivity(Intent(this, ConsentPrepActivity::class.java).apply {
                putExtra("lang", selectedLang)
                putExtra("voiceAssist", voiceAssistEnabled)
            })
        }
    }

    private fun initTts(lang: String) {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = if (lang == "हिन्दी") {
                    tts?.setLanguage(Locale("hi", "IN"))
                } else {
                    tts?.setLanguage(Locale.ENGLISH)
                }
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "$lang voice not available on this device", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        tts?.stop(); tts?.shutdown(); super.onDestroy()
    }
}
