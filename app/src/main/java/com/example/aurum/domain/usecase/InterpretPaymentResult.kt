package com.example.aurum.domain.usecase

import com.example.aurum.domain.model.PaymentOutcome
import javax.inject.Inject

class InterpretPaymentResult @Inject constructor() {

    operator fun invoke(extras: Map<String, String?>): PaymentOutcome {
        val params = LinkedHashMap<String, String>()

        for ((k, v) in extras) {
            if (k.isNotBlank() && !v.isNullOrBlank()) {
                params[k.trim().lowercase()] = v.trim()
            }
        }

        // Automatically unpack the standard "response" query string if present
        params["response"]?.let { rawQuery ->
            for (pair in rawQuery.split('&')) {
                val idx = pair.indexOf('=')
                if (idx > 0) {
                    val key = pair.substring(0, idx).trim().lowercase()
                    val value = pair.substring(idx + 1).trim()
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        params[key] = value
                    }
                }
            }
        }

        val rawResponse = params.entries.joinToString("&") { "${it.key}=${it.value}" }
            .takeIf { it.isNotEmpty() }

        val status = params["status"]?.uppercase()
        val txnId = params["txnid"]. sanitizeNullLiteral()
        val responseCode = params["responsecode"].sanitizeNullLiteral()
        val approvalRefNo = (params["approvalrefno"] ?: params["txnref"]).sanitizeNullLiteral()

        return when (status) {
            "SUCCESS", "S" -> PaymentOutcome.Success(
                txnId = txnId,
                approvalRefNo = approvalRefNo,
                responseCode = responseCode,
                rawResponse = rawResponse
            )
            "FAILURE", "FAILED", "F" -> PaymentOutcome.Failed(
                responseCode = responseCode,
                message = params["message"].sanitizeNullLiteral(),
                rawResponse = rawResponse
            )
            "PENDING", "SUBMITTED", "P" -> PaymentOutcome.Pending(
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

    private fun String?.sanitizeNullLiteral(): String? =
        this?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) && !it.equals("na", ignoreCase = true) }
}