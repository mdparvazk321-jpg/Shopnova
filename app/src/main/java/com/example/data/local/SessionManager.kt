package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "shopnova_session")

class SessionManager(private val context: Context) {
    companion object {
        private val KEY_CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        const val DEFAULT_BUYER_ID = "user_buyer_demo"
    }

    val currentUserIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        if (prefs.contains(KEY_CURRENT_USER_ID)) {
            prefs[KEY_CURRENT_USER_ID]?.takeIf { it.isNotBlank() }
        } else {
            DEFAULT_BUYER_ID
        }
    }

    suspend fun setCurrentUserId(userId: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENT_USER_ID] = userId ?: ""
        }
    }
}
