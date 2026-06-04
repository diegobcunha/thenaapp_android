package com.diegocunha.thenaapp.feature.vaccine.presentation

import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.vaccine.domain.VaccineRepository
import com.diegocunha.thenaapp.feature.vaccine.domain.model.DoseType
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineRecord
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineSchedule
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineScheduleItem
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineScheduleStatus
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class VaccineViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: VaccineRepository = mockk()
    private val recordsFlow = MutableStateFlow<List<VaccineRecord>>(emptyList())
    private val babyId = "test-baby-id"

    private lateinit var viewModel: VaccineViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repository.observeRecords(any()) } returns recordsFlow
        coEvery { repository.getSchedule(any()) } returns Resource.Error(Exception())
        coJustRun { repository.syncPendingRecords(any()) }
        viewModel = VaccineViewModel(repository = repository, babyId = babyId)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN schedule loads successfully THEN state reflects items and counts`() = runTest {
        val items = listOf(buildScheduleItem(VaccineScheduleStatus.DUE))
        coEvery { repository.getSchedule(any()) } returns Resource.Success(VaccineSchedule(items.toPersistentList()))

        viewModel = VaccineViewModel(repository = repository, babyId = babyId)

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, viewModel.state.value.scheduleItems.size)
        assertTrue(viewModel.state.value.hasUrgent)
    }

    @Test
    fun `WHEN records emitted THEN completedCount updates`() = runTest {
        recordsFlow.value = listOf(buildRecord(), buildRecord())

        assertEquals(2, viewModel.state.value.completedCount)
    }

    @Test
    fun `WHEN SelectTab THEN selectedTab changes`() = runTest {
        viewModel.sendIntent(VaccineIntent.SelectTab(VaccineTab.COMPLETED))

        assertEquals(VaccineTab.COMPLETED, viewModel.state.value.selectedTab)
    }

    @Test
    fun `WHEN RegisterVaccine THEN NavigateToRegister emitted with null pniTemplateId`() = runTest {
        viewModel.effects.test {
            viewModel.sendIntent(VaccineIntent.RegisterVaccine)

            val effect = awaitItem()
            assertTrue(effect is VaccineEffect.NavigateToRegister)
            effect as VaccineEffect.NavigateToRegister
            assertEquals(babyId, effect.babyId)
            assertEquals(null, effect.pniTemplateId)
        }
    }

    @Test
    fun `WHEN DeleteRecord fails THEN ShowError emitted`() = runTest {
        coEvery { repository.deleteRecord(any(), any()) } returns Resource.Error(Exception())

        viewModel.effects.test {
            viewModel.sendIntent(VaccineIntent.DeleteRecord("some-id"))

            assertTrue(awaitItem() is VaccineEffect.ShowError)
        }
    }

    private fun buildScheduleItem(status: VaccineScheduleStatus) = VaccineScheduleItem(
        pniTemplateId = "pni-1",
        vaccineName = "BCG",
        recommendedAgeMonths = 0,
        doseNumber = 1,
        status = status,
        administeredRecordId = null,
        administeredDate = null,
    )

    private fun buildRecord() = VaccineRecord(
        id = java.util.UUID.randomUUID().toString(),
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