package com.diegocunha.thenaapp.feature.baby.presentation.create

import android.net.Uri
import androidx.annotation.StringRes
import com.diegocunha.thenaapp.core.mvi.MviState
import com.diegocunha.thenaapp.feature.baby.domain.model.BabyGender
import com.diegocunha.thenaapp.feature.baby.domain.model.ResponsibleType

data class CreateBabyState(
    val babyName: String = "",
    val babyGender: BabyGender? = null,
    val responsibleType: ResponsibleType? = null,
    val photo: Uri? = null,
    val birthDate: String = "",
    val birthWeight: String = "",
    val birthHeight: String = "",
    val currentPage: Int = 0,
    val isLoading: Boolean = false,
    @StringRes val babyNameError: Int? = null,
    @StringRes val babyGenderError: Int? = null,
    @StringRes val responsibleTypeError: Int? = null,
    @StringRes val birthDateError: Int? = null,
    @StringRes val birthWeightError: Int? = null,
    @StringRes val birthHeightError: Int? = null,
    @StringRes val generalError: Int? = null,
) : MviState
