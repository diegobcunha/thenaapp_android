package com.diegocunha.thenaapp.datasource.repository.userprofile

sealed class ProfileStatus {
    object Complete : ProfileStatus()
    data class MissingName(val hasBaby: Boolean) : ProfileStatus()
    object MissingBaby : ProfileStatus()
}
