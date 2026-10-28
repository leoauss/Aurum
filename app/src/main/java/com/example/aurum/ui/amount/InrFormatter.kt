package com.example.aurum.ui.amount

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility for formatting currency values according to the Indian Numbering System
 * (lakhs, crores) with the ₹ (INR) symbol.
 *
 * Guaranteed deterministic output across all Android API levels and JVM targets.
 */
object InrFormatter {

    /**
     * Formats an integer string according to the Indian numbering system:
     * - Last 3 digits grouped: XXX
     * - Preceding digits grouped in pairs of 2: XX,XX,XXX
     */
    fun formatIndianNumber(intStr: String): String {
        val clean = intStr.trimStart('0')
        if (clean.isEmpty()) return "0"
        if (clean.length <= 3) return clean

        val last3 = clean.takeLast(3)
        val remaining = clean.dropLast(3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count == 2 && i != 0) {
                sb.append(',')
                count = 0
            }
        }
        val formattedRemaining = sb.reverse().toString()
        return "$formattedRemaining,$last3"
    }

    /**
     * Formats a [BigDecimal] amount with ₹ symbol and Indian grouping.
     * e.g., BigDecimal("100000") -> "₹1,00,000.00"
     */
    fun formatWithSymbol(amount: BigDecimal, includeDecimals: Boolean = true): String {
        val scaled = amount.setScale(2, RoundingMode.HALF_UP)
        val plain = scaled.toPlainString()
        val parts = plain.split('.')
        val intPart = formatIndianNumber(parts[0])

        return if (includeDecimals) {
            "₹$intPart.${parts[1]}"
        } else {
            "₹$intPart"
        }
    }

    /**
     * Formats amount given in paise (1 INR = 100 paise) with ₹ symbol.
     */
    fun formatPaiseWithSymbol(paise: Long, includeDecimals: Boolean = true): String {
        val rupees = BigDecimal.valueOf(paise, 2)
        return formatWithSymbol(rupees, includeDecimals)
    }

    /**
     * Formats dynamic keypad input while typing:
     * - "" -> "₹0"
     * - "100" -> "₹100"
     * - "100000" -> "₹1,00,000"
     * - "100000." -> "₹1,00,000."
     * - "100000.5" -> "₹1,00,000.5"
     */
    fun formatInputLive(input: String): String {
        if (input.isBlank()) return "₹0"

        val hasDot = input.contains('.')
        val parts = input.split('.')
        val intPart = formatIndianNumber(parts[0])
        val decimalPart = if (parts.size > 1) parts[1] else null

        return buildString {
            append("₹")
            append(intPart)
            if (hasDot) {
                append(".")
                if (decimalPart != null) {
                    append(decimalPart)
                }
            }
        }
    }
}
