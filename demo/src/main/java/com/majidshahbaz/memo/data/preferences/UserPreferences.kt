package com.majidshahbaz.memo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    private object PreferencesKeys {
        val DOWNLOAD_ONBOARDING_SHOWN = booleanPreferencesKey("download_onboarding_shown")
        val SELECTED_MODEL_TIER = androidx.datastore.preferences.core.stringPreferencesKey("selected_model_tier")
    }

    val downloadOnboardingShown: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DOWNLOAD_ONBOARDING_SHOWN] ?: false
        }

    val selectedModelTier: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_MODEL_TIER]
        }

    suspend fun setDownloadOnboardingShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOWNLOAD_ONBOARDING_SHOWN] = shown
        }
    }

    suspend fun setSelectedModelTier(tier: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_MODEL_TIER] = tier
        }
    }
}
