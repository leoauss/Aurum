package com.example.aurum.domain.usecase

import com.example.aurum.domain.model.UpiApp
import com.example.aurum.domain.repository.UpiAppRepository
import javax.inject.Inject

/**
 * Use case that retrieves the list of UPI-capable apps installed on the device,
 * sorted with the last-used app first.
 *
 * This is a thin delegation to [UpiAppRepository]; keeping it as a use case
 * ensures the domain layer remains the single source of business logic and
 * keeps ViewModels decoupled from the repository directly.
 */
class GetInstalledUpiApps @Inject constructor(
    private val upiAppRepository: UpiAppRepository,
) {
    operator fun invoke(): List<UpiApp> = upiAppRepository.getInstalledApps()
}

