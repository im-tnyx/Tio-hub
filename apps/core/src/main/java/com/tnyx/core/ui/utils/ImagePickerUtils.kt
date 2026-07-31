package com.tnyx.core.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.min

private const val AVATAR_MAX_EDGE_PX = 1024
private const val AVATAR_JPEG_QUALITY = 88

fun Context.readAvatarJpeg(uri: Uri): ByteArray? {
    val source = contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input)
    } ?: return null

    return try {
        source.toSquareJpegBytes()
    } finally {
        source.recycle()
    }
}

fun Bitmap.toSquareJpegBytes(): ByteArray? {
    return try {
        val edge = min(width, height)
        if (edge <= 0) return null

        val left = (width - edge) / 2
        val top = (height - edge) / 2
        val square = Bitmap.createBitmap(this, left, top, edge, edge)
        val outputSize = min(edge, AVATAR_MAX_EDGE_PX)
        val scaled = if (square.width == outputSize) {
            square
        } else {
            Bitmap.createScaledBitmap(square, outputSize, outputSize, true)
        }

        ByteArrayOutputStream().use { output ->
            if (!scaled.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, output)) {
                return null
            }
            output.toByteArray()
        }.also {
            if (scaled !== square) scaled.recycle()
            if (square !== this) square.recycle()
        }
    } catch (e: Exception) {
        null
    }
}
