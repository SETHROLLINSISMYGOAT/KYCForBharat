package com.sid.kycforbharat


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sid.kycforbharat.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val status = intent.getStringExtra("status") ?: "success"
        if (status == "failed") {
            binding.tvTitle.text = "KYC Failed / KYC असफल"
            binding.tvDetail.text = "Reference: KYC-FAILED"
        } else {
            binding.tvTitle.text = "KYC Successful / KYC सफल"
            binding.tvDetail.text = "Reference: KYC-" + System.currentTimeMillis().toString().takeLast(6)
        }

        binding.btnDone.setOnClickListener {
            finishAffinity()
        }
    }
}
