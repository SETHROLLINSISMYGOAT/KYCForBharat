package com.sid.kycforbharat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.sid.kycforbharat.databinding.ActivityUploadQueueBinding

class UploadQueueActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadQueueBinding
    private val UNIQUE_WORK = "kyc_upload_unique_work"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvStatus.text = "Queued. Will auto-retry when online."

        binding.btnRetryNow.setOnClickListener { enqueueUpload() }
        binding.btnGoHome.setOnClickListener { startActivity(Intent(this, WelcomeActivity::class.java)); finishAffinity() }

        enqueueUpload()
    }

    private fun enqueueUpload() {
        val work = OneTimeWorkRequestBuilder<UploadWorker>().build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(UNIQUE_WORK, ExistingWorkPolicy.KEEP, work)

        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(work.id).observe(this, Observer { info: WorkInfo? ->
            if (info == null) return@Observer
            when (info.state) {
                WorkInfo.State.ENQUEUED -> { binding.tvStatus.text = "Queued. Waiting for network..." ; binding.pbUpload.isIndeterminate = true }
                WorkInfo.State.RUNNING -> { binding.tvStatus.text = "Uploading..." ; binding.pbUpload.isIndeterminate = false ; binding.pbUpload.progress = 50 }
                WorkInfo.State.SUCCEEDED -> { binding.tvStatus.text = "Upload successful"; startActivity(Intent(this, ResultActivity::class.java)); finish() }
                WorkInfo.State.FAILED -> { binding.tvStatus.text = "Upload failed. Will retry." }
                else -> { binding.tvStatus.text = "${info.state}" }
            }
        })
    }
}
