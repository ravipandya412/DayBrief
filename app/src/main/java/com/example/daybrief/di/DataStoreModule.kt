package com.example.daybrief.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.daybrief.data.local.LocalDataSource

private val Context.dataStore by preferencesDataStore(name = "daybrief_prefs")

object DataStoreModule {
    fun provideLocalDataSource(context: Context): LocalDataSource =
        LocalDataSource(context.dataStore)
}
