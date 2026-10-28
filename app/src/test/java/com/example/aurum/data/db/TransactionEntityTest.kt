package com.example.aurum.data.db

import com.example.aurum.domain.model.Transaction
import com.example.aurum.domain.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class TransactionEntityTest {

    @Test
    fun `toEntity maps all fields correctly from domain Transaction`() {
        val domain = Transaction(
            id = "test-uuid-123",
            vpa = "merchant@upi",
            payeeName = "Merchant Shop",
            amountPaise = 15000L,
            note = "Order #1",
            qrTxnRef = "REF123",
            upiTxnId = "UPI999",
            appPackage = "net.one97.paytm",
            status = TransactionStatus.SUCCESS,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val entity = domain.toEntity()

        assertEquals("test-uuid-123", entity.id)
        assertEquals("merchant@upi", entity.vpa)
        assertEquals("Merchant Shop", entity.payeeName)
        assertEquals(15000L, entity.amountPaise)
        assertEquals("Order #1", entity.note)
        assertEquals("REF123", entity.qrTxnRef)
        assertEquals("UPI999", entity.upiTxnId)
        assertEquals("net.one97.paytm", entity.appPackage)
        assertEquals("SUCCESS", entity.status)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
    }

    @Test
    fun `toDomain maps all fields correctly from TransactionEntity`() {
        val entity = TransactionEntity(
            id = "test-uuid-456",
            vpa = "friend@okaxis",
            payeeName = "Friend",
            amountPaise = 5000L,
            note = null,
            qrTxnRef = null,
            upiTxnId = null,
            appPackage = "com.google.android.apps.nbu.paisa.user",
            status = "PENDING",
            createdAt = 3000L,
            updatedAt = 4000L
        )

        val domain = entity.toDomain()

        assertEquals("test-uuid-456", domain.id)
        assertEquals("friend@okaxis", domain.vpa)
        assertEquals("Friend", domain.payeeName)
        assertEquals(5000L, domain.amountPaise)
        assertEquals(BigDecimal("50.00"), domain.amountRupees)
        assertNull(domain.note)
        assertNull(domain.qrTxnRef)
        assertNull(domain.upiTxnId)
        assertEquals("com.google.android.apps.nbu.paisa.user", domain.appPackage)
        assertEquals(TransactionStatus.PENDING, domain.status)
        assertEquals(3000L, domain.createdAt)
        assertEquals(4000L, domain.updatedAt)
    }

    @Test
    fun `toDomain falls back to UNCONFIRMED if status string is unknown`() {
        val entity = TransactionEntity(
            id = "test-uuid-789",
            vpa = "user@upi",
            amountPaise = 1000L,
            status = "SOME_UNKNOWN_LEGACY_STATUS",
            createdAt = 100L,
            updatedAt = 200L
        )

        val domain = entity.toDomain()
        assertEquals(TransactionStatus.UNCONFIRMED, domain.status)
    }

    @Test
    fun `roundtrip domain to entity to domain preserves equality`() {
        val original = Transaction(
            id = "roundtrip-id",
            vpa = "store@upi",
            payeeName = "General Store",
            amountPaise = 299900L,
            note = "Groceries",
            qrTxnRef = "QR123",
            upiTxnId = "TXN789",
            appPackage = "com.phonepe.app",
            status = TransactionStatus.USER_CONFIRMED_PAID,
            createdAt = 5000L,
            updatedAt = 6000L
        )

        val roundtripped = original.toEntity().toDomain()

        assertEquals(original, roundtripped)
    }
}

