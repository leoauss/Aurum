package com.example.aurum.ui.picker

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.aurum.domain.model.UpiApp
import com.example.aurum.domain.model.UpiPayload
import com.example.aurum.ui.amount.InrFormatter
import com.example.aurum.ui.theme.AurumTheme
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiAppPickerSheet(
    payload: UpiPayload,
    amount: BigDecimal,
    apps: List<UpiApp>,
    lastUsedPackage: String?,
    onAppSelected: (UpiApp) -> Unit,
    onCopyAndOpen: ((UpiApp) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var isAppTapped by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AurumTheme.colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AurumTheme.colors.outline)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Paying amount & recipient
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pay to ${payload.payeeName?.takeIf { it.isNotBlank() } ?: payload.vpa}",
                        style = AurumTheme.typography.titleMedium,
                        color = AurumTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (payload.isMerchant) "Verified Merchant Checkout" else "Personal Account Transfer",
                        style = AurumTheme.typography.bodySmall,
                        color = if (payload.isMerchant) AurumTheme.colors.success else AurumTheme.colors.onSurfaceMuted
                    )
                }

                Text(
                    text = InrFormatter.formatWithSymbol(amount),
                    style = AurumTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AurumTheme.colors.accent
                )
            }

            // Explanatory guidance for Personal Accounts
            if (!payload.isMerchant) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AurumTheme.colors.surfaceRaised)
                        .border(1.dp, AurumTheme.colors.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Tip: If PhonePe or GPay blocks the payment with 'gallery limit', use 'Copy & Open' to paste the UPI ID directly.",
                        style = AurumTheme.typography.bodySmall,
                        color = AurumTheme.colors.onSurfaceMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (apps.isEmpty()) {
                // Empty State
                EmptyUpiAppsView(
                    context = context,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items = apps, key = { it.packageName }) { app ->
                        val isLastUsed = app.packageName == lastUsedPackage
                        UpiAppItem(
                            app = app,
                            isLastUsed = isLastUsed,
                            isPersonalAccount = !payload.isMerchant,
                            onPrimaryClick = {
                                if (!isAppTapped) {
                                    isAppTapped = true
                                    onAppSelected(app)
                                }
                            },
                            onCopyAndOpenClick = onCopyAndOpen?.let { copyFn ->
                                {
                                    if (!isAppTapped) {
                                        isAppTapped = true
                                        copyFn(app)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpiAppItem(
    app: UpiApp,
    isLastUsed: Boolean,
    isPersonalAccount: Boolean,
    onPrimaryClick: () -> Unit,
    onCopyAndOpenClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconBitmap = remember(app.packageName) {
        try {
            val drawable: Drawable = context.packageManager.getApplicationIcon(app.packageName)
            drawable.toBitmap(width = 96, height = 96).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AurumTheme.colors.surfaceRaised)
            .clickable(onClick = onPrimaryClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AurumTheme.colors.brandVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.take(1).uppercase(),
                    color = AurumTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.label,
                    style = AurumTheme.typography.titleSmall,
                    color = AurumTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isLastUsed) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AurumTheme.colors.brand.copy(alpha = 0.15f))
                            .border(1.dp, AurumTheme.colors.brand.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Last Used",
                            style = AurumTheme.typography.labelSmall,
                            color = AurumTheme.colors.brand,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Text(
                text = "Tap to open payment sheet",
                style = AurumTheme.typography.bodySmall,
                color = AurumTheme.colors.onSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Secondary Copy & Open Action for Personal accounts
        if (isPersonalAccount && onCopyAndOpenClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AurumTheme.colors.surface)
                    .border(1.dp, AurumTheme.colors.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onCopyAndOpenClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Copy & Open",
                    style = AurumTheme.typography.labelSmall,
                    color = AurumTheme.colors.accent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyUpiAppsView(
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AurumTheme.colors.surfaceRaised)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "No UPI app",
            tint = AurumTheme.colors.warning,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No UPI Apps Found",
            style = AurumTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AurumTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Aurum requires an installed UPI app (Google Pay, PhonePe, Paytm, BHIM, etc.) to securely complete transactions.",
            style = AurumTheme.typography.bodySmall,
            color = AurumTheme.colors.onSurfaceMuted,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=upi"))
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=upi"))
                try {
                    context.startActivity(marketIntent)
                } catch (e: Exception) {
                    try {
                        context.startActivity(webIntent)
                    } catch (_: Exception) {}
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AurumTheme.colors.brand,
                contentColor = AurumTheme.colors.onBrand
            )
        ) {
            Text(
                text = "Find UPI Apps on Play Store",
                style = AurumTheme.typography.labelLarge
            )
        }
    }
}
