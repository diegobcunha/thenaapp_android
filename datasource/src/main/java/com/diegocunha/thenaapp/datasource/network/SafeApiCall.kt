package com.diegocunha.thenaapp.datasource.network

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.core.resource.toResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import timber.log.Timber

suspend fun <T> safeApiCall(
    dispatcher: DispatchersProvider,
    call: suspend () -> T
): Resource<T> = withContext(dispatcher.io()) {
    runCatching { call() }
        .onFailure {
            Timber.e(it, "safeApiCall failed with error: $it")
            if (it is CancellationException) throw it
        }
        .map {
            Timber.d(it.toString())
            it
        }
        .toResource()
}
