package com.example.aurum.domain.usecase

import com.example.aurum.domain.model.UpiPayload
import java.math.BigDecimal
import java.net.URI
import javax.inject.Inject

/**
 * Builds the final UPI payment URI to be launched in a payment intent.
 *
 * Rules:
 *  - If the [UpiPayload] already contains an amount (set by the merchant QR),
 *    the [UpiPayload.rawUri] is returned **unchanged** to preserve any merchant
 *    signature or additional parameters embedded in the original URI.
 *  - If the payload has no amount, [userAmount] must be provided.
 *    The use case appends `am=<value>` and `cu=INR` to the raw URI and returns
 *    the new URI string.
 *
 * @throws IllegalArgumentException if the payload has no amount and [userAmount]
 *   is null, zero, or negative.
 */
class BuildUpiUri @Inject constructor() {

    /**
     * @param payload   Parsed UPI payload from the QR scan.
     * @param userAmount Amount entered by the user (only used when the QR had no amount).
     * @return Final URI string ready to be used with [android.content.Intent.ACTION_VIEW].
     */
    operator fun invoke(payload: UpiPayload, userAmount: BigDecimal? = null): String {
        // If QR already embedded an amount, return the original URI unchanged.
        if (payload.amount != null) {
            return payload.rawUri
        }

        requireNotNull(userAmount) {
            "userAmount must be provided when the UPI payload contains no amount"
        }
        require(userAmount > BigDecimal.ZERO) {
            "userAmount must be positive, got: $userAmount"
        }

        // Format: no trailing zeros, but always at least 2 decimal places for UPI spec compliance.
        val amountStr = userAmount.setScale(2).toPlainString()

        // Append parameters to the existing URI.
        // Use a simple string approach so we never corrupt an already-encoded URI.
        val separator = if (payload.rawUri.contains('?')) "&" else "?"
        return "${payload.rawUri}${separator}am=${amountStr}&cu=INR"
    }
}

