package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bihar_seva_settings"
)

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("app_language")
        val DEFAULT_DISTRICT = stringPreferencesKey("default_district")
        val DEFAULT_GST = intPreferencesKey("default_gst_percent")
        val COMPACT_MODE = booleanPreferencesKey("compact_mode")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    val settingsFlow: Flow<UserSettings> = context.settingsDataStore.data.map { prefs ->
        val theme = ThemeMode.entries.firstOrNull { it.name == prefs[Keys.THEME_MODE] }
            ?: ThemeMode.SYSTEM
        val lang = AppLanguage.entries.firstOrNull { it.name == prefs[Keys.LANGUAGE] }
            ?: AppLanguage.BILINGUAL
        UserSettings(
            themeMode = theme,
            language = lang,
            defaultDistrict = prefs[Keys.DEFAULT_DISTRICT] ?: "Patna",
            defaultGstPercent = prefs[Keys.DEFAULT_GST] ?: 18,
            compactMode = prefs[Keys.COMPACT_MODE] ?: false,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setDefaultDistrict(district: String) {
        context.settingsDataStore.edit { it[Keys.DEFAULT_DISTRICT] = district }
    }

    suspend fun setDefaultGstPercent(gst: Int) {
        context.settingsDataStore.edit { it[Keys.DEFAULT_GST] = gst }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.COMPACT_MODE] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.HAPTIC_FEEDBACK] = enabled }
    }
}
