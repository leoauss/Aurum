package com.example.aurum.di

import com.example.aurum.data.db.TransactionRepositoryImpl
import com.example.aurum.data.prefs.PreferencesRepositoryImpl
import com.example.aurum.data.upi.UpiAppRepositoryImpl
import com.example.aurum.domain.repository.PreferencesRepository
import com.example.aurum.domain.repository.TransactionRepository
import com.example.aurum.domain.repository.UpiAppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindUpiAppRepository(
        impl: UpiAppRepositoryImpl
    ): UpiAppRepository
}

