package com.sid.kycforbharat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceDetection
import com.sid.kycforbharat.databinding.ActivityFaceAuthBinding
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceAuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFaceAuthBinding
    private lateinit var cameraExecutor: ExecutorService
    private var analysisUseCase: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null

    private var seenOpen = false
    private var seenClosed = false
    private var livenessConfirmed = false
    private var lastFaceTime = System.currentTimeMillis()

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        if (Utils.ensureCameraPermission(this)) startCamera()


        binding.btnContinue.setOnClickListener {
            startActivity(Intent(this, ReviewActivity::class.java))
        }

        binding.btnRetake.setOnClickListener {
            livenessConfirmed = false
            seenOpen = false
            seenClosed = false
            KycData.facePassed = false


            binding.btnContinue.isEnabled = false
            binding.btnRetake.visibility = android.view.View.GONE
            val retakePrompt = LLMHelper.getPrompt("face_auth_start")
            binding.tvPrompt.text = retakePrompt
            LLMHelper.speak(this, retakePrompt)

            startCamera()
        }


        val initialPrompt = LLMHelper.getPrompt("face_auth_start")
        binding.tvPrompt.text = initialPrompt
        LLMHelper.speak(this, initialPrompt)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            imageCapture = ImageCapture.Builder().build()

            analysisUseCase = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, ImageAnalyzer())
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysisUseCase, imageCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class ImageAnalyzer : ImageAnalysis.Analyzer {
        @OptIn(markerClass = [ExperimentalGetImage::class])
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: return
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        binding.tvPrompt.text = LLMHelper.getPrompt("face_not_detected")
                        return@addOnSuccessListener
                    }
                    val face = faces[0]
                    checkLiveness(face)
                }
                .addOnFailureListener {
                    binding.tvPrompt.text = LLMHelper.getPrompt("face_auth_fail")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun checkLiveness(face: Face) {
        if (livenessConfirmed) return

        if (face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null) {
            val leftEyeOpen = face.leftEyeOpenProbability!! > 0.4
            val rightEyeOpen = face.rightEyeOpenProbability!! > 0.4

            if (leftEyeOpen && rightEyeOpen) {
                seenOpen = true
            }
            if (!leftEyeOpen && !rightEyeOpen) {
                seenClosed = true
            }
        }

        if (seenOpen && seenClosed) {
            livenessConfirmed = true
            KycData.facePassed = true

            val successPrompt = LLMHelper.getPrompt("liveness_passed")
            binding.tvPrompt.text = successPrompt
            LLMHelper.speak(this, successPrompt)

            captureFaceSnapshot()
        } else {
            val blinkPrompt = LLMHelper.getPrompt("blink_prompt")
            binding.tvPrompt.text = blinkPrompt
        }
    }

    private fun captureFaceSnapshot() {
        val ic = imageCapture ?: return
        val file = File(cacheDir, "face_${System.currentTimeMillis()}.jpg")
        val outOpts = ImageCapture.OutputFileOptions.Builder(file).build()
        ic.takePicture(outOpts, ContextCompat.getMainExecutor(this), object: ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                KycData.facePath = file.absolutePath
                runOnUiThread {
                    binding.btnContinue.isEnabled = true
                    binding.btnRetake.visibility = android.view.View.VISIBLE
                    val capturePrompt = LLMHelper.getPrompt("face_captured")
                    binding.tvPrompt.text = capturePrompt
                    LLMHelper.speak(applicationContext, capturePrompt)
                }
            }
            override fun onError(exception: ImageCaptureException) {
                runOnUiThread {
                    val failPrompt = LLMHelper.getPrompt("capture_fail")
                    binding.tvPrompt.text = failPrompt
                    LLMHelper.speak(applicationContext, failPrompt)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        detector.close()
    }
}
