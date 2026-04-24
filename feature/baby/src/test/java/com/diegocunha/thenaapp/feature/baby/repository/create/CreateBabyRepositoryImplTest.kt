package com.diegocunha.thenaapp.feature.baby.repository.create

import android.net.Uri
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.CloudinaryResponse
import com.diegocunha.thenaapp.datasource.network.model.baby.BabyResponse
import com.diegocunha.thenaapp.datasource.network.service.BabyService
import com.diegocunha.thenaapp.datasource.network.service.CloudinaryService
import com.diegocunha.thenaapp.feature.baby.domain.create.dto.CreateBabyRequest
import com.diegocunha.thenaapp.feature.baby.domain.model.BabyGender
import com.diegocunha.thenaapp.feature.baby.domain.model.ResponsibleType
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class CreateBabyRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val babyService: BabyService = mockk()
    private val cloudinaryService: CloudinaryService = mockk()
    private val imageCompressor: ImageCompressor = mockk()
    private val dispatchersProvider: DispatchersProvider = mockk {
        every { io() } returns testDispatcher
    }

    private lateinit var repository: CreateBabyRepositoryImpl

    private val babyId = UUID.randomUUID()
    private val mockBabyResponse = BabyResponse(
        id = babyId,
        name = "Luna",
        birthDate = "2024-04-12",
        gender = "GIRL",
        photoUrl = null,
        responsible = emptyList(),
    )
    private val mockCloudinaryResponse = CloudinaryResponse(
        secureUrl = "https://res.cloudinary.com/test/image/upload/profile.jpg",
        publicId = babyId.toString(),
        bytes = 50000,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = CreateBabyRepositoryImpl(
            service = babyService,
            cloudinaryService = cloudinaryService,
            dispatchersProvider = dispatchersProvider,
            imageCompressor = imageCompressor,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN createBaby without photo THEN updateBaby called with null photoUrl and returns Success`() = runTest {
        coEvery { babyService.createBaby(any()) } returns mockBabyResponse
        coEvery { babyService.updateBaby(any(), any()) } returns mockBabyResponse

        val result = repository.createBaby(buildRequest(photo = null))

        assertTrue(result is Resource.Success)
        coVerify(exactly = 0) { imageCompressor.compress(any()) }
        coVerify { babyService.updateBaby(babyId, match { it.photoUrl == null }) }
    }

    @Test
    fun `WHEN createBaby with photo THEN image is uploaded and updateBaby called with secureUrl`() = runTest {
        val mockUri: Uri = mockk()
        coEvery { babyService.createBaby(any()) } returns mockBabyResponse
        coEvery { imageCompressor.compress(mockUri) } returns ByteArray(100)
        coEvery { cloudinaryService.uploadImage(any(), any(), any(), any()) } returns mockCloudinaryResponse
        coEvery { babyService.updateBaby(any(), any()) } returns mockBabyResponse

        val result = repository.createBaby(buildRequest(photo = mockUri))

        assertTrue(result is Resource.Success)
        coVerify { imageCompressor.compress(mockUri) }
        coVerify {
            babyService.updateBaby(
                babyId,
                match { it.photoUrl == mockCloudinaryResponse.secureUrl }
            )
        }
    }

    @Test
    fun `WHEN service createBaby throws THEN returns Resource Error`() = runTest {
        coEvery { babyService.createBaby(any()) } throws Exception("Network error")

        val result = repository.createBaby(buildRequest())

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN cloudinaryService uploadImage throws THEN returns Resource Error`() = runTest {
        val mockUri: Uri = mockk()
        coEvery { babyService.createBaby(any()) } returns mockBabyResponse
        coEvery { imageCompressor.compress(any()) } returns ByteArray(100)
        coEvery { cloudinaryService.uploadImage(any(), any(), any(), any()) } throws Exception("Upload failed")

        val result = repository.createBaby(buildRequest(photo = mockUri))

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `WHEN service updateBaby throws THEN returns Resource Error`() = runTest {
        coEvery { babyService.createBaby(any()) } returns mockBabyResponse
        coEvery { babyService.updateBaby(any(), any()) } throws Exception("Update failed")

        val result = repository.createBaby(buildRequest(photo = null))

        assertTrue(result is Resource.Error)
    }

    private fun buildRequest(photo: Uri? = null) = CreateBabyRequest(
        name = "Luna",
        birthDate = "2024-04-12",
        gender = BabyGender.GIRL,
        responsibleType = ResponsibleType.MOTHER,
        photo = photo,
    )
}
