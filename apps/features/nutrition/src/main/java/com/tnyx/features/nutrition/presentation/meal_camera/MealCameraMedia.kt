package com.tnyx.features.nutrition.presentation.meal_camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class CapturedMealPhoto(
    val path: String,
    val mimeType: String,
)

internal fun createMealCameraFile(context: Context): File {
    val directory = File(context.cacheDir, CAMERA_CACHE_DIRECTORY).apply { mkdirs() }
    return File.createTempFile("meal-capture-", ".jpg", directory)
}

internal suspend fun copyGalleryPhotoToCache(
    context: Context,
    uri: Uri,
): CapturedMealPhoto = withContext(Dispatchers.IO) {
    val mimeType = context.contentResolver.getType(uri)?.lowercase()
        ?: error("Selected file type is unavailable.")
    require(mimeType in SUPPORTED_PHOTO_MIME_TYPES) {
        "Select a JPEG, PNG, or WebP image."
    }
    val extension = when (mimeType) {
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        else -> ".jpg"
    }
    val directory = File(context.cacheDir, CAMERA_CACHE_DIRECTORY).apply { mkdirs() }
    val target = File.createTempFile("meal-gallery-", extension, directory)
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(target).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var totalBytes = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    totalBytes += count
                    require(totalBytes <= MAX_ORIGINAL_PHOTO_BYTES) {
                        "Meal photo is too large. Maximum size is 10 MB."
                    }
                    output.write(buffer, 0, count)
                }
            }
        } ?: error("Meal photo could not be opened.")
        CapturedMealPhoto(path = target.absolutePath, mimeType = mimeType)
    } catch (error: Throwable) {
        target.delete()
        throw error
    }
}

internal suspend fun prepareRecognitionImage(path: String): ByteArray = withContext(Dispatchers.Default) {
    val sourceBytes = File(path).readBytes()
    require(sourceBytes.isNotEmpty()) { "Meal photo is empty." }
    val decoded = BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.size)
        ?: error("Meal photo could not be decoded.")
    val oriented = decoded.applyExifOrientation(sourceBytes)
    val squareEdge = minOf(oriented.width, oriented.height)
    val square = if (oriented.width != oriented.height) {
        Bitmap.createBitmap(
            oriented,
            (oriented.width - squareEdge) / 2,
            (oriented.height - squareEdge) / 2,
            squareEdge,
            squareEdge,
        )
    } else {
        oriented
    }
    val resized = if (square.width != RECOGNITION_IMAGE_EDGE) {
        Bitmap.createScaledBitmap(
            square,
            RECOGNITION_IMAGE_EDGE,
            RECOGNITION_IMAGE_EDGE,
            true,
        )
    } else {
        square
    }

    val bytes = ByteArrayOutputStream().use { output ->
        check(resized.compress(Bitmap.CompressFormat.JPEG, RECOGNITION_JPEG_QUALITY, output)) {
            "Meal photo could not be prepared."
        }
        output.toByteArray()
    }
    if (resized !== square) resized.recycle()
    if (square !== oriented) square.recycle()
    if (oriented !== decoded) oriented.recycle()
    decoded.recycle()
    require(bytes.size <= MAX_RECOGNITION_IMAGE_BYTES) {
        "Meal photo could not be reduced for analysis."
    }
    bytes
}

private fun Bitmap.applyExifOrientation(sourceBytes: ByteArray): Bitmap {
    val orientation = runCatching {
        ExifInterface(ByteArrayInputStream(sourceBytes)).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                setRotate(180f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                setRotate(90f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                setRotate(-90f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
        }
    }
    if (matrix.isIdentity) return this
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private const val CAMERA_CACHE_DIRECTORY = "meal-camera"
private const val MAX_ORIGINAL_PHOTO_BYTES = 10 * 1024 * 1024
private const val MAX_RECOGNITION_IMAGE_BYTES = 740_000
private const val RECOGNITION_IMAGE_EDGE = 512
private const val RECOGNITION_JPEG_QUALITY = 84
private val SUPPORTED_PHOTO_MIME_TYPES = setOf(
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/webp",
)
