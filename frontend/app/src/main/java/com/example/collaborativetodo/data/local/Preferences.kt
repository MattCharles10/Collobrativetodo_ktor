package com.example.collaborativetodo.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.collaborativetodo.data.dtos.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "todo_app_prefs")

class Preferences(context: Context) {
    private val dataStore = context.dataStore
    private val gson = Gson()

    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val USER = stringPreferencesKey("user_data")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[Keys.TOKEN] = token
            preferences[Keys.IS_LOGGED_IN] = true
        }
    }

    suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[Keys.USER] = gson.toJson(user)
        }
    }

    fun getToken(): String? {
        return runCatching {
            runBlocking {
                dataStore.data.map { it[Keys.TOKEN] }.first()
            }
        }.getOrNull()
    }

    fun getUser(): User? {
        return runCatching {
            runBlocking {
                val userJson = dataStore.data.map { it[Keys.USER] }.first()
                userJson?.let { gson.fromJson(it, User::class.java) }
            }
        }.getOrNull()
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}