package com.example.aurum.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.aurum.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed implementation of [PreferencesRepository].
 */
@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    private val cachedLastUsedApp = AtomicReference<String?>(null)

    override val lastUsedAppCached: String?
        get() = cachedLastUsedApp.get()

    override fun getLastUsedUpiApp(): Flow<String?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEY_LAST_USED_UPI_APP]
            }
            .onEach { cached ->
                cachedLastUsedApp.set(cached)
            }
    }

    override suspend fun setLastUsedUpiApp(packageName: String) {
        cachedLastUsedApp.set(packageName)
        dataStore.edit { preferences ->
            preferences[KEY_LAST_USED_UPI_APP] = packageName
        }
    }

    companion object {
        val KEY_LAST_USED_UPI_APP = stringPreferencesKey("last_used_upi_app")
    }
}
