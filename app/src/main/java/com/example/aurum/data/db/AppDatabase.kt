package com.example.aurum.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Main Room database instance for Aurum application.
 */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "aurum_transactions.db"
    }
}

