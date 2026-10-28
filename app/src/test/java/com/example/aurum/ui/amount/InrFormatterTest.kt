package com.example.aurum.ui.amount

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class InrFormatterTest {

    @Test
    fun formatWithSymbol_formatsLakhsAndCroresCorrectly() {
        val oneLakh = BigDecimal("100000.00")
        assertEquals("₹1,00,000.00", InrFormatter.formatWithSymbol(oneLakh, includeDecimals = true))
        assertEquals("₹1,00,000", InrFormatter.formatWithSymbol(oneLakh, includeDecimals = false))

        val oneCrore = BigDecimal("10000000.50")
        assertEquals("₹1,00,00,000.50", InrFormatter.formatWithSymbol(oneCrore, includeDecimals = true))

        val smallAmount = BigDecimal("50.25")
        assertEquals("₹50.25", InrFormatter.formatWithSymbol(smallAmount, includeDecimals = true))
    }

    @Test
    fun formatPaiseWithSymbol_convertsPaiseToRupees() {
        assertEquals("₹1,00,000.00", InrFormatter.formatPaiseWithSymbol(10000000L, includeDecimals = true))
        assertEquals("₹500.00", InrFormatter.formatPaiseWithSymbol(50000L, includeDecimals = true))
        assertEquals("₹1.50", InrFormatter.formatPaiseWithSymbol(150L, includeDecimals = true))
    }

    @Test
    fun formatInputLive_formatsTypingGracefully() {
        assertEquals("₹0", InrFormatter.formatInputLive(""))
        assertEquals("₹5", InrFormatter.formatInputLive("5"))
        assertEquals("₹500", InrFormatter.formatInputLive("500"))
        assertEquals("₹1,000", InrFormatter.formatInputLive("1000"))
        assertEquals("₹1,00,000", InrFormatter.formatInputLive("100000"))
        assertEquals("₹1,00,000.", InrFormatter.formatInputLive("100000."))
        assertEquals("₹1,00,000.5", InrFormatter.formatInputLive("100000.5"))
        assertEquals("₹1,00,000.50", InrFormatter.formatInputLive("100000.50"))
    }
}

