package com.example.aurum.domain.model

/**
 * Domain error representations for UPI QR scan and validation failures.
 */
sealed interface ScanError {
    /** QR code content is not a valid UPI URI (wrong scheme or missing upi://pay). */
    data object NotUpiQr : ScanError

    /** The Virtual Payment Address (VPA) is missing or invalid in format. */
    data object InvalidVpa : ScanError

    /** Currency specified in QR code is not supported (only INR is supported). */
    data object UnsupportedCurrency : ScanError

    /** Amount in QR code is invalid (e.g. non-numeric, negative, or >2 decimal places). */
    data object InvalidAmount : ScanError
}

class ScanException(val error: ScanError) : IllegalArgumentException("UPI QR scan error: $error")