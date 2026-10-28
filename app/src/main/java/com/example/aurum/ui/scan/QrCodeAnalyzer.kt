package com.example.aurum.ui.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CameraX [ImageAnalysis.Analyzer] that feeds frames to Google ML Kit Barcode Scanner.
 *
 * Configured strictly for [Barcode.FORMAT_QR_CODE]. Employs an [AtomicBoolean] lock to
 * prevent race conditions, rapid duplicate emissions, or repeated screen transitions.
 *
 * @param onQrCodeScanned Callback invoked with raw QR string upon first valid detection.
 * @param onScanFailed Optional error handler for ML Kit detection failures.
 */
class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit,
    private val onScanFailed: ((Exception) -> Unit)? = null
) : ImageAnalysis.Analyzer {

    private val isLocked = AtomicBoolean(false)

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || isLocked.get()) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (isLocked.get()) return@addOnSuccessListener

                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue?.trim() ?: continue
                    if (rawValue.isNotEmpty()) {
                        if (isLocked.compareAndSet(false, true)) {
                            onQrCodeScanned(rawValue)
                            break
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                onScanFailed?.invoke(exception)
            }
            .addOnCompleteListener {
                // Ensure ImageProxy is released to receive the next frame
                imageProxy.close()
            }
    }

    /**
     * Resets the scan lock so the analyzer can accept future QR codes (e.g. after
     * an error dialog is dismissed or user navigates back to scan screen).
     */
    fun resumeScanning() {
        isLocked.set(false)
    }

    /**
     * Closes the underlying ML Kit scanner resources.
     */
    fun close() {
        scanner.close()
    }
}

