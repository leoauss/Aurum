package com.example.aurum.domain.usecase

import com.example.aurum.domain.model.ScanError
import com.example.aurum.domain.model.ScanException
import com.example.aurum.domain.model.UpiPayload
import java.math.BigDecimal
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class ParseUpiQr @Inject constructor() {

    operator fun invoke(rawQr: String): Result<UpiPayload> {
        val trimmed = rawQr.trim()
        if (trimmed.isEmpty()) {
            return failure(ScanError.NotUpiQr)
        }

        val sanitized = sanitizeControlChars(trimmed)
        val uriSafeString = sanitized.replace(" ", "%20")
        val uri = try {
            URI(uriSafeString)
        } catch (_: Exception) {
            return failure(ScanError.NotUpiQr)
        }

        if (!uri.scheme.equals(EXPECTED_SCHEME, ignoreCase = true) ||
            !uri.host.equals(EXPECTED_HOST, ignoreCase = true)
        ) {
            return failure(ScanError.NotUpiQr)
        }

        val rawQuery = uri.rawQuery
        if (rawQuery.isNullOrBlank()) {
            return failure(ScanError.InvalidVpa)
        }

        val params = parseQueryParameters(rawQuery)

        // 1. Validate VPA ('pa')
        val vpa = params["pa"]?.let(::sanitizeControlChars)?.trim()
        if (vpa.isNullOrEmpty() || !VPA_REGEX.matches(vpa)) {
            return failure(ScanError.InvalidVpa)
        }

        // 2. Validate Currency ('cu') — must be INR or absent
        val currency = params["cu"]?.let(::sanitizeControlChars)?.trim()
        if (!currency.isNullOrEmpty() && !currency.equals(SUPPORTED_CURRENCY, ignoreCase = true)) {
            return failure(ScanError.UnsupportedCurrency)
        }

        // 3. Validate Amount ('am') — optional.
        // If am=0 or am=0.00, treat as null (open/dynamic amount for Indian POS QR codes).
        // If present and non-zero: must be > 0, <= 2 significant decimal places, <= max cap.
        val rawAmount = params["am"]?.let(::sanitizeControlChars)?.trim()
        val amount: BigDecimal? = if (!rawAmount.isNullOrEmpty()) {
            val parsed = rawAmount.toBigDecimalOrNull()
                ?: return failure(ScanError.InvalidAmount)

            if (parsed.compareTo(BigDecimal.ZERO) == 0) {
                null
            } else if (parsed < BigDecimal.ZERO ||
                parsed.stripTrailingZeros().scale() > 2 ||
                parsed > MAX_QR_AMOUNT
            ) {
                return failure(ScanError.InvalidAmount)
            } else {
                parsed
            }
        } else {
            null
        }

        val payeeName = params["pn"]
            ?.let(::sanitizeControlChars)
            ?.trim()
            ?.take(MAX_NAME_LENGTH)
            ?.takeIf { it.isNotEmpty() }

        val note = params["tn"]
            ?.let(::sanitizeControlChars)
            ?.trim()
            ?.take(MAX_NOTE_LENGTH)
            ?.takeIf { it.isNotEmpty() }

        val txnRef = params["tr"]
            ?.let(::sanitizeControlChars)
            ?.trim()
            ?.take(MAX_TXN_REF_LENGTH)
            ?.takeIf { it.isNotEmpty() }

        return Result.success(
            UpiPayload(
                vpa = vpa,
                payeeName = payeeName,
                amount = amount,
                note = note,
                txnRef = txnRef,
                rawUri = trimmed
            )
        )
    }

    private fun parseQueryParameters(rawQuery: String): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        for (pair in rawQuery.split('&')) {
            if (pair.isBlank()) continue
            val idx = pair.indexOf('=')
            val rawKey = if (idx >= 0) pair.substring(0, idx) else pair
            val rawValue = if (idx >= 0) pair.substring(idx + 1) else ""

            val key = urlDecode(rawKey).lowercase()
            val value = if (key == "pa") {
                try {
                    URLDecoder.decode(rawValue.replace("+", "%2B"), StandardCharsets.UTF_8.name())
                } catch (_: Exception) {
                    rawValue
                }
            } else {
                urlDecode(rawValue)
            }
            if (key.isNotEmpty() && !map.containsKey(key)) {
                map[key] = value
            }
        }
        return map
    }

    private fun urlDecode(value: String): String = try {
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())
    } catch (_: IllegalArgumentException) {
        value
    }

    private fun sanitizeControlChars(input: String): String =
        input.replace(CONTROL_CHARS_REGEX, "")

    private fun failure(error: ScanError): Result<Nothing> =
        Result.failure(ScanException(error))

    companion object {
        private const val EXPECTED_SCHEME = "upi"
        private const val EXPECTED_HOST = "pay"
        private const val SUPPORTED_CURRENCY = "INR"
        private const val MAX_NAME_LENGTH = 100
        private const val MAX_NOTE_LENGTH = 100
        private const val MAX_TXN_REF_LENGTH = 64
        private val MAX_QR_AMOUNT = BigDecimal("1000000")
        private val VPA_REGEX = Regex("^[a-zA-Z0-9._-]{2,256}@[a-zA-Z]{2,64}$")
        private val CONTROL_CHARS_REGEX = Regex("[\\p{Cntrl}]")
    }
}