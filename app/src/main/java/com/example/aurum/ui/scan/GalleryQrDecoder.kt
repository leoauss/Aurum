package com.example.aurum.ui.scan

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Domain errors specifically related to gallery image QR code extraction.
 */
sealed class GalleryDecodeException(message: String) : Exception(message) {
    data object NoQrFound : GalleryDecodeException("No QR code detected in the selected image.")
    data class ReadFailed(val causeException: Throwable) : GalleryDecodeException("Failed to read image: ${causeException.localizedMessage}")
}

/**
 * Decodes QR codes from user-selected image URIs using Google ML Kit.
 */
@Singleton
class GalleryQrDecoder @Inject constructor() {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    /**
     * Extracts raw QR code string from the provided [imageUri].
     *
     * @return [Result.success] with the raw QR string, or [Result.failure] if no QR code
     * was detected or the file could not be read.
     */
    suspend fun decode(context: Context, imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        val inputImage = try {
            InputImage.fromFilePath(context, imageUri)
        } catch (e: Exception) {
            return@withContext Result.failure(GalleryDecodeException.ReadFailed(e))
        }

        suspendCancellableCoroutine { continuation ->
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val qrCode = barcodes
                        .firstOrNull { !it.rawValue.isNullOrBlank() }
                        ?.rawValue
                        ?.trim()

                    if (qrCode != null) {
                        continuation.resume(Result.success(qrCode))
                    } else {
                        continuation.resume(Result.failure(GalleryDecodeException.NoQrFound))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }
}

