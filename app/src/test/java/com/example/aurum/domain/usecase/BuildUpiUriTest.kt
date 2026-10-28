package com.example.aurum.domain.usecase

import com.example.aurum.domain.model.UpiPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class BuildUpiUriTest {

    private lateinit var buildUpiUri: BuildUpiUri

    @Before
    fun setUp() {
        buildUpiUri = BuildUpiUri()
    }

    // ── QR already had amount ─────────────────────────────────────────────────

    @Test
    fun `when QR has amount, rawUri is returned unchanged`() {
        val rawUri = "upi://pay?pa=merchant@upi&pn=Shop&am=150.00&cu=INR&tr=REF001"
        val payload = UpiPayload(
            vpa = "merchant@upi",
            payeeName = "Shop",
            amount = BigDecimal("150.00"),
            txnRef = "REF001",
            rawUri = rawUri
        )

        val result = buildUpiUri(payload, userAmount = BigDecimal("200")) // userAmount ignored
        assertEquals(rawUri, result)
    }

    @Test
    fun `when QR has amount, userAmount parameter is completely ignored`() {
        val rawUri = "upi://pay?pa=shop@paytm&am=75.50"
        val payload = UpiPayload(
            vpa = "shop@paytm",
            amount = BigDecimal("75.50"),
            rawUri = rawUri
        )

        val result = buildUpiUri(payload, userAmount = null)
        assertEquals(rawUri, result)
    }

    // ── QR had no amount — userAmount is appended ─────────────────────────────

    @Test
    fun `when QR has no amount, am and cu are appended with ampersand`() {
        val rawUri = "upi://pay?pa=user@oksbi&pn=Test"
        val payload = UpiPayload(vpa = "user@oksbi", payeeName = "Test", rawUri = rawUri)

        val result = buildUpiUri(payload, userAmount = BigDecimal("250"))
        assertEquals("upi://pay?pa=user@oksbi&pn=Test&am=250.00&cu=INR", result)
    }

    @Test
    fun `amount is formatted with exactly 2 decimal places`() {
        val rawUri = "upi://pay?pa=user@upi"
        val payload = UpiPayload(vpa = "user@upi", rawUri = rawUri)

        val result = buildUpiUri(payload, userAmount = BigDecimal("100"))
        assertTrue(result.contains("am=100.00"))
    }

    @Test
    fun `decimal amount is preserved correctly`() {
        val rawUri = "upi://pay?pa=user@upi"
        val payload = UpiPayload(vpa = "user@upi", rawUri = rawUri)

        val result = buildUpiUri(payload, userAmount = BigDecimal("99.50"))
        assertTrue(result.contains("am=99.50"))
    }

    @Test
    fun `cu=INR is appended when not present`() {
        val rawUri = "upi://pay?pa=user@upi"
        val payload = UpiPayload(vpa = "user@upi", rawUri = rawUri)

        val result = buildUpiUri(payload, userAmount = BigDecimal("50"))
        assertTrue(result.contains("cu=INR"))
    }

    @Test
    fun `cu=INR is not duplicated when already present in rawUri`() {
        val rawUri = "upi://pay?pa=merchant@upi&pn=Shop&cu=INR"
        val payload = UpiPayload(vpa = "merchant@upi", payeeName = "Shop", rawUri = rawUri)

        val result = buildUpiUri(payload, userAmount = BigDecimal("100"))
        assertEquals("upi://pay?pa=merchant@upi&pn=Shop&cu=INR&am=100.00", result)
    }

    @Test
    fun `amount with trailing zeros is formatted without arithmetic exception`() {
        val rawUri = "upi://pay?pa=merchant@upi"
        val payload = UpiPayload(vpa = "merchant@upi", rawUri = rawUri)

        val result = buildUpiUri(payload, userAmount = BigDecimal("50.000"))
        assertEquals("upi://pay?pa=merchant@upi&am=50.00&cu=INR", result)
    }

    @Test
    fun `resulting URI is parseable as a valid URI`() {
        val rawUri = "upi://pay?pa=merchant@icici"
        val payload = UpiPayload(vpa = "merchant@icici", rawUri = rawUri)

        val result = buildUpiUri(payload, userAmount = BigDecimal("1000"))

        // Should not throw
        val uri = java.net.URI(result)
        assertEquals("upi", uri.scheme)
    }

    // ── Error cases ───────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `when QR has no amount and userAmount is null, throws`() {
        val payload = UpiPayload(vpa = "user@upi", rawUri = "upi://pay?pa=user@upi")
        buildUpiUri(payload, userAmount = null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when QR has no amount and userAmount is zero, throws`() {
        val payload = UpiPayload(vpa = "user@upi", rawUri = "upi://pay?pa=user@upi")
        buildUpiUri(payload, userAmount = BigDecimal.ZERO)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when QR has no amount and userAmount is negative, throws`() {
        val payload = UpiPayload(vpa = "user@upi", rawUri = "upi://pay?pa=user@upi")
        buildUpiUri(payload, userAmount = BigDecimal("-10"))
    }
}

