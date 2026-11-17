package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun uriToCompressedBase64(context: Context, uri: Uri, maxDim: Int = 260): String? {
        return try {
            val stream = context.contentResolver.openInputStream(uri) ?: return null
            val original = stream.use { BitmapFactory.decodeStream(it) } ?: return null
            val scale = minOf(
                maxDim.toFloat() / original.width.coerceAtLeast(1),
                maxDim.toFloat() / original.height.coerceAtLeast(1),
                1f
            )
            val targetW = (original.width * scale).toInt().coerceAtLeast(1)
            val targetH = (original.height * scale).toInt().coerceAtLeast(1)
            val resized = Bitmap.createScaledBitmap(original, targetW, targetH, true)
            val out = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 68, out)
            val bytes = out.toByteArray()
            if (bytes.size > 100_000) {
                val outLow = ByteArrayOutputStream()
                resized.compress(Bitmap.CompressFormat.JPEG, 45, outLow)
                Base64.encodeToString(outLow.toByteArray(), Base64.NO_WRAP)
            } else {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun decodeBase64Bitmap(base64: String): Bitmap? {
        if (base64.isBlank()) return null
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }
}

fun Int.toBengaliDigits(): String = toString().toBengaliDigits()

fun Long.toBengaliDigits(): String = toString().toBengaliDigits()

fun Double.toBengaliPrice(): String {
    val rounded = kotlin.math.round(this).toLong()
    return "৳ ${rounded.toBengaliDigits()}"
}

fun String.toBengaliDigits(): String {
    val map = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return buildString(length) {
        for (ch in this@toBengaliDigits) {
            if (ch in '0'..'9') append(map[ch - '0']) else append(ch)
        }
    }
}
