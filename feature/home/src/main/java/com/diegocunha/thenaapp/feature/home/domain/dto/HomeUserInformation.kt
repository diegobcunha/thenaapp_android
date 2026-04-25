package com.diegocunha.thenaapp.feature.home.domain.dto

import java.math.BigDecimal

data class HomeUserInformation(
    val userName: String,
    val babyInformation: HomeBabyInformation,
)

data class HomeBabyInformation(
    val babyName: String,
    val babyBirthDate: String,
    val babyWeight: BigDecimal,
    val babyHeight: BigDecimal,
    val babyPhotoUrl: String? = null,
)
