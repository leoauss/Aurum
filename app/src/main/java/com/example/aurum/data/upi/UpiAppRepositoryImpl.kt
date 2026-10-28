package com.example.aurum.data.upi

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.aurum.domain.model.UpiApp
import com.example.aurum.domain.repository.PreferencesRepository
import com.example.aurum.domain.repository.UpiAppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android [PackageManager]-backed implementation of [UpiAppRepository].
 *
 * Queries the system for any applications that register an activity capable of
 * handling a `upi://pay` deep link intent.
 */
@Singleton
class UpiAppRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) : UpiAppRepository {

    override fun getInstalledApps(): List<UpiApp> {
        val packageManager = context.packageManager
        val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(UPI_PAY_URI_SCHEME))

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                upiIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(upiIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        val lastUsedPackage = preferencesRepository.lastUsedAppCached

        return resolveInfos
            .filter { resolveInfo ->
                // Filter out self if the current app registers upi://pay
                resolveInfo.activityInfo.packageName != context.packageName
            }
            .distinctBy { it.activityInfo.packageName }
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: packageName
                UpiApp(
                    packageName = packageName,
                    label = label
                )
            }
            .sortedWith { app1, app2 ->
                when {
                    app1.packageName == lastUsedPackage -> -1
                    app2.packageName == lastUsedPackage -> 1
                    else -> app1.label.compareTo(app2.label, ignoreCase = true)
                }
            }
    }

    companion object {
        private const val UPI_PAY_URI_SCHEME = "upi://pay"
    }
}
