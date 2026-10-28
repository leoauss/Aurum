package com.example.aurum.data.db

import com.example.aurum.domain.model.Transaction
import com.example.aurum.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionRepositoryImplTest {

    private lateinit var fakeDao: FakeTransactionDao
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeTransactionDao()
        repository = TransactionRepositoryImpl(fakeDao)
    }

    @Test
    fun `insert saves transaction entity to DAO`() = runBlocking {
        val txn = Transaction(
            id = "txn-1",
            vpa = "payee@upi",
            amountPaise = 10000L,
            status = TransactionStatus.INITIATED
        )

        repository.insert(txn)

        val all = repository.getAllTransactions().first()
        assertEquals(1, all.size)
        assertEquals("txn-1", all[0].id)
        assertEquals("payee@upi", all[0].vpa)
        assertEquals(10000L, all[0].amountPaise)
    }

    @Test
    fun `updateStatus updates status and upiTxnId`() = runBlocking {
        val txn = Transaction(
            id = "txn-2",
            vpa = "payee@upi",
            amountPaise = 5000L,
            status = TransactionStatus.INITIATED
        )
        repository.insert(txn)

        repository.updateStatus(
            id = "txn-2",
            status = TransactionStatus.SUCCESS,
            upiTxnId = "BANK-REF-99",
            updatedAt = 9999L
        )

        val updated = repository.getAllTransactions().first().first()
        assertEquals(TransactionStatus.SUCCESS, updated.status)
        assertEquals("BANK-REF-99", updated.upiTxnId)
        assertEquals(9999L, updated.updatedAt)
    }

    @Test
    fun `getInitiatedTransactions returns only transactions with INITIATED status`() = runBlocking {
        val txnInitiated = Transaction(
            id = "txn-init",
            vpa = "init@upi",
            amountPaise = 1000L,
            status = TransactionStatus.INITIATED
        )
        val txnSuccess = Transaction(
            id = "txn-succ",
            vpa = "succ@upi",
            amountPaise = 2000L,
            status = TransactionStatus.SUCCESS
        )

        repository.insert(txnInitiated)
        repository.insert(txnSuccess)

        val initiated = repository.getInitiatedTransactions()
        assertEquals(1, initiated.size)
        assertEquals("txn-init", initiated[0].id)
    }

    @Test
    fun `deleteAll clears all transactions`() = runBlocking {
        repository.insert(Transaction(id = "1", vpa = "a@upi", amountPaise = 100L))
        repository.insert(Transaction(id = "2", vpa = "b@upi", amountPaise = 200L))

        repository.deleteAll()

        val all = repository.getAllTransactions().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun `getTransactionById returns correct transaction or null`() = runBlocking {
        val txn = Transaction(id = "find-me", vpa = "find@upi", amountPaise = 500L)
        repository.insert(txn)

        val found = repository.getTransactionById("find-me")
        assertEquals("find-me", found?.id)
        assertEquals("find@upi", found?.vpa)

        val notFound = repository.getTransactionById("unknown")
        org.junit.Assert.assertNull(notFound)
    }

    private class FakeTransactionDao : TransactionDao {
        private val storage = mutableMapOf<String, TransactionEntity>()
        private val stateFlow = MutableStateFlow<List<TransactionEntity>>(emptyList())

        private fun updateFlow() {
            stateFlow.value = storage.values.sortedByDescending { it.createdAt }
        }

        override suspend fun insertTransaction(entity: TransactionEntity) {
            storage[entity.id] = entity
            updateFlow()
        }

        override suspend fun updateStatus(id: String, status: String, upiTxnId: String?, updatedAt: Long) {
            val existing = storage[id] ?: return
            storage[id] = existing.copy(
                status = status,
                upiTxnId = upiTxnId ?: existing.upiTxnId,
                updatedAt = updatedAt
            )
            updateFlow()
        }

        override fun getAllTransactions(): Flow<List<TransactionEntity>> = stateFlow

        override suspend fun getInitiatedTransactions(): List<TransactionEntity> {
            return storage.values.filter { it.status == TransactionStatus.INITIATED.name }
        }

        override suspend fun getTransactionById(id: String): TransactionEntity? = storage[id]

        override suspend fun deleteAll() {
            storage.clear()
            updateFlow()
        }
    }
}

