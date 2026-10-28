package com.example.aurum.ui.scan

import com.example.aurum.domain.model.PaymentOutcome
import com.example.aurum.domain.model.ScanError
import com.example.aurum.domain.model.Transaction
import com.example.aurum.domain.model.UpiApp
import com.example.aurum.domain.model.UpiPayload
import java.math.BigDecimal

/**
 * Sealed interface representing the complete UI and session state
 * across the Aurum UPI scan-and-pay user flow.
 */
sealed interface PayUiState {

    /**
     * Camera scanner is active and analyzing video feed for valid UPI QR codes.
     */
    data object Scanning : PayUiState

    /**
     * QR code scanned and validated; user must review payee name and VPA before proceeding.
     */
    data class Confirm(
        val payload: UpiPayload
    ) : PayUiState

    /**
     * QR code has no preset amount (or am=0); user is entering the amount via custom keypad.
     */
    data class EnterAmount(
        val payload: UpiPayload,
        val input: String = "",
        val error: String? = null
    ) : PayUiState

    /**
     * Payee and amount confirmed; user selects which installed UPI application to pay with.
     */
    data class ChooseApp(
        val payload: UpiPayload,
        val amount: BigDecimal,
        val apps: List<UpiApp>,
        val lastUsedPackage: String? = null
    ) : PayUiState

    /**
     * Payment Intent has been handed over to the external UPI app; awaiting user return.
     */
    data class AwaitingResult(
        val txnId: String,
        val payload: UpiPayload,
        val amount: BigDecimal,
        val selectedApp: UpiApp
    ) : PayUiState

    /**
     * Payment finished; displays verified or unconfirmed outcome to user.
     */
    data class Result(
        val outcome: PaymentOutcome,
        val transaction: Transaction? = null
    ) : PayUiState

    /**
     * An error occurred during QR scanning or image decoding.
     */
    data class Error(
        val error: ScanError? = null,
        val message: String
    ) : PayUiState

    /**
     * Cold start reconciliation: An in-flight transaction was left in INITIATED state before process death.
     */
    data class ReconcilePending(
        val transaction: Transaction
    ) : PayUiState
}

