package com.example.aurum.domain.usecase

import com.example.aurum.domain.model.PaymentOutcome
import javax.inject.Inject

/**
 * Interprets the response from a UPI payment intent and maps it to a [PaymentOutcome].
 *
 * UPI apps communicate result via the response Intent's data URI query string.
 * The `response` parameter (if present) holds a URL-encoded key-value string like:
 *   `Status=SUCCESS&txnId=ABC123&responseCode=00&ApprovalRefNo=RRN001`
 *
 * Alternatively, individual extras may be present on the intent bundle.
 *
 * This use case accepts a pre-flattened [Map] of key→value pairs so it remains
 * a pure Kotlin function testable on the JVM (no Android `Intent` dependency).
 *
 * Status → Outcome mapping (case-insensitive):
 *  - "SUCCESS"            → [PaymentOutcome.Success]
 *  - "FAILURE" / "FAILED" → [PaymentOutcome.Failed]
 *  - "PENDING" / "SUBMITTED" → [PaymentOutcome.Pending]
 *  - anything else / absent  → [PaymentOutcome.Unconfirmed]
 */
class InterpretPaymentResult @Inject constructor() {

    /**
     * @param extras A flat map of key→value pairs extracted from the UPI response.
     *   Callers in the Android layer should merge the data URI query params and the
     *   intent bundle extras into this map before invoking this use case.
     */
    operator fun invoke(extras: Map<String, String?>): PaymentOutcome {
        // Build a case-insensitive lookup
        val params = extras
            .entries
            .filter { it.key.isNotBlank() }
            .associate { it.key.lowercase() to it.value?.trim() }

        val rawResponse = params.entries.joinToString("&") { "${it.key}=${it.value}" }

        val status = params["status"]?.uppercase()
        val txnId = params["txnid"]?.takeIf { it.isNotBlank() }
        val responseCode = params["responsecode"]?.takeIf { it.isNotBlank() }
        val approvalRefNo = params["approvalrefno"]?.takeIf { it.isNotBlank() }

        return when (status) {
            "SUCCESS" -> PaymentOutcome.Success(
                txnId = txnId,
                approvalRefNo = approvalRefNo,
                responseCode = responseCode,
                rawResponse = rawResponse
            )

            "FAILURE", "FAILED" -> PaymentOutcome.Failed(
                responseCode = responseCode,
                message = params["message"]?.takeIf { it.isNotBlank() },
                rawResponse = rawResponse
            )

            "PENDING", "SUBMITTED" -> PaymentOutcome.Pending(
                txnId = txnId,
                responseCode = responseCode,
                rawResponse = rawResponse
            )

            else -> PaymentOutcome.Unconfirmed(
                message = if (status != null) "Unknown status: $status" else "No status in response",
                rawResponse = rawResponse
            )
        }
    }
}

