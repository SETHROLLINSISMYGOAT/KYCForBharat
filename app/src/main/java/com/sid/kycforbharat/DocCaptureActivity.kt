package com.sid.kycforbharat

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.sid.kycforbharat.databinding.ActivityDocCaptureBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DocCaptureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDocCaptureBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        if (Utils.ensureCameraPermission(this)) startCamera()

        val initialPrompt = LLMHelper.getPrompt("doc_capture_start")
        binding.tvTip.text = initialPrompt
        LLMHelper.speak(this, initialPrompt)

        binding.btnCaptureFront.setOnClickListener { takePhoto("doc_front") }
        binding.btnCaptureBack.setOnClickListener { takePhoto("doc_back") }


        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, ReviewActivity::class.java))
        }


        if (KycData.method == "PAN") {
            binding.btnCaptureBack.visibility = android.view.View.GONE
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto(prefix: String) {
        val imgCap = imageCapture ?: return
        val file = File(cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imgCap.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("DocCapture", "Photo capture failed: ${exc.message}")
                val failPrompt = LLMHelper.getPrompt("capture_fail")
                binding.tvTip.text = failPrompt
                LLMHelper.speak(applicationContext, failPrompt)
            }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                if (file.length() < 8_000) {
                    val blurryPrompt = LLMHelper.getPrompt("image_blurry")
                    binding.tvTip.text = blurryPrompt
                    LLMHelper.speak(applicationContext, blurryPrompt)
                } else {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    runOnUiThread {
                        binding.imgPreview.setImageBitmap(bmp)
                    }

                    if (prefix == "doc_front") {
                        KycData.frontPath = file.absolutePath
                        binding.btnCaptureFront.text = "Retake Front"
                    } else {
                        KycData.backPath = file.absolutePath
                        binding.btnCaptureBack.text = "Retake Back"
                    }

                    val successPrompt = LLMHelper.getPrompt("capture_success")
                    binding.tvTip.text = successPrompt
                    LLMHelper.speak(applicationContext, successPrompt)

                    checkIfReady()
                }
            }
        })
    }

    private fun checkIfReady() {
        val method = KycData.method
        if (method == "PAN") {

            if (KycData.frontPath != null) {
                binding.btnNext.isEnabled = true
            }
        } else {

            if (KycData.frontPath != null && KycData.backPath != null) {
                binding.btnNext.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
