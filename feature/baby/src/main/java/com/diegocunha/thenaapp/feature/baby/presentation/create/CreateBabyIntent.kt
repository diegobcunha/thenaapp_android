package com.diegocunha.thenaapp.feature.baby.presentation.create

import android.net.Uri
import com.diegocunha.thenaapp.core.mvi.MviIntent
import com.diegocunha.thenaapp.feature.baby.domain.model.BabyGender
import com.diegocunha.thenaapp.feature.baby.domain.model.ResponsibleType

sealed interface CreateBabyIntent : MviIntent {
    data class OnNameChange(val name: String) : CreateBabyIntent
    data class OnPhotoChange(val photo: Uri?) : CreateBabyIntent
    data class OnGenderChange(val gender: BabyGender) : CreateBabyIntent

    data class OnResponsibleTypeChange(val type: ResponsibleType): CreateBabyIntent
    data class OnBirthDateChange(val birthDate: String) : CreateBabyIntent
    data class OnWeightChange(val weight: String) : CreateBabyIntent
    data class OnHeightChange(val height: String) : CreateBabyIntent
    object OnNextPage : CreateBabyIntent
    object OnPreviousPage : CreateBabyIntent
    object CreateBaby : CreateBabyIntent
}
