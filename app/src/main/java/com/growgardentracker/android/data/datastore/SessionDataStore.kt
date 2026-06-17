package com.growgardentracker.android.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "grow_session")

class SessionDataStore(private val context: Context) {
    private val userIdKey = longPreferencesKey("logged_in_user_id")

    val loggedInUserId: Flow<Long?> = context.sessionDataStore.data.map { preferences ->
        val value = preferences[userIdKey] ?: 0L
        if (value == 0L) null else value
    }

    suspend fun saveLoggedInUser(userId: Long) {
        context.sessionDataStore.edit { preferences ->
            preferences[userIdKey] = userId
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(userIdKey)
        }
    }
}
