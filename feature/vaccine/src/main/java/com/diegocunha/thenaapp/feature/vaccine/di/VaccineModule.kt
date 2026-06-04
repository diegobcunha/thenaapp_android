package com.diegocunha.thenaapp.feature.vaccine.di

import com.diegocunha.thenaapp.feature.vaccine.domain.VaccineRepository
import com.diegocunha.thenaapp.feature.vaccine.presentation.RegisterVaccineViewModel
import com.diegocunha.thenaapp.feature.vaccine.presentation.VaccineViewModel
import com.diegocunha.thenaapp.feature.vaccine.repository.VaccineRepositoryImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vaccineModule = module {

    single<VaccineRepository> {
        VaccineRepositoryImpl(
            vaccineApiService = get(),
            vaccineRecordDao = get(),
            dispatchersProvider = get(),
        )
    }

    viewModel { (babyId: String) ->
        VaccineViewModel(repository = get(), babyId = babyId)
    }

    viewModel { (babyId: String, pniTemplateId: String?, vaccineName: String?) ->
        RegisterVaccineViewModel(
            repository = get(),
            babyId = babyId,
            pniTemplateId = pniTemplateId,
            vaccineName = vaccineName,
        )
    }
}