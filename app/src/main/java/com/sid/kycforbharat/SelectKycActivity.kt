package com.sid.kycforbharat


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sid.kycforbharat.databinding.ActivitySelectKycBinding

class SelectKycActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectKycBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectKycBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDigiLocker.setOnClickListener {
            startActivity(Intent(this, DigiLockerActivity::class.java))
        }
        binding.btnAadhaar.setOnClickListener {
            KycData.method = "Aadhaar"
            startActivity(Intent(this, DocCaptureActivity::class.java))
        }
        binding.btnPAN.setOnClickListener {
            KycData.method = "PAN"
            startActivity(Intent(this, DocCaptureActivity::class.java))
        }
        binding.btnDL.setOnClickListener {
            KycData.method = "DL"
            startActivity(Intent(this, DocCaptureActivity::class.java))
        }
        binding.btnVoter.setOnClickListener {
            KycData.method = "VoterID"
            startActivity(Intent(this, DocCaptureActivity::class.java))
        }
    }
}
