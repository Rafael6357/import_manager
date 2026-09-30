package com.example.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageCompressor {
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            
            var quality = 90
            var byteArray: ByteArray
            val outputStream = ByteArrayOutputStream()
            
            // Scaled down resolution to ensure fast compression and reasonable DB storage size
            var resizedBitmap = originalBitmap
            if (originalBitmap.width > 800 || originalBitmap.height > 800) {
                val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                val targetWidth = if (aspectRatio > 1) 800 else (800 * aspectRatio).toInt()
                val targetHeight = if (aspectRatio > 1) (800 / aspectRatio).toInt() else 800
                resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            }
            
            do {
                outputStream.reset()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                byteArray = outputStream.toByteArray()
                quality -= 10
            } while (byteArray.size > 450 * 1024 && quality > 10) // Keep size < 450KB
            
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
