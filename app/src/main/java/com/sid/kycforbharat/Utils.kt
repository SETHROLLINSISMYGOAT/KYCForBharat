package com.sid.kycforbharat


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

object Utils {
    const val REQ_CAMERA = 100

    fun ensureCameraPermission(activity: Activity): Boolean {
        val ok = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!ok) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA)
        }
        return ok
    }

    fun saveBitmapToFile(cacheDir: File, bmp: Bitmap, prefix: String = "img"): String {
        val f = File(cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(f).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        return f.absolutePath
    }
}
