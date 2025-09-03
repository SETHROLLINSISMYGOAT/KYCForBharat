package com.sid.kycforbharat

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import com.sid.kycforbharat.databinding.ActivityConsentPrepBinding
import java.util.Locale

class ConsentPrepActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConsentPrepBinding
    private var tts: TextToSpeech? = null
    private var lang: String = "English"
    private var voiceAssist: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityConsentPrepBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lang = intent.getStringExtra("lang") ?: "English"
        voiceAssist = intent.getBooleanExtra("voiceAssist", false)


        if (voiceAssist) {
            tts = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    if (lang == "हिन्दी") {
                        tts?.language = Locale("hi", "IN")
                        tts?.speak(
                            "कृपया सहमति दें ताकि हम आगे बढ़ सकें",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "consent"
                        )
                    } else {
                        tts?.language = Locale.ENGLISH
                        tts?.speak(
                            "Please give consent to proceed",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "consent"
                        )
                    }
                }
            }
        }


        binding.cbConsent.setOnCheckedChangeListener { _, isChecked ->
            binding.btnContinue.isEnabled = isChecked
        }

        binding.btnContinue.setOnClickListener {
            startActivity(Intent(this, SelectKycActivity::class.java))
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
