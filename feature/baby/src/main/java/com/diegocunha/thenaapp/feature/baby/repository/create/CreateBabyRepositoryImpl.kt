package com.diegocunha.thenaapp.feature.baby.repository.create

import android.net.Uri
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.BuildConfig
import com.diegocunha.thenaapp.datasource.network.model.baby.UpdateBabyRequest
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.BabyService
import com.diegocunha.thenaapp.datasource.network.service.CloudinaryService
import com.diegocunha.thenaapp.feature.baby.domain.create.CreateBabyRepository
import com.diegocunha.thenaapp.feature.baby.domain.create.dto.CreateBabyRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import com.diegocunha.thenaapp.datasource.network.model.baby.CreateBabyRequest as DatasourceCreateBabyRequest

class CreateBabyRepositoryImpl(
    private val service: BabyService,
    private val cloudinaryService: CloudinaryService,
    private val dispatchersProvider: DispatchersProvider,
    private val imageCompressor: ImageCompressor,
) : CreateBabyRepository {

    override suspend fun createBaby(request: CreateBabyRequest): Resource<Unit> =
        safeApiCall(dispatchersProvider) {
            val baby = service.createBaby(
                DatasourceCreateBabyRequest(
                    name = request.name,
                    birthDate = request.birthDate,
                    gender = request.gender.name,
                    responsible = request.responsibleType.name,
                )
            )

            val photoUrl = request.photo?.let { uploadPhoto(baby.id, it) }
            service.updateBaby(
                baby.id, UpdateBabyRequest(
                    photoUrl = photoUrl,
                )
            )
        }

    private suspend fun uploadPhoto(babyId: UUID, photoUri: Uri): String? {
        val imageBytes = imageCompressor.compress(photoUri)

        val filePart = MultipartBody.Part.createFormData(
            name = NAME,
            filename = FILE_NAME,
            body = imageBytes.toRequestBody(IMAGE_JPEG.toMediaType())
        )

        val preset = UPLOAD_PRESET.toRequestBody(TEXT_PLAIN.toMediaType())
        val publicId = babyId.toString().toRequestBody(TEXT_PLAIN.toMediaType())

        val response = cloudinaryService.uploadImage(
            file = filePart,
            cloudName = CLOUD_NAME,
            uploadPreset = preset,
            publicId = publicId
        )

        return response.secureUrl
    }

    companion object {
        private const val CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME
        private const val UPLOAD_PRESET = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        private const val NAME = "file"
        private const val FILE_NAME = "profile.jpg"
        private const val TEXT_PLAIN = "text/plain"
        private const val IMAGE_JPEG = "image/jpeg"
    }
}
