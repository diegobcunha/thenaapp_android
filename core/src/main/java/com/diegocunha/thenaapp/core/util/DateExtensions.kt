package com.diegocunha.thenaapp.core.util

import java.text.SimpleDateFormat
import java.util.Locale

fun String.toDisplayDate(): String = runCatching {
    val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
    val outputFmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    outputFmt.format(inputFmt.parse(this)!!)
}.getOrDefault(this)