package com.diegocunha.thenaapp.feature.baby.presentation.create

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.baby.R
import com.diegocunha.thenaapp.feature.baby.domain.create.CreateBabyRepository
import com.diegocunha.thenaapp.feature.baby.domain.create.dto.CreateBabyRequest
import com.diegocunha.thenaapp.feature.baby.domain.model.BabyGender
import com.diegocunha.thenaapp.feature.baby.domain.model.ResponsibleType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CreateBabyViewModel(
    private val createBabyRepository: CreateBabyRepository,
) : BaseViewModel<CreateBabyState, CreateBabyIntent, CreateBabyEffect>(CreateBabyState()) {

    override fun processIntent(intent: CreateBabyIntent) {
        when (intent) {
            is CreateBabyIntent.OnNameChange -> updateState {
                copy(babyName = intent.name, babyNameError = null)
            }

            is CreateBabyIntent.OnPhotoChange -> updateState {
                copy(photo = intent.photo)
            }

            is CreateBabyIntent.OnGenderChange -> updateState {
                copy(babyGender = intent.gender, babyGenderError = null)
            }

            is CreateBabyIntent.OnResponsibleTypeChange -> updateState {
                copy(responsibleType = intent.type, responsibleTypeError = null)
            }

            is CreateBabyIntent.OnBirthDateChange -> updateState {
                copy(birthDate = intent.birthDate, birthDateError = null)
            }

            is CreateBabyIntent.OnWeightChange -> updateState {
                copy(birthWeight = intent.weight, birthWeightError = null)
            }

            is CreateBabyIntent.OnHeightChange -> updateState {
                copy(birthHeight = intent.height, birthHeightError = null)
            }

            is CreateBabyIntent.OnNextPage -> advancePage()
            is CreateBabyIntent.OnPreviousPage -> updateState {
                copy(currentPage = (currentPage - 1).coerceAtLeast(0))
            }

            is CreateBabyIntent.CreateBaby -> createBaby()
        }
    }

    private fun advancePage() {
        val current = state.value
        when (current.currentPage) {
            PAGE_0 -> {
                val nameError =
                    if (current.babyName.isBlank()) {
                        R.string.create_baby_error_name_required
                    } else {
                        null
                    }
                val genderError =
                    if (current.babyGender == null) {
                        R.string.create_baby_error_gender_required
                    } else {
                        null
                    }
                val responsibleTypeError = if (current.responsibleType == null) {
                    R.string.create_baby_responsible_error
                } else {
                    null
                }
                if (nameError != null || genderError != null || responsibleTypeError != null) {
                    updateState {
                        copy(
                            babyNameError = nameError,
                            babyGenderError = genderError,
                            responsibleTypeError = responsibleTypeError
                        )
                    }
                    return
                }
                updateState { copy(currentPage = PAGE_1) }
            }

            PAGE_1 -> {
                val current2 = state.value
                val dateError = when {
                    current2.birthDate.isBlank() -> R.string.create_baby_error_date_required
                    !isValidDate(current2.birthDate) -> R.string.create_baby_error_date_invalid
                    else -> null
                }
                val weightError =
                    if (current2.birthWeight.isBlank()) R.string.create_baby_error_weight_required else null
                val heightError =
                    if (current2.birthHeight.isBlank()) R.string.create_baby_error_height_required else null
                if (dateError != null || weightError != null || heightError != null) {
                    updateState {
                        copy(
                            birthDateError = dateError,
                            birthWeightError = weightError,
                            birthHeightError = heightError,
                        )
                    }
                    return
                }
                updateState { copy(currentPage = PAGE_2) }
            }
        }
    }

    private fun createBaby() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, generalError = null) }
            val current = state.value
            val request = CreateBabyRequest(
                name = current.babyName.trim(),
                birthDate = formatDateForApi(current.birthDate),
                gender = current.babyGender ?: BabyGender.OTHER,
                photo = current.photo,
                responsibleType = current.responsibleType ?: ResponsibleType.OTHER,
            )
            when (createBabyRepository.createBaby(request)) {
                is Resource.Success -> sendEffect(CreateBabyEffect.NavigateToHome)
                is Resource.Error -> updateState {
                    copy(isLoading = false, generalError = R.string.create_baby_error_general)
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun isValidDate(digits: String): Boolean {
        if (digits.length != 8) {
            return false
        }
        val isYMD = !Locale.getDefault().language.startsWith("pt")
        val pattern = if (isYMD) "yyyyMMdd" else "ddMMyyyy"
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.isLenient = false
            sdf.parse(digits) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun formatDateForApi(digits: String): String {
        if (digits.length != 8) {
            return digits
        }
        val isYMD = !Locale.getDefault().language.startsWith("pt")
        return if (isYMD) {
            "${digits.substring(0, 4)}-${digits.substring(4, 6)}-${digits.substring(6, 8)}"
        } else {
            "${digits.substring(4, 8)}-${digits.substring(2, 4)}-${digits.substring(0, 2)}"
        }
    }

    private companion object {
        private const val PAGE_0 = 0
        private const val PAGE_1 = 1
        private const val PAGE_2 = 2

    }
}
