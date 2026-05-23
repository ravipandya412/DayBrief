package com.example.daybrief.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.daybrief.domain.model.AppSettings
import com.example.daybrief.domain.model.BriefingEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalDataSource(private val dataStore: DataStore<Preferences>) {

    private val gson = Gson()

    val briefingHistory: Flow<List<BriefingEntry>> = dataStore.data.map { prefs ->
        val json = prefs[KEY_HISTORY] ?: return@map emptyList()
        val type = object : TypeToken<List<BriefingEntry>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        val json = prefs[KEY_SETTINGS] ?: return@map AppSettings()
        gson.fromJson(json, AppSettings::class.java) ?: AppSettings()
    }

    suspend fun saveBriefing(entry: BriefingEntry) {
        dataStore.edit { prefs ->
            val existing: List<BriefingEntry> = prefs[KEY_HISTORY]
                ?.let { gson.fromJson(it, object : TypeToken<List<BriefingEntry>>() {}.type) }
                ?: emptyList()
            val updated = (listOf(entry) + existing).take(MAX_HISTORY)
            prefs[KEY_HISTORY] = gson.toJson(updated)
        }
    }

    suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_SETTINGS] = gson.toJson(settings)
        }
    }

    companion object {
        private val KEY_HISTORY = stringPreferencesKey("briefing_history")
        private val KEY_SETTINGS = stringPreferencesKey("app_settings")
        private const val MAX_HISTORY = 7
    }
}
