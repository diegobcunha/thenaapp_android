package com.diegocunha.thenaapp.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccine_records")
data class VaccineRecordEntity(
    @PrimaryKey val id: String,
    val babyId: String,
    val vaccineName: String,
    val administeredDate: String,
    val doseType: String,
    val injectionSite: String?,
    val batchNumber: String?,
    val healthcareProvider: String?,
    val reactionSeverity: String?,
    val reactionNotes: String?,
    val pniTemplateId: String?,
    val serverId: String?,
    val pendingSync: Boolean = true,
)