package com.sid.kycforbharat



import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sid.kycforbharat.databinding.ActivityDigilockerBinding

class DigiLockerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDigilockerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDigilockerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenWeb.setOnClickListener {
            val url = "https://digilocker.gov.in"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
        binding.btnSimulateSuccess.setOnClickListener {
            KycData.method = "DigiLocker"
            startActivity(Intent(this, FaceAuthActivity::class.java))
        }
        binding.btnSimulateFail.setOnClickListener {
            val i = Intent(this, ResultActivity::class.java)
            i.putExtra("status", "failed")
            startActivity(i)
        }
    }
}
