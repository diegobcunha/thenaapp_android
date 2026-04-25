package com.diegocunha.thenaapp.feature.home.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.baby.BabyResponse
import com.diegocunha.thenaapp.datasource.network.model.user.UserResponse
import com.diegocunha.thenaapp.datasource.network.service.UserService
import io.mockk.coEvery
import io.mockk.every
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class HomeRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val userService: UserService = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk {
        every { io() } returns testDispatcher
    }
    private lateinit var repository: HomeRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = HomeRepositoryImpl(userService, dispatchersProvider)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN getUsersInformation succeeds with babies THEN Resource Success with HomeUserInformation is returned`() = runTest {
        coEvery { userService.getUsersInformation() } returns makeUserResponse(
            name = "Test User",
            babies = listOf(makeBabyResponse())
        )

        val result = repository.getUserInformation()

        assertTrue(result is Resource.Success)
        assertEquals("Test User", (result as Resource.Success).data.userName)
        assertEquals("Baby Luna", result.data.babyInformation.babyName)
    }

    @Test
    fun `WHEN user babies list is empty THEN Resource Error is returned`() = runTest {
        coEvery { userService.getUsersInformation() } returns makeUserResponse(
            name = "Test User",
            babies = emptyList()
        )

        val result = repository.getUserInformation()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN userService throws THEN Resource Error is returned`() = runTest {
        coEvery { userService.getUsersInformation() } throws Exception("network error")

        val result = repository.getUserInformation()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN getUsersInformation succeeds THEN babyHeight is scaled to 0 decimal places`() = runTest {
        coEvery { userService.getUsersInformation() } returns makeUserResponse(
            babies = listOf(makeBabyResponse(birthHeight = BigDecimal("65.7")))
        )

        val result = repository.getUserInformation()

        assertEquals(0, (result as Resource.Success).data.babyInformation.babyHeight.scale())
    }

    @Test
    fun `WHEN getUsersInformation succeeds THEN babyWeight is scaled to 2 decimal places`() = runTest {
        coEvery { userService.getUsersInformation() } returns makeUserResponse(
            babies = listOf(makeBabyResponse(birthWeight = BigDecimal("4.123")))
        )

        val result = repository.getUserInformation()

        assertEquals(2, (result as Resource.Success).data.babyInformation.babyWeight.scale())
    }

    @Test
    fun `WHEN user name is null THEN userName is empty string`() = runTest {
        coEvery { userService.getUsersInformation() } returns makeUserResponse(
            name = null,
            babies = listOf(makeBabyResponse())
        )

        val result = repository.getUserInformation()

        assertEquals("", (result as Resource.Success).data.userName)
    }

    @Test
    fun `WHEN photoUrl is null THEN babyPhotoUrl is null`() = runTest {
        coEvery { userService.getUsersInformation() } returns makeUserResponse(
            babies = listOf(makeBabyResponse(photoUrl = null))
        )

        val result = repository.getUserInformation()

        assertNull((result as Resource.Success).data.babyInformation.babyPhotoUrl)
    }

    private fun makeBabyResponse(
        name: String = "Baby Luna",
        birthDate: String = "2023-01-01",
        photoUrl: String? = "https://example.com/photo.jpg",
        birthWeight: BigDecimal = BigDecimal("4.00"),
        birthHeight: BigDecimal = BigDecimal("50.00"),
    ) = BabyResponse(
        id = UUID.randomUUID(),
        name = name,
        birthDate = birthDate,
        gender = "GIRL",
        photoUrl = photoUrl,
        responsible = emptyList(),
        birthWeight = birthWeight,
        birthHeight = birthHeight,
    )

    private fun makeUserResponse(
        name: String? = "Test User",
        babies: List<BabyResponse> = listOf(makeBabyResponse()),
    ) = UserResponse(
        id = UUID.randomUUID(),
        name = name,
        email = "test@example.com",
        babies = babies,
    )
}
