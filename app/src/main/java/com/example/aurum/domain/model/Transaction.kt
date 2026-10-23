package com.example.aurum.domain.model

import java.math.BigDecimal
import java.util.UUID

/**
 * Domain model representing a UPI payment transaction record.
 *
 * @property id Unique identifier for the transaction (UUID string).
 * @property vpa Virtual Payment Address of the payee (e.g. "merchant@upi").
 * @property payeeName Optional display name of the payee.
 * @property amountPaise Payment amount in paise (1 INR = 100 paise) to prevent floating-point inaccuracies.
 * @property note Optional transaction note / description.
 * @property qrTxnRef Optional reference ID extracted from the scanned QR code ('tr' parameter).
 * @property upiTxnId Optional transaction reference ID returned by the UPI app after completion.
 * @property appPackage Optional package name of the UPI application used for the payment.
 * @property status Current lifecycle status of the transaction.
 * @property createdAt Epoch timestamp in milliseconds when transaction was created.
 * @property updatedAt Epoch timestamp in milliseconds when transaction was last updated.
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val vpa: String,
    val payeeName: String? = null,
    val amountPaise: Long,
    val note: String? = null,
    val qrTxnRef: String? = null,
    val upiTxnId: String? = null,
    val appPackage: String? = null,
    val status: TransactionStatus = TransactionStatus.INITIATED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val amountRupees: BigDecimal
        get() = BigDecimal.valueOf(amountPaise, 2)
}
