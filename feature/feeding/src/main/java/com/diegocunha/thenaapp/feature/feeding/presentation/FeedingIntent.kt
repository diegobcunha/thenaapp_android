package com.diegocunha.thenaapp.feature.feeding.presentation

import com.diegocunha.thenaapp.core.mvi.MviIntent
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast

sealed interface FeedingIntent : MviIntent {
    data object SelectBreastfeeding : FeedingIntent
    data object SelectBottle : FeedingIntent

    // Single tap intent — ViewModel decides start / pause / resume / switch
    data class TapBreast(val breast: Breast) : FeedingIntent
    data object StopSession : FeedingIntent

    data class UpdateBottleMl(val ml: String) : FeedingIntent
    data class SelectBottleType(val type: BottleType) : FeedingIntent
    data object SaveBottleFeeding : FeedingIntent

    data object Tick : FeedingIntent
}
