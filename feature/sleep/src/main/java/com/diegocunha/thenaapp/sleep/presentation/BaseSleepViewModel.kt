package com.diegocunha.thenaapp.sleep.presentation

import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.mvi.MviEffect
import com.diegocunha.thenaapp.core.mvi.MviIntent
import com.diegocunha.thenaapp.core.mvi.MviState
import com.diegocunha.thenaapp.sleep.domain.model.SleepDailyStats
import com.diegocunha.thenaapp.sleep.domain.model.SleepSession
import com.diegocunha.thenaapp.sleep.domain.model.SleepType
import com.diegocunha.thenaapp.sleep.domain.model.SleepWeeklyStats
import com.diegocunha.thenaapp.sleep.presentation.model.SleepDailyStatsUi
import com.diegocunha.thenaapp.sleep.presentation.model.SleepSessionUi
import com.diegocunha.thenaapp.sleep.presentation.model.SleepWeeklyStatsUi
import kotlinx.collections.immutable.toPersistentList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class BaseSleepViewModel<State : MviState, Intent : MviIntent, Effect : MviEffect>(
    initialState: State
) : BaseViewModel<State, Intent, Effect>(initialState) {

    protected fun SleepSession.toUi(): SleepSessionUi = SleepSessionUi(
        id = id,
        typeIcon = sleepType.icon(),
        typeName = sleepType.displayName(),
        timeRange = "${startTimeMs.toTimeString()} → ${endTimeMs?.toTimeString() ?: "—"}",
        durationDisplay = durationMinutes?.let { min ->
            val h = min / 60
            val m = min % 60
            if (h > 0) "${h}h ${m}m" else "${m}m"
        } ?: "Active",
    )

    protected fun SleepDailyStats.toUi(): SleepDailyStatsUi = SleepDailyStatsUi(
        date = date,
        progress = if (goalMinutes > 0) (totalSleepMinutes.toFloat() / goalMinutes).coerceIn(
            0f,
            1f
        ) else 0f,
        totalDisplay = "${totalSleepMinutes / 60}h ${totalSleepMinutes % 60}m",
        goalDisplay = "of ${goalMinutes / 60}h",
        sessionCount = sessionCount,
        efficiencyDisplay = "${(efficiency * 100).toInt()}%",
        totalSleepMinutes = totalSleepMinutes,
        insight = insight,
    )

    protected fun SleepWeeklyStats.toUi(): SleepWeeklyStatsUi = SleepWeeklyStatsUi(
        days = days.map { it.toUi() }.toPersistentList(),
        weeklyAvgDisplay = weeklyAvgMinutes?.let { "${weeklyAvgMinutes / 60}h ${weeklyAvgMinutes % 60}m" },
        trend = trend,
    )

    private fun Long.toTimeString(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))

    private fun SleepType.displayName() = when (this) {
        SleepType.NAP -> "Nap"
        SleepType.NIGHT_SLEEP -> "Night Sleep"
        SleepType.EARLY_MORNING -> "Early Morning"
        SleepType.CATNAP -> "Catnap"
        SleepType.CONTACT_NAP -> "Contact Nap"
        SleepType.CAR_NAP -> "Car Nap"
    }

    private fun SleepType.icon(): String = name
}
