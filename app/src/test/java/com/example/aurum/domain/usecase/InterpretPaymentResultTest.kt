package com.example.aurum.domain.usecase

import com.example.aurum.domain.model.PaymentOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InterpretPaymentResultTest {

    private lateinit var interpret: InterpretPaymentResult

    @Before
    fun setUp() {
        interpret = InterpretPaymentResult()
    }

    // ── SUCCESS ───────────────────────────────────────────────────────────────

    @Test
    fun `STATUS=SUCCESS maps to PaymentOutcome_Success`() {
        val result = interpret(mapOf("Status" to "SUCCESS", "txnId" to "TXN001"))
        assertTrue(result is PaymentOutcome.Success)
    }

    @Test
    fun `SUCCESS extracts txnId, approvalRefNo, and responseCode`() {
        val result = interpret(
            mapOf(
                "Status" to "SUCCESS",
                "txnId" to "TXN001",
                "responseCode" to "00",
                "ApprovalRefNo" to "RRN999"
            )
        )
        val outcome = result as PaymentOutcome.Success
        assertEquals("TXN001", outcome.txnId)
        assertEquals("00", outcome.responseCode)
        assertEquals("RRN999", outcome.approvalRefNo)
    }

    @Test
    fun `SUCCESS is case-insensitive for key and value`() {
        val result = interpret(mapOf("STATUS" to "success"))
        assertTrue(result is PaymentOutcome.Success)
    }

    @Test
    fun `mixed-case SUCCESS key and value is accepted`() {
        val result = interpret(mapOf("Status" to "Success"))
        assertTrue(result is PaymentOutcome.Success)
    }

    // ── FAILURE / FAILED ──────────────────────────────────────────────────────

    @Test
    fun `STATUS=FAILURE maps to PaymentOutcome_Failed`() {
        val result = interpret(mapOf("Status" to "FAILURE"))
        assertTrue(result is PaymentOutcome.Failed)
    }

    @Test
    fun `STATUS=FAILED also maps to PaymentOutcome_Failed`() {
        val result = interpret(mapOf("Status" to "FAILED"))
        assertTrue(result is PaymentOutcome.Failed)
    }

    @Test
    fun `FAILURE extracts responseCode and message`() {
        val result = interpret(
            mapOf(
                "Status" to "FAILURE",
                "responseCode" to "ZM",
                "message" to "Payment declined"
            )
        )
        val outcome = result as PaymentOutcome.Failed
        assertEquals("ZM", outcome.responseCode)
        assertEquals("Payment declined", outcome.message)
    }

    // ── PENDING / SUBMITTED ───────────────────────────────────────────────────

    @Test
    fun `STATUS=PENDING maps to PaymentOutcome_Pending`() {
        val result = interpret(mapOf("Status" to "PENDING"))
        assertTrue(result is PaymentOutcome.Pending)
    }

    @Test
    fun `STATUS=SUBMITTED maps to PaymentOutcome_Pending`() {
        val result = interpret(mapOf("Status" to "SUBMITTED"))
        assertTrue(result is PaymentOutcome.Pending)
    }

    @Test
    fun `PENDING extracts txnId`() {
        val result = interpret(mapOf("Status" to "PENDING", "txnId" to "TXN002"))
        val outcome = result as PaymentOutcome.Pending
        assertEquals("TXN002", outcome.txnId)
    }

    // ── UNCONFIRMED ───────────────────────────────────────────────────────────

    @Test
    fun `empty extras map returns Unconfirmed`() {
        val result = interpret(emptyMap())
        assertTrue(result is PaymentOutcome.Unconfirmed)
    }

    @Test
    fun `null status value returns Unconfirmed`() {
        val result = interpret(mapOf("Status" to null))
        assertTrue(result is PaymentOutcome.Unconfirmed)
    }

    @Test
    fun `unknown status string returns Unconfirmed`() {
        val result = interpret(mapOf("Status" to "CANCELLED"))
        assertTrue(result is PaymentOutcome.Unconfirmed)
        val outcome = result as PaymentOutcome.Unconfirmed
        assertTrue(outcome.message?.contains("CANCELLED") == true)
    }

    @Test
    fun `extras with no Status key returns Unconfirmed with no-status message`() {
        val result = interpret(mapOf("txnId" to "TXN999"))
        val outcome = result as PaymentOutcome.Unconfirmed
        assertEquals("No status in response", outcome.message)
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `blank txnId is treated as absent (null) in Success`() {
        val result = interpret(mapOf("Status" to "SUCCESS", "txnId" to "  "))
        val outcome = result as PaymentOutcome.Success
        assertNull(outcome.txnId)
    }

    @Test
    fun `blank responseCode is treated as absent (null) in Failed`() {
        val result = interpret(mapOf("Status" to "FAILURE", "responseCode" to ""))
        val outcome = result as PaymentOutcome.Failed
        assertNull(outcome.responseCode)
    }

    @Test
    fun `all keys are matched case-insensitively`() {
        val result = interpret(
            mapOf(
                "STATUS" to "SUCCESS",
                "TXNID" to "T123",
                "RESPONSECODE" to "00",
                "APPROVALREFNO" to "R456"
            )
        )
        val outcome = result as PaymentOutcome.Success
        assertEquals("T123", outcome.txnId)
        assertEquals("00", outcome.responseCode)
        assertEquals("R456", outcome.approvalRefNo)
    }
}

