package com.example.agedogs.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "agedogs_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_SAVED_EMAIL = stringPreferencesKey("saved_email")
        val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    val savedEmailFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_SAVED_EMAIL] ?: "" }

    val rememberMeFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_REMEMBER_ME] ?: false }

    suspend fun saveRememberMeData(email: String, remember: Boolean) {
        context.dataStore.edit { prefs ->
            if (remember) {
                prefs[KEY_SAVED_EMAIL] = email
                prefs[KEY_REMEMBER_ME] = true
            } else {
                prefs[KEY_SAVED_EMAIL] = ""
                prefs[KEY_REMEMBER_ME] = false
            }
        }
    }
}
