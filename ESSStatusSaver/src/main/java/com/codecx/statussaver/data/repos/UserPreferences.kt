package com.codecx.statussaver.data.repos

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import androidx.core.content.edit

class UserPreferences(private val context: Context) {

    private val Context.userDataStore by preferencesDataStore("user_preferences")
    private val FIRST_RUN_KEY = booleanPreferencesKey("is_first_run")

    private val USER_LANGUAGE_KEY = stringPreferencesKey("user_language")

    private val USER_SELECTED_DIR_PATH = stringPreferencesKey("user_selected_dir_path")

    val userLanguage = context.userDataStore.data.map { preferences ->
        preferences[USER_LANGUAGE_KEY] ?: "en"
    }


    val isFirstRun = context.userDataStore.data.map { preferences ->
        preferences[FIRST_RUN_KEY] ?: true
    }

    val userSelectedDirPath = context.userDataStore.data.map { preferences ->
        preferences[USER_SELECTED_DIR_PATH] ?: ""
    }


    suspend fun updateSelectedDirPath(path: String) {
        context.userDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[USER_SELECTED_DIR_PATH] = path
            }
        }
    }

    suspend fun updateUserLanguage(language: String) {
        context.userDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[USER_LANGUAGE_KEY] = language
            }
        }
    }

    suspend fun updateFirstRun(value: Boolean) {
        context.userDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[FIRST_RUN_KEY] = value
            }
        }
    }

    fun updatePremium(value: Boolean) {
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE).edit {
            putBoolean("is_premium", value)
        }
    }

    fun isUserPremium(): Boolean {
        return context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            .getBoolean("is_premium", false)
    }

}