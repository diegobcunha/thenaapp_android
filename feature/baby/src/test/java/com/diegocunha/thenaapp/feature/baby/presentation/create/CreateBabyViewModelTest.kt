package com.diegocunha.thenaapp.feature.baby.presentation.create

import android.net.Uri
import app.cash.turbine.test
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.baby.domain.create.CreateBabyRepository
import com.diegocunha.thenaapp.feature.baby.domain.model.BabyGender
import com.diegocunha.thenaapp.feature.baby.domain.model.ResponsibleType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateBabyViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val createBabyRepository: CreateBabyRepository = mockk()
    private lateinit var viewModel: CreateBabyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CreateBabyViewModel(createBabyRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN OnNameChange is sent THEN name is updated and nameError is cleared`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnNameChange("Luna"))

        val state = viewModel.state.value
        assertEquals("Luna", state.babyName)
        assertNull(state.babyNameError)
    }

    @Test
    fun `WHEN OnGenderChange is sent THEN gender is updated and genderError is cleared`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnGenderChange(BabyGender.GIRL))

        val state = viewModel.state.value
        assertEquals(BabyGender.GIRL, state.babyGender)
        assertNull(state.babyGenderError)
    }

    @Test
    fun `WHEN OnResponsibleTypeChange is sent THEN responsibleType is updated and error is cleared`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnResponsibleTypeChange(ResponsibleType.MOTHER))

        val state = viewModel.state.value
        assertEquals(ResponsibleType.MOTHER, state.responsibleType)
        assertNull(state.responsibleTypeError)
    }

    @Test
    fun `WHEN OnBirthDateChange is sent THEN birthDate is updated and error is cleared`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnBirthDateChange("12042024"))

        val state = viewModel.state.value
        assertEquals("12042024", state.birthDate)
        assertNull(state.birthDateError)
    }

    @Test
    fun `WHEN OnWeightChange is sent THEN birthWeight is updated and error is cleared`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnWeightChange("3.4"))

        val state = viewModel.state.value
        assertEquals("3.4", state.birthWeight)
        assertNull(state.birthWeightError)
    }

    @Test
    fun `WHEN OnHeightChange is sent THEN birthHeight is updated and error is cleared`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnHeightChange("50"))

        val state = viewModel.state.value
        assertEquals("50", state.birthHeight)
        assertNull(state.birthHeightError)
    }

    @Test
    fun `WHEN OnPhotoChange is sent THEN photo is updated`() = runTest {
        val mockUri: Uri = mockk()
        viewModel.sendIntent(CreateBabyIntent.OnPhotoChange(mockUri))

        assertEquals(mockUri, viewModel.state.value.photo)
    }

    @Test
    fun `WHEN OnPreviousPage sent on page 0 THEN currentPage stays at 0`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnPreviousPage)

        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `WHEN OnPreviousPage sent on page 1 THEN currentPage goes to 0`() = runTest {
        fillPage0()
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)
        viewModel.sendIntent(CreateBabyIntent.OnPreviousPage)

        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `WHEN OnNextPage on page 0 with all valid fields THEN advances to page 1`() = runTest {
        fillPage0()
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        val state = viewModel.state.value
        assertEquals(1, state.currentPage)
        assertNull(state.babyNameError)
        assertNull(state.babyGenderError)
        assertNull(state.responsibleTypeError)
    }

    @Test
    fun `WHEN OnNextPage on page 0 with blank name THEN nameError is set and stays on page 0`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnGenderChange(BabyGender.BOY))
        viewModel.sendIntent(CreateBabyIntent.OnResponsibleTypeChange(ResponsibleType.FATHER))
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        val state = viewModel.state.value
        assertNotNull(state.babyNameError)
        assertEquals(0, state.currentPage)
        coVerify(exactly = 0) { createBabyRepository.createBaby(any()) }
    }

    @Test
    fun `WHEN OnNextPage on page 0 with null gender THEN genderError is set and stays on page 0`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnNameChange("Luna"))
        viewModel.sendIntent(CreateBabyIntent.OnResponsibleTypeChange(ResponsibleType.MOTHER))
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        val state = viewModel.state.value
        assertNotNull(state.babyGenderError)
        assertEquals(0, state.currentPage)
    }

    @Test
    fun `WHEN OnNextPage on page 0 with null responsibleType THEN responsibleTypeError is set and stays on page 0`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnNameChange("Luna"))
        viewModel.sendIntent(CreateBabyIntent.OnGenderChange(BabyGender.GIRL))
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        val state = viewModel.state.value
        assertNotNull(state.responsibleTypeError)
        assertEquals(0, state.currentPage)
    }

    @Test
    fun `WHEN OnNextPage on page 0 with all fields missing THEN all three errors are set`() = runTest {
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        val state = viewModel.state.value
        assertNotNull(state.babyNameError)
        assertNotNull(state.babyGenderError)
        assertNotNull(state.responsibleTypeError)
        assertEquals(0, state.currentPage)
    }

    @Test
    fun `WHEN OnNextPage on page 1 with valid birth details THEN advances to page 2`() = runTest {
        goToPage1()
        fillPage1()
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        assertEquals(2, viewModel.state.value.currentPage)
    }

    @Test
    fun `WHEN OnNextPage on page 1 with blank date THEN birthDateError is set and stays on page 1`() = runTest {
        goToPage1()
        viewModel.sendIntent(CreateBabyIntent.OnWeightChange("3.4"))
        viewModel.sendIntent(CreateBabyIntent.OnHeightChange("50"))
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        val state = viewModel.state.value
        assertNotNull(state.birthDateError)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `WHEN OnNextPage on page 1 with too-short date THEN birthDateError is set`() = runTest {
        goToPage1()
        viewModel.sendIntent(CreateBabyIntent.OnBirthDateChange("12042"))
        viewModel.sendIntent(CreateBabyIntent.OnWeightChange("3.4"))
        viewModel.sendIntent(CreateBabyIntent.OnHeightChange("50"))
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        assertNotNull(viewModel.state.value.birthDateError)
        assertEquals(1, viewModel.state.value.currentPage)
    }

    @Test
    fun `WHEN OnNextPage on page 1 with invalid calendar date THEN birthDateError is set`() = runTest {
        goToPage1()
        viewModel.sendIntent(CreateBabyIntent.OnBirthDateChange("99992099"))
        viewModel.sendIntent(CreateBabyIntent.OnWeightChange("3.4"))
        viewModel.sendIntent(CreateBabyIntent.OnHeightChange("50"))
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        assertNotNull(viewModel.state.value.birthDateError)
        assertEquals(1, viewModel.state.value.currentPage)
    }

    @Test
    fun `WHEN OnNextPage on page 1 with blank weight THEN birthWeightError is set and stays on page 1`() = runTest {
        goToPage1()
        viewModel.sendIntent(CreateBabyIntent.OnBirthDateChange("12042024"))
        viewModel.sendIntent(CreateBabyIntent.OnHeightChange("50"))
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        val state = viewModel.state.value
        assertNotNull(state.birthWeightError)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `WHEN OnNextPage on page 1 with blank height THEN birthHeightError is set and stays on page 1`() = runTest {
        goToPage1()
        viewModel.sendIntent(CreateBabyIntent.OnBirthDateChange("12042024"))
        viewModel.sendIntent(CreateBabyIntent.OnWeightChange("3.4"))
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)

        val state = viewModel.state.value
        assertNotNull(state.birthHeightError)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `WHEN CreateBaby succeeds THEN NavigateToHome effect is emitted`() = runTest {
        coEvery { createBabyRepository.createBaby(any()) } returns Resource.Success(Unit)

        viewModel.effects.test {
            goToPage2()
            viewModel.sendIntent(CreateBabyIntent.CreateBaby)

            assertEquals(CreateBabyEffect.NavigateToHome, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN CreateBaby fails THEN generalError is set and isLoading is false`() = runTest {
        coEvery { createBabyRepository.createBaby(any()) } returns Resource.Error(Exception("Server error"))

        goToPage2()
        viewModel.sendIntent(CreateBabyIntent.CreateBaby)

        val state = viewModel.state.value
        assertNotNull(state.generalError)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `WHEN CreateBaby is in flight THEN isLoading is true`() = runTest {
        val deferred = CompletableDeferred<Resource<Unit>>()
        coEvery { createBabyRepository.createBaby(any()) } coAnswers { deferred.await() }

        goToPage2()

        viewModel.state.test {
            awaitItem()
            viewModel.sendIntent(CreateBabyIntent.CreateBaby)

            assertEquals(true, awaitItem().isLoading)

            deferred.complete(Resource.Success(Unit))
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun fillPage0() {
        viewModel.sendIntent(CreateBabyIntent.OnNameChange("Luna"))
        viewModel.sendIntent(CreateBabyIntent.OnGenderChange(BabyGender.GIRL))
        viewModel.sendIntent(CreateBabyIntent.OnResponsibleTypeChange(ResponsibleType.MOTHER))
    }

    private fun fillPage1() {
        viewModel.sendIntent(CreateBabyIntent.OnBirthDateChange("12042024"))
        viewModel.sendIntent(CreateBabyIntent.OnWeightChange("3.4"))
        viewModel.sendIntent(CreateBabyIntent.OnHeightChange("50"))
    }

    private fun goToPage1() {
        fillPage0()
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)
    }

    private fun goToPage2() {
        goToPage1()
        fillPage1()
        viewModel.sendIntent(CreateBabyIntent.OnNextPage)
    }
}
