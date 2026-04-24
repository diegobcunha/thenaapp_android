package com.diegocunha.thenaapp.feature.baby.presentation.create.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Locale

/**
 * Formats raw digit input (max 8 chars) as a locale-aware date mask.
 * Portuguese locale → DD/MM/YYYY; all others → YYYY-MM-DD.
 */
class DateMaskVisualTransformation : VisualTransformation {

    private val isYMD = !Locale.getDefault().language.startsWith("pt")

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            digits.forEachIndexed { i, c ->
                if (isYMD) {
                    if (i == 4 || i == 6) append('-')
                } else {
                    if (i == 2 || i == 4) append('/')
                }
                append(c)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping(digits.length, formatted.length))
    }

    private fun offsetMapping(rawLen: Int, formattedLen: Int) = if (isYMD) {
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var adj = offset
                if (offset >= 4) adj++
                if (offset >= 6) adj++
                return adj.coerceIn(0, formattedLen)
            }
            override fun transformedToOriginal(offset: Int): Int {
                var adj = offset
                if (offset > 4) adj--
                if (offset > 7) adj--
                return adj.coerceIn(0, rawLen)
            }
        }
    } else {
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var adj = offset
                if (offset >= 2) adj++
                if (offset >= 4) adj++
                return adj.coerceIn(0, formattedLen)
            }
            override fun transformedToOriginal(offset: Int): Int {
                var adj = offset
                if (offset > 2) adj--
                if (offset > 5) adj--
                return adj.coerceIn(0, rawLen)
            }
        }
    }
}
