package com.example.aurum.domain.model

sealed interface PaymentOutcome {
    /**
     * Payment was reported as successful by the UPI app.
     *
     * @property txnId UPI Transaction ID returned by the app.
     * @property approvalRefNo Bank / NPCI approval reference number (RRN).
     * @property responseCode UPI response code (e.g. "00" or "0").
     * @property rawResponse Full raw response query or intent extras string.
     */
    data class Success(
        val txnId: String? = null,
        val approvalRefNo: String? = null,
        val responseCode: String? = null,
        val rawResponse: String? = null
    ) : PaymentOutcome

    /**
     * Payment is pending or submitted for processing by the UPI app / bank.
     *
     * @property txnId UPI Transaction ID returned by the app.
     * @property responseCode UPI response code.
     * @property rawResponse Full raw response query or intent extras string.
     */
    data class Pending(
        val txnId: String? = null,
        val responseCode: String? = null,
        val rawResponse: String? = null
    ) : PaymentOutcome

    /**
     * Payment failed or was rejected by the UPI app / bank.
     *
     * @property responseCode UPI response error code (e.g. "ZM", "U19").
     * @property message Human-readable failure explanation or error text.
     * @property rawResponse Full raw response query or intent extras string.
     */
    data class Failed(
        val responseCode: String? = null,
        val message: String? = null,
        val rawResponse: String? = null
    ) : PaymentOutcome

    /**
     * Payment outcome could not be determined automatically (e.g., user cancelled,
     * app was killed, or returning intent had no response data).
     *
     * @property message Diagnostic explanation or user prompt.
     * @property rawResponse Full raw response query or intent extras string if any.
     */
    data class Unconfirmed(
        val message: String? = null,
        val rawResponse: String? = null
    ) : PaymentOutcome
}
