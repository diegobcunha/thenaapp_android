package com.diegocunha.thenaapp.datasource.network.service

import com.diegocunha.thenaapp.datasource.network.model.vaccine.PniTemplateResponse
import com.diegocunha.thenaapp.datasource.network.model.vaccine.RegisterVaccineRequest
import com.diegocunha.thenaapp.datasource.network.model.vaccine.VaccineRecordResponse
import com.diegocunha.thenaapp.datasource.network.model.vaccine.VaccineRecordSummaryResponse
import com.diegocunha.thenaapp.datasource.network.model.vaccine.VaccineScheduleResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VaccineApiService {

    @GET("/v1/baby/{babyId}/vaccines/schedule")
    suspend fun getSchedule(
        @Path("babyId") babyId: String,
    ): VaccineScheduleResponse

    @GET("/v1/vaccines/templates")
    suspend fun getTemplates(): List<PniTemplateResponse>

    @GET("/v1/baby/{babyId}/vaccines/records")
    suspend fun listRecords(
        @Path("babyId") babyId: String,
    ): List<VaccineRecordSummaryResponse>

    @POST("/v1/baby/{babyId}/vaccines/records")
    suspend fun registerVaccine(
        @Path("babyId") babyId: String,
        @Body request: RegisterVaccineRequest,
    ): VaccineRecordResponse

    @DELETE("/v1/baby/{babyId}/vaccines/records/{recordId}")
    suspend fun deleteRecord(
        @Path("babyId") babyId: String,
        @Path("recordId") recordId: String,
    )
}