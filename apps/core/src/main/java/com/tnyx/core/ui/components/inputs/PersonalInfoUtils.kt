package com.tnyx.core.ui.components.inputs

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

fun String.toDobMillis(): Long? {
    val raw = trim()
    if (raw.isBlank()) return null
    raw.toLongOrNull()?.let { return it }
    return runCatching { Instant.parse(raw).toEpochMilli() }.getOrNull()
        ?: runCatching {
            java.time.LocalDate.parse(raw)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
}

fun Long.toDisplayDate(): String {
    if (this == 0L) return ""
    return runCatching {
        Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrDefault("")
}

fun formatFeetInches(f: String, i: String) = "${f.ifBlank { "0" }}' ${i.ifBlank { "0" }}\""

fun feetInchesToCm(f: String, i: String): Float? {
    val ft = f.toIntOrNull() ?: return null
    val inch = i.toIntOrNull() ?: 0
    return ((ft * 12) + inch) * 2.54f
}

fun cmToFeetInches(cm: Float): Pair<Int, Int> {
    val totalInches = (cm / 2.54f).roundToInt()
    return (totalInches / 12) to (totalInches % 12)
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
