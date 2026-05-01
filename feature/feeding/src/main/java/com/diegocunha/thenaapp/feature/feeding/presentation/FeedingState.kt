package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviState
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType

@Immutable
data class FeedingState(
    val sessionId: String? = null,
    val feedingType: FeedingType? = null,
    val activeBreast: Breast? = null,
    val leftElapsedSeconds: Long = 0L,
    val rightElapsedSeconds: Long = 0L,
    val totalElapsedSeconds: Long = 0L,
    val bottleMl: String = "",
    val bottleType: BottleType? = null,
    val isLoading: Boolean = false,
) : MviState
