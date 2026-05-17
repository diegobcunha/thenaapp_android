package com.diegocunha.thenaapp.feature.home.domain

import java.util.Calendar

class CalculateBabyAgeUseCase {

    operator fun invoke(birthDateString: String): BabyAgeResult? = runCatching {
        val parts = birthDateString.split("-")
        val birth = Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }
        val now = Calendar.getInstance()

        var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        var months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)

        if (months < 0) {
            years--
            months += 12
        }

        BabyAgeResult(
            totalMonths = years * 12 + months,
            years = years,
            remainderMonths = months,
        )
    }.getOrNull()
}
