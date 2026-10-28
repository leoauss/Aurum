package com.example.aurum.ui.scan

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aurum.ui.theme.AurumTheme

enum class CameraPermissionStatus {
    GRANTED,
    DENIED_RATIONALE,
    PERMANENTLY_DENIED
}

/**
 * Handles the camera permission lifecycle, displaying appropriate rationale and
 * fallback gallery import triggers when permission is denied.
 */
@Composable
fun CameraPermissionHandler(
    modifier: Modifier = Modifier,
    onGalleryPickRequested: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasRequestedInitialPrompt by rememberSaveable { mutableStateOf(false) }

    var permissionStatus by remember {
        mutableStateOf(checkCameraPermission(context, activity, hasRequestedInitialPrompt))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRequestedInitialPrompt = true
        permissionStatus = if (isGranted) {
            CameraPermissionStatus.GRANTED
        } else {
            if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                CameraPermissionStatus.DENIED_RATIONALE
            } else {
                CameraPermissionStatus.PERMANENTLY_DENIED
            }
        }
    }

    // Auto-prompt system permission dialog on very first launch
    LaunchedEffect(Unit) {
        if (!hasRequestedInitialPrompt && permissionStatus != CameraPermissionStatus.GRANTED) {
            hasRequestedInitialPrompt = true
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // Refresh permission status when app resumes (e.g. returning from app settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionStatus = checkCameraPermission(context, activity, hasRequestedInitialPrompt)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (permissionStatus) {
        CameraPermissionStatus.GRANTED -> {
            content()
        }
        CameraPermissionStatus.DENIED_RATIONALE -> {
            PermissionRationaleContent(
                modifier = modifier,
                title = "Camera Permission Needed",
                description = "Aurum needs access to your camera to scan UPI QR codes quickly and securely.",
                primaryButtonText = "Grant Permission",
                onPrimaryAction = { launcher.launch(Manifest.permission.CAMERA) },
                onGalleryPickRequested = onGalleryPickRequested
            )
        }
        CameraPermissionStatus.PERMANENTLY_DENIED -> {
            PermissionRationaleContent(
                modifier = modifier,
                title = "Camera Access Blocked",
                description = "Camera permission was permanently denied. Please enable it in Settings to scan QR codes, or import a QR code from your gallery.",
                primaryButtonText = "Open Settings",
                onPrimaryAction = { openAppSettings(context) },
                onGalleryPickRequested = onGalleryPickRequested
            )
        }
    }
}

@Composable
private fun PermissionRationaleContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    primaryButtonText: String,
    onPrimaryAction: () -> Unit,
    onGalleryPickRequested: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AurumTheme.colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(AurumTheme.colors.surfaceRaised, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📷",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AurumTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AurumTheme.colors.onSurfaceMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onPrimaryAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AurumTheme.colors.brand,
                    contentColor = AurumTheme.colors.onBrand
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = primaryButtonText, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onGalleryPickRequested,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AurumTheme.colors.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Choose from Gallery", fontWeight = FontWeight.Normal)
            }
        }
    }
}

private fun checkCameraPermission(
    context: Context,
    activity: Activity?,
    hasRequestedPrompt: Boolean
): CameraPermissionStatus {
    val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    return when {
        granted -> CameraPermissionStatus.GRANTED
        activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) ->
            CameraPermissionStatus.DENIED_RATIONALE
        hasRequestedPrompt -> CameraPermissionStatus.PERMANENTLY_DENIED
        else -> CameraPermissionStatus.DENIED_RATIONALE
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
