package com.example.aurum.domain.usecase

import com.example.aurum.domain.model.ScanError
import com.example.aurum.domain.model.ScanException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ParseUpiQrTest {

    private lateinit var parseUpiQr: ParseUpiQr

    @Before
    fun setUp() {
        parseUpiQr = ParseUpiQr()
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    fun `valid minimal QR with only VPA succeeds`() {
        val result = parseUpiQr("upi://pay?pa=merchant@upi")
        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("merchant@upi", payload.vpa)
        assertNull(payload.amount)
        assertNull(payload.payeeName)
        assertNull(payload.note)
        assertNull(payload.txnRef)
    }

    @Test
    fun `valid merchant QR with all fields succeeds`() {
        val result = parseUpiQr(
            "upi://pay?pa=merchant@upi&pn=Merchant%20Name&am=99.50&cu=INR&tn=Test%20Payment&tr=TXN123"
        )
        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("merchant@upi", payload.vpa)
        assertEquals("Merchant Name", payload.payeeName)
        assertEquals(BigDecimal("99.50"), payload.amount)
        assertEquals("Test Payment", payload.note)
        assertEquals("TXN123", payload.txnRef)
    }

    @Test
    fun `scheme is case-insensitive`() {
        val result = parseUpiQr("UPI://PAY?pa=user@oksbi")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `currency absent is treated as INR`() {
        val result = parseUpiQr("upi://pay?pa=user@okicici&am=10.00")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `currency INR (case-insensitive) is accepted`() {
        val result = parseUpiQr("upi://pay?pa=user@okicici&am=10.00&cu=inr")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `URL-encoded payee name and note are decoded correctly`() {
        val result = parseUpiQr("upi://pay?pa=shop@paytm&pn=My%20Shop%20%26%20More&tn=For%20goods")
        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("My Shop & More", payload.payeeName)
        assertEquals("For goods", payload.note)
    }

    @Test
    fun `extra unknown parameters are ignored and parse succeeds`() {
        val result = parseUpiQr("upi://pay?pa=user@upi&unknown=xyz&foo=bar")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `amount with exactly 2 decimal places is valid`() {
        val result = parseUpiQr("upi://pay?pa=user@upi&am=100.99")
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("100.99"), result.getOrThrow().amount)
    }

    @Test
    fun `amount with 1 decimal place is valid`() {
        val result = parseUpiQr("upi://pay?pa=user@upi&am=100.9")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `amount with no decimal places is valid`() {
        val result = parseUpiQr("upi://pay?pa=user@upi&am=500")
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("500"), result.getOrThrow().amount)
    }

    @Test
    fun `rawUri is preserved from input`() {
        val raw = "upi://pay?pa=user@upi"
        val result = parseUpiQr(raw)
        assertEquals(raw, result.getOrThrow().rawUri)
    }

    @Test
    fun `leading and trailing whitespace is stripped`() {
        val result = parseUpiQr("  upi://pay?pa=user@upi  ")
        assertTrue(result.isSuccess)
    }

    // ── Failure: NotUpiQr ─────────────────────────────────────────────────────

    @Test
    fun `empty string returns NotUpiQr`() {
        assertScanError("", ScanError.NotUpiQr)
    }

    @Test
    fun `random text returns NotUpiQr`() {
        assertScanError("hello world", ScanError.NotUpiQr)
    }

    @Test
    fun `HTTP URL returns NotUpiQr`() {
        assertScanError("https://example.com/pay?pa=user@upi", ScanError.NotUpiQr)
    }

    @Test
    fun `wrong scheme returns NotUpiQr`() {
        assertScanError("upx://pay?pa=user@upi", ScanError.NotUpiQr)
    }

    @Test
    fun `wrong host returns NotUpiQr`() {
        assertScanError("upi://request?pa=user@upi", ScanError.NotUpiQr)
    }

    // ── Failure: InvalidVpa ───────────────────────────────────────────────────

    @Test
    fun `missing pa parameter returns InvalidVpa`() {
        assertScanError("upi://pay?pn=SomeName&am=10.00", ScanError.InvalidVpa)
    }

    @Test
    fun `empty pa parameter returns InvalidVpa`() {
        assertScanError("upi://pay?pa=", ScanError.InvalidVpa)
    }

    @Test
    fun `VPA with no @ symbol returns InvalidVpa`() {
        assertScanError("upi://pay?pa=invalidsyntax", ScanError.InvalidVpa)
    }

    @Test
    fun `VPA with handle shorter than 2 chars returns InvalidVpa`() {
        assertScanError("upi://pay?pa=user@x", ScanError.InvalidVpa)
    }

    @Test
    fun `VPA with numeric handle returns InvalidVpa`() {
        assertScanError("upi://pay?pa=user@123", ScanError.InvalidVpa)
    }

    @Test
    fun `VPA local part shorter than 2 chars returns InvalidVpa`() {
        assertScanError("upi://pay?pa=a@upi", ScanError.InvalidVpa)
    }

    // ── Failure: UnsupportedCurrency ──────────────────────────────────────────

    @Test
    fun `non-INR currency returns UnsupportedCurrency`() {
        assertScanError("upi://pay?pa=user@upi&am=10.00&cu=USD", ScanError.UnsupportedCurrency)
    }

    @Test
    fun `EUR currency returns UnsupportedCurrency`() {
        assertScanError("upi://pay?pa=user@upi&am=10.00&cu=EUR", ScanError.UnsupportedCurrency)
    }

    // ── Failure: InvalidAmount ────────────────────────────────────────────────

    @Test
    fun `non-numeric amount returns InvalidAmount`() {
        assertScanError("upi://pay?pa=user@upi&am=abc", ScanError.InvalidAmount)
    }

    @Test
    fun `negative amount returns InvalidAmount`() {
        assertScanError("upi://pay?pa=user@upi&am=-10.00", ScanError.InvalidAmount)
    }

    @Test
    fun `zero amount returns InvalidAmount`() {
        assertScanError("upi://pay?pa=user@upi&am=0", ScanError.InvalidAmount)
    }

    @Test
    fun `amount with more than 2 decimal places returns InvalidAmount`() {
        assertScanError("upi://pay?pa=user@upi&am=10.123", ScanError.InvalidAmount)
    }

    @Test
    fun `amount exceeding max cap returns InvalidAmount`() {
        // Max cap is 1,000,000
        assertScanError("upi://pay?pa=user@upi&am=1000001", ScanError.InvalidAmount)
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `control characters in VPA are stripped leading to InvalidVpa`() {
        // Inserting a control char makes the VPA empty after sanitization
        assertScanError("upi://pay?pa=\u0000@upi", ScanError.InvalidVpa)
    }

    @Test
    fun `amountPaise helper is correct`() {
        val result = parseUpiQr("upi://pay?pa=user@upi&am=99.50")
        assertEquals(9950L, result.getOrThrow().amountPaise)
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun assertScanError(input: String, expected: ScanError) {
        val result = parseUpiQr(input)
        assertTrue("Expected failure but got success for: $input", result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is ScanException)
        assertEquals(expected, (ex as ScanException).error)
    }
}

