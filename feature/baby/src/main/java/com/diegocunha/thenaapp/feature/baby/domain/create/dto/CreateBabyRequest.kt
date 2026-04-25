package com.diegocunha.thenaapp.feature.baby.domain.create.dto

import android.net.Uri
import com.diegocunha.thenaapp.feature.baby.domain.model.BabyGender
import com.diegocunha.thenaapp.feature.baby.domain.model.ResponsibleType

data class CreateBabyRequest(
    val name: String,
    val birthDate: String,
    val gender: BabyGender,
    val responsibleType: ResponsibleType,
    val photo: Uri?,
    val weight: String,
    val height: String,
)
