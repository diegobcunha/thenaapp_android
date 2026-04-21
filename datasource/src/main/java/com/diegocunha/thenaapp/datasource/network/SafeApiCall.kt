package com.diegocunha.thenaapp.datasource.network

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.core.resource.toResource
import kotlinx.coroutines.withContext

suspend fun <T> safeApiCall(
    dispatcher: DispatchersProvider,
    call: suspend () -> T
): Resource<T> = withContext(dispatcher.io()) {
    runCatching { call() }.toResource()
}
