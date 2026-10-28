package com.example.aurum.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ValidateAmountTest {

    private lateinit var validateAmount: ValidateAmount

    @Before
    fun setUp() {
        validateAmount = ValidateAmount()
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    fun `whole number is valid`() {
        val result = validateAmount("100")
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("100"), result.getOrThrow())
    }

    @Test
    fun `one decimal place is valid`() {
        val result = validateAmount("99.9")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `two decimal places is valid`() {
        val result = validateAmount("49.99")
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("49.99"), result.getOrThrow())
    }

    @Test
    fun `trailing zeros with 2 decimal places is valid`() {
        // "100.00" has scale 2 but stripTrailingZeros gives scale 0 -> still valid
        val result = validateAmount("100.00")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `minimum valid amount is 0_01`() {
        val result = validateAmount("0.01")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `maximum allowed amount (100000) is valid`() {
        val result = validateAmount("100000")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `amount with leading whitespace is valid`() {
        val result = validateAmount("  50.00  ")
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("50.00"), result.getOrThrow())
    }

    @Test
    fun `custom cap is respected when amount equals cap`() {
        val result = validateAmount("500", maxAmount = BigDecimal("500"))
        assertTrue(result.isSuccess)
    }

    // ── Failure: Empty ────────────────────────────────────────────────────────

    @Test
    fun `blank string returns Empty error`() {
        assertError("", AmountError.Empty)
    }

    @Test
    fun `whitespace-only string returns Empty error`() {
        assertError("   ", AmountError.Empty)
    }

    // ── Failure: NotANumber ───────────────────────────────────────────────────

    @Test
    fun `alphabetic input returns NotANumber`() {
        assertError("abc", AmountError.NotANumber)
    }

    @Test
    fun `alphanumeric input returns NotANumber`() {
        assertError("10abc", AmountError.NotANumber)
    }

    @Test
    fun `currency-prefixed input returns NotANumber`() {
        assertError("₹100", AmountError.NotANumber)
    }

    @Test
    fun `scientific notation returns NotANumber`() {
        assertError("1e2", AmountError.NotANumber)
        assertError("1E+4", AmountError.NotANumber)
    }

    @Test
    fun `leading plus sign returns NotANumber`() {
        assertError("+50", AmountError.NotANumber)
    }

    // ── Failure: NotPositive ──────────────────────────────────────────────────

    @Test
    fun `zero returns NotPositive`() {
        assertError("0", AmountError.NotPositive)
    }

    @Test
    fun `zero with decimals returns NotPositive`() {
        assertError("0.00", AmountError.NotPositive)
    }

    @Test
    fun `negative amount returns NotPositive`() {
        assertError("-10", AmountError.NotPositive)
    }

    @Test
    fun `negative decimal returns NotPositive`() {
        assertError("-0.01", AmountError.NotPositive)
    }

    // ── Failure: TooManyDecimals ──────────────────────────────────────────────

    @Test
    fun `three decimal places returns TooManyDecimals`() {
        assertError("10.123", AmountError.TooManyDecimals)
    }

    @Test
    fun `many decimal places returns TooManyDecimals`() {
        assertError("1.00001", AmountError.TooManyDecimals)
    }

    // ── Failure: ExceedsCap ───────────────────────────────────────────────────

    @Test
    fun `amount above default cap returns ExceedsCap`() {
        val result = validateAmount("100001")
        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as AmountValidationException).error
        assertTrue(error is AmountError.ExceedsCap)
    }

    @Test
    fun `amount above custom cap returns ExceedsCap`() {
        val result = validateAmount("1001", maxAmount = BigDecimal("1000"))
        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as AmountValidationException).error
        assertTrue(error is AmountError.ExceedsCap)
        assertEquals(BigDecimal("1000"), (error as AmountError.ExceedsCap).cap)
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun assertError(input: String, expected: AmountError) {
        val result = validateAmount(input)
        assertTrue("Expected failure but got success for: '$input'", result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is AmountValidationException)
        assertEquals(expected, (ex as AmountValidationException).error)
    }
}

