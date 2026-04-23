package com.diegocunha.thenaapp.datasource.storage.sharedpreferences

import android.content.Context
import androidx.core.content.edit
import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CustomSharedPreferencesImpl(
    private val context: Context,
    private val dispatchersProvider: DispatchersProvider,
) : CustomSharedPreferences {

    private val job = CoroutineScope(SupervisorJob() + dispatchersProvider.io())
    private val values = HashMap<String, Any?>()
    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
    }

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
        job.launch {
            sharedPreferences.edit { putBoolean(key, value) }
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (values.containsKey(key)) {
            values[key] as? Boolean ?: defaultValue
        } else {
            val result = sharedPreferences.getBoolean(key, defaultValue)
            values[key] = result
            result
        }
    }

    companion object {
        private const val SHARED_PREFERENCES = "thena_sp"
    }
}
