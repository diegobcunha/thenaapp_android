package com.diegocunha.thenaapp.feature.baby.repository.create

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ImageCompressor(
    private val context: Context,
    private val dispatchersProvider: DispatchersProvider,
) {

    suspend fun compress(uri: Uri): ByteArray = withContext(dispatchersProvider.io()) {
        val original = decodeUri(uri)
        val resized = resize(original)
        return@withContext compressToJpeg(resized)
    }

    private suspend fun decodeUri(uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Not possible open image")

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        with(options) {
            inSampleSize = calculateInSampleSize(options)
            inJustDecodeBounds = false
        }

        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Not possible open image")

        return BitmapFactory.decodeStream(stream, null, options)
            ?: throw IllegalArgumentException("Not possible decode image")
    }

    private suspend fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > MAX_HEIGHT || width > MAX_WIDTH) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= MAX_HEIGHT
                && halfWidth / inSampleSize >= MAX_WIDTH
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private suspend fun resize(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) return bitmap

        val ratio = minOf(MAX_WIDTH.toFloat() / width, MAX_HEIGHT.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return bitmap.scale(newWidth, newHeight)
    }

    private suspend fun compressToJpeg(bitmap: Bitmap): ByteArray {
        var quality = QUALITY
        var result: ByteArray

        do {
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            result = output.toByteArray()
            quality -= 10
        } while (result.size > MAX_SIZE_BYTES && quality > 10)

        return result
    }

    companion object {
        private const val MAX_WIDTH = 1024
        private const val MAX_HEIGHT = 1024
        private const val QUALITY = 80
        private const val MAX_SIZE_BYTES = 500_000
    }
}
