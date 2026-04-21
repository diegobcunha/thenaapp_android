package com.diegocunha.thenaapp.core.resource

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val exception: Throwable) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

fun <T> Result<T>.toResource(): Resource<T> =
    fold(
        onSuccess = { Resource.Success(it) },
        onFailure = { Resource.Error(it) }
    )
