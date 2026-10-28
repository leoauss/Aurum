package com.example.aurum.ui.scan

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aurum.domain.model.PaymentOutcome
import com.example.aurum.domain.model.ScanError
import com.example.aurum.domain.model.ScanException
import com.example.aurum.domain.model.Transaction
import com.example.aurum.domain.model.TransactionStatus
import com.example.aurum.domain.model.UpiApp
import com.example.aurum.domain.model.UpiPayload
import com.example.aurum.domain.repository.PreferencesRepository
import com.example.aurum.domain.repository.TransactionRepository
import com.example.aurum.domain.usecase.BuildUpiUri
import com.example.aurum.domain.usecase.GetInstalledUpiApps
import com.example.aurum.domain.usecase.InterpretPaymentResult
import com.example.aurum.domain.usecase.ParseUpiQr
import com.example.aurum.domain.usecase.ValidateAmount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentFlowViewModel @Inject constructor(
    private val parseUpiQr: ParseUpiQr,
    private val validateAmount: ValidateAmount,
    private val buildUpiUri: BuildUpiUri,
    private val interpretPaymentResult: InterpretPaymentResult,
    private val getInstalledUpiApps: GetInstalledUpiApps,
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: PreferencesRepository,
    private val galleryQrDecoder: GalleryQrDecoder
) : ViewModel() {

    private val _uiState = MutableStateFlow<PayUiState>(PayUiState.Scanning)
    val uiState: StateFlow<PayUiState> = _uiState.asStateFlow()

    var activePayload: UpiPayload? = null
        private set

    var activeAmount: BigDecimal? = null
        private set

    var activeTxnId: String? = null
        private set

    private var isLaunchingPayment = false

    init {
        checkPendingReconciliation()
    }

    /**
     * Checks if there were any unfinished INITIATED transactions from a prior process death.
     */
    fun checkPendingReconciliation() {
        viewModelScope.launch {
            val initiated = transactionRepository.getInitiatedTransactions()
            if (initiated.isNotEmpty()) {
                val latest = initiated.first()
                _uiState.value = PayUiState.ReconcilePending(latest)
            }
        }
    }

    /**
     * Handles raw QR text detected from camera or gallery.
     * Returns true if successfully parsed and valid.
     */
    fun onQrScanned(rawQr: String): Boolean {
        val result = parseUpiQr(rawQr)
        return result.fold(
            onSuccess = { payload ->
                activePayload = payload
                activeAmount = payload.amount
                _uiState.value = PayUiState.Confirm(payload)
                true
            },
            onFailure = { error ->
                val scanError = (error as? ScanException)?.error
                val message = when (scanError) {
                    ScanError.NotUpiQr -> "Not a valid UPI QR code."
                    ScanError.InvalidVpa -> "Invalid UPI ID format in QR code."
                    ScanError.UnsupportedCurrency -> "Only INR currency is supported."
                    ScanError.InvalidAmount -> "Invalid payment amount specified in QR."
                    null -> error.localizedMessage ?: "Failed to read QR code."
                }
                _uiState.value = PayUiState.Error(scanError, message)
                false
            }
        )
    }

    /**
     * Decodes and parses a QR code from a gallery image URI.
     */
    fun onGalleryImageSelected(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            val decodeResult = galleryQrDecoder.decode(context, imageUri)
            decodeResult.fold(
                onSuccess = { rawText -> onQrScanned(rawText) },
                onFailure = { error ->
                    _uiState.value = PayUiState.Error(
                        error = null,
                        message = error.localizedMessage ?: "Could not find a valid UPI QR in the selected image."
                    )
                }
            )
        }
    }

    /**
     * Moves from Payee confirmation to either EnterAmount or ChooseApp.
     */
    fun onConfirmContinue() {
        val payload = activePayload ?: return
        if (payload.amount != null) {
            activeAmount = payload.amount
            loadAppsAndPresentPicker(payload, payload.amount)
        } else {
            _uiState.value = PayUiState.EnterAmount(payload = payload, input = "", error = null)
        }
    }

    /**
     * Updates the amount input string and performs real-time validation.
     */
    fun onAmountInputChanged(newInput: String) {
        val payload = activePayload ?: return
        val validation = validateAmount(newInput)
        val errorMessage = validation.fold(
            onSuccess = { null },
            onFailure = { it.message }
        )
        _uiState.value = PayUiState.EnterAmount(payload = payload, input = newInput, error = errorMessage)
    }

    /**
     * Adds preset rupee denominations (+100, +500, +1000, +2000) to current amount.
     */
    fun onQuickAdd(quickAmount: Long) {
        val currentState = _uiState.value as? PayUiState.EnterAmount ?: return
        val currentInput = currentState.input
        val currentBd = currentInput.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
        val newBd = currentBd.add(java.math.BigDecimal.valueOf(quickAmount))
        onAmountInputChanged(newBd.stripTrailingZeros().toPlainString())
    }

    /**
     * Handles custom numeric keypad interactions for amount entry.
     */
    fun onKeypadKeyPressed(key: String) {
        val currentState = _uiState.value as? PayUiState.EnterAmount ?: return
        val currentInput = currentState.input

        val updatedInput = when (key) {
            "DEL" -> if (currentInput.isNotEmpty()) currentInput.dropLast(1) else ""
            "CLEAR" -> ""
            "." -> {
                if (currentInput.isEmpty()) {
                    "0."
                } else if (!currentInput.contains(".")) {
                    "$currentInput."
                } else {
                    currentInput
                }
            }
            else -> {
                // Digits 0-9
                if (currentInput == "0" && key != ".") {
                    key
                } else if (currentInput.contains(".")) {
                    val parts = currentInput.split(".")
                    if (parts.size > 1 && parts[1].length >= 2) {
                        currentInput // Cap at 2 decimal places
                    } else {
                        currentInput + key
                    }
                } else {
                    if (currentInput.length >= 7) currentInput else currentInput + key
                }
            }
        }
        onAmountInputChanged(updatedInput)
    }

    /**
     * Confirms the entered amount and transitions to the UPI app picker.
     */
    fun onAmountConfirmed(): Boolean {
        val currentState = _uiState.value as? PayUiState.EnterAmount ?: return false
        val payload = currentState.payload
        val validation = validateAmount(currentState.input)

        return validation.fold(
            onSuccess = { validAmount ->
                activeAmount = validAmount
                loadAppsAndPresentPicker(payload, validAmount)
                true
            },
            onFailure = { err ->
                _uiState.value = currentState.copy(error = err.message ?: "Invalid amount")
                false
            }
        )
    }

    /**
     * Loads installed UPI applications and presents the picker sheet.
     */
    fun loadAppsAndPresentPicker(payload: UpiPayload, amount: BigDecimal) {
        viewModelScope.launch {
            val apps = getInstalledUpiApps()
            val lastUsed = preferencesRepository.lastUsedAppCached
                ?: preferencesRepository.getLastUsedUpiApp().firstOrNull()

            _uiState.value = PayUiState.ChooseApp(
                payload = payload,
                amount = amount,
                apps = apps,
                lastUsedPackage = lastUsed
            )
        }
    }

    /**
     * Builds UPI intent, records INITIATED transaction in Room, and dispatches intent launch.
     */
    fun initiatePayment(app: UpiApp, onLaunch: (Intent) -> Unit) {
        if (isLaunchingPayment) return
        isLaunchingPayment = true

        val payload = activePayload ?: return
        val amount = activeAmount ?: return

        viewModelScope.launch {
            try {
                preferencesRepository.setLastUsedUpiApp(app.packageName)

                val txnId = UUID.randomUUID().toString()
                activeTxnId = txnId

                val transaction = Transaction(
                    id = txnId,
                    vpa = payload.vpa,
                    payeeName = payload.payeeName,
                    amountPaise = amount.movePointRight(2).longValueExact(),
                    note = payload.note,
                    qrTxnRef = payload.txnRef,
                    appPackage = app.packageName,
                    status = TransactionStatus.INITIATED
                )
                transactionRepository.insert(transaction)

                val uriString = buildUpiUri(
                    payload = payload,
                    userAmount = if (payload.amount == null) amount else null
                )

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                    setPackage(app.packageName)
                }

                _uiState.value = PayUiState.AwaitingResult(
                    txnId = txnId,
                    payload = payload,
                    amount = amount,
                    selectedApp = app
                )

                onLaunch(intent)
            } finally {
                isLaunchingPayment = false
            }
        }
    }

    /**
     * Copies payee UPI ID to clipboard and opens the UPI application directly.
     * Essential workaround for personal (P2P) transfers where banks/PhonePe restrict raw intent injection.
     */
    fun copyUpiIdAndLaunchApp(context: Context, app: UpiApp) {
        val payload = activePayload ?: return
        val amount = activeAmount

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("UPI ID", payload.vpa)
        clipboard?.setPrimaryClip(clip)

        viewModelScope.launch {
            preferencesRepository.setLastUsedUpiApp(app.packageName)

            val txnId = UUID.randomUUID().toString()
            activeTxnId = txnId

            if (amount != null) {
                val transaction = Transaction(
                    id = txnId,
                    vpa = payload.vpa,
                    payeeName = payload.payeeName,
                    amountPaise = amount.movePointRight(2).longValueExact(),
                    note = payload.note,
                    qrTxnRef = payload.txnRef,
                    appPackage = app.packageName,
                    status = TransactionStatus.UNCONFIRMED
                )
                transactionRepository.insert(transaction)
            }

            _uiState.value = PayUiState.AwaitingResult(
                txnId = txnId,
                payload = payload,
                amount = amount ?: BigDecimal.ZERO,
                selectedApp = app
            )

            android.widget.Toast.makeText(
                context,
                "UPI ID copied! In ${app.label}, tap 'To UPI ID' and paste to pay.",
                android.widget.Toast.LENGTH_LONG
            ).show()

            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
        }
    }

    private var hasLeftAppForPayment = false

    fun onAppPaused() {
        if (_uiState.value is PayUiState.AwaitingResult) {
            hasLeftAppForPayment = true
        }
    }

    /**
     * Triggered when returning from the external UPI application to the foreground.
     * Guarantees the user is given the option to confirm success or failure even if
     * the external app did not pass back an Activity Result intent.
     */
    fun onAppResumed() {
        if (!hasLeftAppForPayment) return
        hasLeftAppForPayment = false

        val awaiting = _uiState.value as? PayUiState.AwaitingResult ?: return
        val txnId = awaiting.txnId

        viewModelScope.launch {
            transactionRepository.updateStatus(id = txnId, status = TransactionStatus.UNCONFIRMED)
            val updatedTxn = transactionRepository.getTransactionById(txnId)
            _uiState.value = PayUiState.Result(
                outcome = PaymentOutcome.Unconfirmed(
                    message = "Returned from ${awaiting.selectedApp.label}. Please confirm payment outcome."
                ),
                transaction = updatedTxn
            )
        }
    }

    /**
     * Interprets result returned from the launched UPI application activity.
     */
    fun onPaymentResultReceived(extras: Map<String, String?>) {
        val txnId = activeTxnId ?: return
        val outcome = interpretPaymentResult(extras)

        viewModelScope.launch {
            val (targetStatus, outcomeTxnId) = when (outcome) {
                is PaymentOutcome.Success -> TransactionStatus.SUCCESS to outcome.txnId
                is PaymentOutcome.Pending -> TransactionStatus.PENDING to outcome.txnId
                is PaymentOutcome.Failed -> TransactionStatus.FAILED to null
                is PaymentOutcome.Unconfirmed -> TransactionStatus.UNCONFIRMED to null
            }

            transactionRepository.updateStatus(
                id = txnId,
                status = targetStatus,
                upiTxnId = outcomeTxnId
            )

            val updatedTxn = transactionRepository.getTransactionById(txnId)
            _uiState.value = PayUiState.Result(outcome = outcome, transaction = updatedTxn)
        }
    }

    /**
     * Allows user to manually confirm an unconfirmed or pending transaction.
     */
    fun onUserManualConfirm(txnId: String, markAsPaid: Boolean) {
        viewModelScope.launch {
            val status = if (markAsPaid) TransactionStatus.USER_CONFIRMED_PAID else TransactionStatus.FAILED
            transactionRepository.updateStatus(id = txnId, status = status)
            val updated = transactionRepository.getTransactionById(txnId)
            val outcome = if (markAsPaid) {
                PaymentOutcome.Success(txnId = updated?.upiTxnId, responseCode = "MANUAL_PAID")
            } else {
                PaymentOutcome.Failed(responseCode = "MANUAL_FAILED", message = "Marked as not completed by user")
            }
            _uiState.value = PayUiState.Result(outcome = outcome, transaction = updated)
        }
    }

    /**
     * Resets the entire payment session back to scanning.
     */
    fun resetToScanning() {
        activePayload = null
        activeAmount = null
        activeTxnId = null
        isLaunchingPayment = false
        _uiState.value = PayUiState.Scanning
    }
}
