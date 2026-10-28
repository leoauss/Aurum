package com.example.aurum.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer contract for accessing and updating user preferences.
 *
 * The data layer implementation uses Jetpack DataStore under the hood.
 */
interface PreferencesRepository {

    /**
     * Returns a [Flow] that emits the package name of the last UPI app selected
     * by the user, or `null` if none has been selected yet.
     */
    fun getLastUsedUpiApp(): Flow<String?>

    /**
     * Persists the [packageName] of the UPI app most recently used for payment.
     *
     * @param packageName Package name to save (e.g. `"net.one97.paytm"`).
     */
    suspend fun setLastUsedUpiApp(packageName: String)

    /**
     * Synchronous in-memory cached value of the last used UPI app package name, if known.
     */
    val lastUsedAppCached: String?
        get() = null
}

