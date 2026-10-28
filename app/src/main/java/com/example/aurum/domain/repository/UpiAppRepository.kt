package com.example.aurum.domain.repository

import com.example.aurum.domain.model.UpiApp

/**
 * Domain-layer contract for querying UPI-capable apps installed on the device.
 *
 * The data layer implementation uses [android.content.pm.PackageManager] to probe
 * which apps can handle a `upi://pay` intent.
 */
interface UpiAppRepository {

    /**
     * Returns all installed apps capable of handling a UPI payment intent.
     *
     * The list is sorted so the last-used app (if known) appears first,
     * followed by remaining apps in alphabetical order.
     */
    fun getInstalledApps(): List<UpiApp>
}

