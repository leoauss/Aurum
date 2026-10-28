package com.example.aurum.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room operations on [TransactionEntity].
 */
@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: TransactionEntity)

    @Query(
        """
        UPDATE transactions 
        SET status = :status, 
            upi_txn_id = COALESCE(:upiTxnId, upi_txn_id), 
            updated_at = :updatedAt 
        WHERE id = :id
        """
    )
    suspend fun updateStatus(
        id: String,
        status: String,
        upiTxnId: String?,
        updatedAt: Long
    )

    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = 'INITIATED' ORDER BY created_at DESC")
    suspend fun getInitiatedTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

