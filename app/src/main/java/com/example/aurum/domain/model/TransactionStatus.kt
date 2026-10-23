package com.example.aurum.domain.model

/**
 * Lifecycle status of a UPI transaction.
 */
enum class TransactionStatus {
    /** Payment intent initiated and launched, awaiting result. */
    INITIATED,

    /** Payment confirmed successful by the UPI app. */
    SUCCESS,

    /** Payment submitted/pending in the UPI app or banking network. */
    PENDING,

    /** Payment reported failed or declined. */
    FAILED,

    /** App returned without clear confirmation or was interrupted. */
    UNCONFIRMED,

    /** User manually confirmed that the payment went through outside the app. */
    USER_CONFIRMED_PAID
}
