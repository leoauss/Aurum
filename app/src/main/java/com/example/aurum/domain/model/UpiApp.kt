package com.example.aurum.domain.model

/**
 * Represents an installed UPI-capable application on the device.
 *
 * @property packageName Application package identifier (e.g. "net.one97.paytm").
 * @property label Human-readable application name (e.g. "Paytm").
 */
data class UpiApp(
    val packageName: String,
    val label: String
)
