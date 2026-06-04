package com.diegocunha.thenaapp.feature.vaccine.domain.model

enum class DoseType(val value: String) {
    PRIMARY("PRIMARY"),
    BOOSTER("BOOSTER"),
    ANNUAL("ANNUAL"),
    CATCH_UP("CATCH_UP"),
}