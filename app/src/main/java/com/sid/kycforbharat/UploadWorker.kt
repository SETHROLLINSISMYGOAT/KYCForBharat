package com.sid.kycforbharat

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import kotlin.random.Random
import java.io.File

class UploadWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {

        val method = KycData.method
        val uploads = mutableListOf<String?>()
        when (method) {
            "PAN" -> uploads.add(KycData.frontPath)
            "Aadhaar", "DL", "VoterID" -> { uploads.add(KycData.frontPath); uploads.add(KycData.backPath) }
            "DigiLocker" -> uploads.add("digilocker_metadata")
            else -> { uploads.add(KycData.frontPath); uploads.add(KycData.backPath) }
        }


        for (path in uploads) {
            if (path == null) continue

            delay(1000)
            val ok = Random.nextInt(100) < 90
            if (!ok) {
                return Result.retry()
            }
        }


        try {
            listOf(KycData.frontPath, KycData.backPath, KycData.facePath).forEach { p ->
                p?.let {
                    try { File(it).delete() } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}

        return Result.success()
    }
}
