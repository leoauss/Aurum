package com.example.aurum.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PreferencesRepositoryImplTest {

    private lateinit var fakeDataStore: FakePreferencesDataStore
    private lateinit var repository: PreferencesRepositoryImpl

    @Before
    fun setUp() {
        fakeDataStore = FakePreferencesDataStore()
        repository = PreferencesRepositoryImpl(fakeDataStore)
    }

    @Test
    fun `initial state returns null last used app`() = runBlocking {
        val initial = repository.getLastUsedUpiApp().first()
        assertNull(initial)
        assertNull(repository.lastUsedAppCached)
    }

    @Test
    fun `setLastUsedUpiApp persists and emits package name and updates cache`() = runBlocking {
        repository.setLastUsedUpiApp("net.one97.paytm")

        val emitted = repository.getLastUsedUpiApp().first()
        assertEquals("net.one97.paytm", emitted)
        assertEquals("net.one97.paytm", repository.lastUsedAppCached)
    }

    @Test
    fun `subsequent updates override previous last used app`() = runBlocking {
        repository.setLastUsedUpiApp("net.one97.paytm")
        assertEquals("net.one97.paytm", repository.getLastUsedUpiApp().first())
        assertEquals("net.one97.paytm", repository.lastUsedAppCached)

        repository.setLastUsedUpiApp("com.phonepe.app")
        assertEquals("com.phonepe.app", repository.getLastUsedUpiApp().first())
        assertEquals("com.phonepe.app", repository.lastUsedAppCached)
    }

    private class FakePreferencesDataStore : DataStore<Preferences> {
        private val stateFlow = MutableStateFlow<Preferences>(emptyPreferences())

        override val data: Flow<Preferences> = stateFlow

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val mutable = stateFlow.value.toMutablePreferences()
            val updated = transform(mutable)
            stateFlow.value = updated
            return updated
        }
    }
}
