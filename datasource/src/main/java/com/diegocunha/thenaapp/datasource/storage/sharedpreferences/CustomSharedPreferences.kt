package com.diegocunha.thenaapp.datasource.storage.sharedpreferences

interface CustomSharedPreferences {

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putString(key: String, value: String?)
    fun getString(key: String): String?
}
