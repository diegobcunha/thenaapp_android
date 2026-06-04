package com.diegocunha.thenaapp.feature.vaccine.presentation

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.vaccine.domain.VaccineRepository
import com.diegocunha.thenaapp.feature.vaccine.domain.model.DoseType
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineRecord
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterVaccineViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: VaccineRepository = mockk()
    private val babyId = "test-baby-id"

    private lateinit var viewModel: RegisterVaccineViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = RegisterVaccineViewModel(
            repository = repository,
            babyId = babyId,
            pniTemplateId = null,
            vaccineName = null,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN vaccineName pre-filled THEN field is locked`() {
        val vm = RegisterVaccineViewModel(
            repository = repository,
            babyId = babyId,
            pniTemplateId = "pni-1",
            vaccineName = "BCG",
        )
        assertEquals("BCG", vm.state.value.vaccineName)
        assertTrue(vm.state.value.vaccineNameLocked)
    }

    @Test
    fun `WHEN UpdateVaccineName THEN state reflects new name`() = runTest {
        viewModel.sendIntent(RegisterVaccineIntent.UpdateVaccineName("Polio"))

        assertEquals("Polio", viewModel.state.value.vaccineName)
        assertFalse(viewModel.state.value.vaccineNameError)
    }

    @Test
    fun `WHEN Submit with empty name THEN vaccineNameError set`() = runTest {
        viewModel.sendIntent(RegisterVaccineIntent.Submit)

        assertTrue(viewModel.state.value.vaccineNameError)
    }

    @Test
    fun `WHEN Submit with short date THEN dateError set`() = runTest {
        viewModel.sendIntent(RegisterVaccineIntent.UpdateVaccineName("BCG"))
        viewModel.sendIntent(RegisterVaccineIntent.UpdateDate("202601"))
        viewModel.sendIntent(RegisterVaccineIntent.Submit)

        assertTrue(viewModel.state.value.dateError)
    }

    @Test
    fun `WHEN Submit succeeds THEN NavigateBack emitted`() = runTest {
        coEvery { repository.registerVaccine(any()) } returns Resource.Success(buildRecord())

        viewModel.effects.test {
            viewModel.sendIntent(RegisterVaccineIntent.UpdateVaccineName("BCG"))
            viewModel.sendIntent(RegisterVaccineIntent.UpdateDate("20260101"))
            viewModel.sendIntent(RegisterVaccineIntent.Submit)

            assertEquals(RegisterVaccineEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `WHEN Submit fails THEN ShowError emitted and isSubmitting reset`() = runTest {
        coEvery { repository.registerVaccine(any()) } returns Resource.Error(Exception())

        viewModel.effects.test {
            viewModel.sendIntent(RegisterVaccineIntent.UpdateVaccineName("BCG"))
            viewModel.sendIntent(RegisterVaccineIntent.UpdateDate("20260101"))
            viewModel.sendIntent(RegisterVaccineIntent.Submit)

            assertTrue(awaitItem() is RegisterVaccineEffect.ShowError)
        }
        assertFalse(viewModel.state.value.isSubmitting)
    }

    private fun buildRecord() = VaccineRecord(
        id = "record-1",
        babyId = babyId,
        vaccineName = "BCG",
        administeredDate = "2026-01-01",
        doseType = DoseType.PRIMARY,
        injectionSite = null,
        batchNumber = null,
        healthcareProvider = null,
        reactionSeverity = null,
        reactionNotes = null,
        pniTemplateId = null,
        pendingSync = false,
    )
}