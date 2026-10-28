package com.example.aurum.domain.repository

import com.example.aurum.domain.model.Transaction
import com.example.aurum.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer contract for persisting and querying UPI [Transaction] records.
 *
 * The data layer provides the implementation (`TransactionRepositoryImpl`).
 * All methods are `suspend` or return a [Flow] so they must be called from a coroutine.
 */
interface TransactionRepository {

    /**
     * Inserts a new transaction into persistent storage.
     *
     * @param transaction The transaction to save.
     */
    suspend fun insert(transaction: Transaction)

    /**
     * Updates the [status] and optional [upiTxnId] of an existing transaction.
     *
     * @param id          UUID of the transaction to update.
     * @param status      New lifecycle status.
     * @param upiTxnId    Optional UPI transaction ID returned by the payment app.
     * @param updatedAt   Timestamp in milliseconds; defaults to current time.
     */
    suspend fun updateStatus(
        id: String,
        status: TransactionStatus,
        upiTxnId: String? = null,
        updatedAt: Long = System.currentTimeMillis(),
    )

    /**
     * Returns a [Flow] of all transactions ordered by creation time (newest first).
     */
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Returns all transactions that are still in the [TransactionStatus.INITIATED] state.
     * Used for process-death reconciliation on cold start.
     */
    suspend fun getInitiatedTransactions(): List<Transaction>

    /**
     * Retrieves a single transaction by its unique [id], or null if not found.
     */
    suspend fun getTransactionById(id: String): Transaction?

    /**
     * Permanently deletes all transactions from storage.
     */
    suspend fun deleteAll()
}

