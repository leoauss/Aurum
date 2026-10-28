package com.example.aurum.data.db

import com.example.aurum.domain.model.Transaction
import com.example.aurum.domain.model.TransactionStatus
import com.example.aurum.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of [TransactionRepository].
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateStatus(
        id: String,
        status: TransactionStatus,
        upiTxnId: String?,
        updatedAt: Long
    ) {
        transactionDao.updateStatus(
            id = id,
            status = status.name,
            upiTxnId = upiTxnId,
            updatedAt = updatedAt
        )
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getInitiatedTransactions(): List<Transaction> {
        return transactionDao.getInitiatedTransactions().map { it.toDomain() }
    }

    override suspend fun deleteAll() {
        transactionDao.deleteAll()
    }
}

