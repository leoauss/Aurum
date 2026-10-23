package com.example.aurum.domain.model

import java.math.BigDecimal

/**
 * Represents parsed UPI payment details extracted from a QR code or UPI deep link URI.
 *
 * @property vpa Virtual Payment Address (e.g., merchant@upi), mapped from 'pa' parameter.
 * @property payeeName Optional Payee Name, mapped from 'pn' parameter.
 * @property amount Optional payment amount in INR (Rupees), mapped from 'am' parameter.
 * @property note Optional transaction note / description, mapped from 'tn' parameter.
 * @property txnRef Optional transaction reference ID, mapped from 'tr' parameter.
 * @property rawUri The complete raw URI string scanned from the QR code.
 */
data class UpiPayload(
    val vpa: String,
    val payeeName: String? = null,
    val amount: BigDecimal? = null,
    val note: String? = null,
    val txnRef: String? = null,
    val rawUri: String
) {
    /**
     * Helper to get the amount converted to paise (1 INR = 100 paise), or null if amount is not specified.
     */
    val amountPaise: Long?
        get() = amount?.movePointRight(2)?.toLong()
}
