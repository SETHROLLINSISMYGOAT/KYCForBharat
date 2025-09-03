package com.sid.kycforbharat

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sid.kycforbharat.databinding.ActivityReviewConfirmBinding
import java.io.File

class ReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewConfirmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayKycData()

        binding.btnConfirm.setOnClickListener {

            val i = Intent(this, UploadQueueActivity::class.java)
            startActivity(i)
        }

        binding.btnEdit.setOnClickListener {

            startActivity(Intent(this, DocCaptureActivity::class.java))
        }
    }

    private fun displayKycData() {

        val method = KycData.method.ifEmpty { "N/A" }
        binding.tvDocMethod.text = "Document Type: $method"

        KycData.frontPath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    binding.imgFrontDoc.setImageBitmap(bitmap)
                    binding.imgFrontDoc.visibility = android.view.View.VISIBLE
                } else {

                }
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Error loading front image: ${e.message}")
            }
        }

        if (KycData.method in listOf("Aadhaar", "DL", "VoterID")) {
            KycData.backPath?.let { path ->
                try {
                    val file = File(path)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        binding.imgBackDoc.setImageBitmap(bitmap)
                        binding.imgBackDoc.visibility = android.view.View.VISIBLE
                    } else {

                    }
                } catch (e: Exception) {
                    Log.e("ReviewActivity", "Error loading back image: ${e.message}")
                }
            }
        }


        KycData.facePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    binding.imgFace.setImageBitmap(bitmap)
                    binding.imgFace.visibility = android.view.View.VISIBLE
                    binding.tvFaceStatus.text = "Face Captured Successfully"
                } else {

                }
            } catch (e: Exception) {
                Log.e("ReviewActivity", "Error loading face image: ${e.message}")
            }
        }
    }
}
