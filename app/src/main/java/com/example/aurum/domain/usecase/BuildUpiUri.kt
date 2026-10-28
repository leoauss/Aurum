package com.example.aurum.domain.usecase

import com.example.aurum.domain.model.UpiPayload
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class BuildUpiUri @Inject constructor() {

    operator fun invoke(payload: UpiPayload, userAmount: BigDecimal? = null): String {
        if (payload.amount != null) {
            return payload.rawUri
        }

        requireNotNull(userAmount) {
            "userAmount must be provided when the UPI payload contains no amount"
        }
        require(userAmount > BigDecimal.ZERO && userAmount.stripTrailingZeros().scale() <= 2) {
            "userAmount must be positive and have at most 2 decimal places, got: $userAmount"
        }

        val amountStr = userAmount.setScale(2, RoundingMode.UNNECESSARY).toPlainString()
        var baseUri = payload.rawUri.trim()

        // Strip any existing empty or zero amount (e.g. am=0 or am=0.00)
        baseUri = removeQueryParam(baseUri, "am")

        // Strip digital signature if present, as user-provided amount invalidates it
        baseUri = removeQueryParam(baseUri, "sign")

        // Strip static offline QR mode (mode=02) which triggers gallery QR restrictions in PhonePe
        baseUri = removeQueryParam(baseUri, "mode")

        val hasCurrency = CURRENCY_REGEX.containsMatchIn(baseUri)
        val separator = if (baseUri.contains('?')) "&" else "?"

        return buildString {
            append(baseUri)
            append(separator)
            append("am=").append(amountStr)
            if (!hasCurrency) {
                append("&cu=INR")
            }
            if (payload.isMerchant) {
                append("&mode=04")
            }
        }
    }

    private fun removeQueryParam(uri: String, paramKey: String): String {
        val regex = Regex("(?i)([?&])$paramKey=[^&]*(&)?")
        val replaced = uri.replace(regex) { matchResult ->
            val prefix = matchResult.groupValues[1]
            val suffix = matchResult.groupValues[2]
            if (prefix == "?" && suffix == "&") "?"
            else if (suffix == "&") "&"
            else ""
        }
        return replaced.trimEnd('?', '&')
    }

    companion object {
        private val CURRENCY_REGEX = Regex("(?i)[?&]cu=INR(&|$)")
    }
}
