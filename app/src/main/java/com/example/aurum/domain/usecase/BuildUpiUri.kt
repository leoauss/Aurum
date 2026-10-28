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
        val baseUri = payload.rawUri.trim()
        val separator = if (baseUri.contains('?')) "&" else "?"
        val hasCurrency = CURRENCY_REGEX.containsMatchIn(baseUri)

        return buildString {
            append(baseUri)
            append(separator)
            append("am=").append(amountStr)
            if (!hasCurrency) {
                append("&cu=INR")
            }
        }
    }

    companion object {
        private val CURRENCY_REGEX = Regex("(?i)[?&]cu=INR(&|$)")
    }
}