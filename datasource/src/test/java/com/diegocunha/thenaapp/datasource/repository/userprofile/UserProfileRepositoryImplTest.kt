package com.diegocunha.thenaapp.datasource.repository.userprofile

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.baby.BabyResponse
import com.diegocunha.thenaapp.datasource.network.model.user.UserResponse
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

class UserProfileRepositoryImplTest {

    private val userService: UserService = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk {
        every { io() } returns Dispatchers.Unconfined
    }
    private val mockFirebaseUser: FirebaseUser = mockk()
    private val firebaseAuth: FirebaseAuth = mockk {
        every { currentUser } returns mockFirebaseUser
    }
    private val repository =
        UserProfileRepositoryImpl(userService, dispatchersProvider, firebaseAuth)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `WHEN name is null THEN returns MissingName with hasBaby false`() = runTest {
        coEvery { userService.getUsersInformation() } returns userResponse(
            name = null,
            babies = emptyList()
        )

        val result = repository.getProfileStatus()

        Assert.assertTrue(result is Resource.Success)
        Assert.assertEquals(
            ProfileStatus.MissingName(hasBaby = false),
            (result as Resource.Success).data
        )
    }

    @Test
    fun `WHEN name is blank THEN returns MissingName with hasBaby false`() = runTest {
        coEvery { userService.getUsersInformation() } returns userResponse(
            name = "  ",
            babies = emptyList()
        )

        val result = repository.getProfileStatus()

        Assert.assertTrue(result is Resource.Success)
        Assert.assertEquals(
            ProfileStatus.MissingName(hasBaby = false),
            (result as Resource.Success).data
        )
    }

    @Test
    fun `WHEN name is null but has baby THEN returns MissingName with hasBaby true`() = runTest {
        coEvery { userService.getUsersInformation() } returns userResponse(
            name = null,
            babies = listOf(mockBaby())
        )

        val result = repository.getProfileStatus()

        Assert.assertTrue(result is Resource.Success)
        Assert.assertEquals(
            ProfileStatus.MissingName(hasBaby = true),
            (result as Resource.Success).data
        )
    }

    @Test
    fun `WHEN name is set and babies is empty THEN returns MissingBaby`() = runTest {
        coEvery { userService.getUsersInformation() } returns userResponse(
            name = "Diego",
            babies = emptyList()
        )

        val result = repository.getProfileStatus()

        Assert.assertTrue(result is Resource.Success)
        Assert.assertEquals(ProfileStatus.MissingBaby, (result as Resource.Success).data)
    }

    @Test
    fun `WHEN name is set and has baby THEN returns Complete`() = runTest {
        coEvery { userService.getUsersInformation() } returns userResponse(
            name = "Diego",
            babies = listOf(mockBaby())
        )

        val result = repository.getProfileStatus()

        Assert.assertTrue(result is Resource.Success)
        Assert.assertEquals(ProfileStatus.Complete, (result as Resource.Success).data)
    }

    @Test
    fun `WHEN service throws THEN returns Error`() = runTest {
        coEvery { userService.getUsersInformation() } throws RuntimeException("Network error")

        val result = repository.getProfileStatus()

        Assert.assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN current user is present THEN getFirebaseAuthUser returns user`() = runTest {
        val result = repository.getFirebaseAuthUser()

        Assert.assertNotNull(result)
    }

    @Test
    fun `WHEN current user is null THEN getFirebaseAuthUser throws`() = runTest {
        every { firebaseAuth.currentUser } returns null

        var threw = false
        try {
            repository.getFirebaseAuthUser()
        } catch (e: Exception) {
            threw = true
        }

        Assert.assertTrue(threw)
    }

    private fun userResponse(name: String?, babies: List<BabyResponse>) = UserResponse(
        id = UUID.randomUUID(),
        name = name,
        email = "test@test.com",
        babies = babies,
    )

    private fun mockBaby() = BabyResponse(
        id = UUID.randomUUID(),
        name = "Luna",
        birthDate = "2024-01-01",
        gender = "girl",
        photoUrl = null,
        responsible = emptyList(),
        birthWeight = BigDecimal("3.5"),
        birthHeight = BigDecimal("50"),
    )
}
