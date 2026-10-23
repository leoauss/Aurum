package com.example.aurum.domain.model

import android.graphics.drawable.Drawable

/**
 * Represents an installed UPI-capable application on the device.
 *
 * @property packageName Android application package identifier (e.g. "net.one97.paytm").
 * @property label Human-readable application name (e.g. "Paytm").
 * @property icon Optional app launcher icon drawable. Nullable for JVM unit testing and headless scenarios.
 */
data class UpiApp(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null
)
