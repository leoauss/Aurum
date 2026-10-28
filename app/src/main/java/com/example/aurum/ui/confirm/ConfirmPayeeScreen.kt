package com.example.aurum.ui.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurum.domain.model.UpiPayload
import com.example.aurum.ui.amount.InrFormatter
import com.example.aurum.ui.theme.AurumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmPayeeScreen(
    payload: UpiPayload,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPayeeNameMissing = payload.payeeName.isNullOrBlank()
    val isPresetAmount = payload.amount != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AurumTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Verify Payee",
                        style = AurumTheme.typography.titleMedium,
                        color = AurumTheme.colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AurumTheme.colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AurumTheme.colors.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Payee Avatar Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AurumTheme.colors.brandVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (payload.payeeName?.take(1) ?: payload.vpa.take(1)).uppercase(),
                    color = AurumTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payee Name (Large)
            Text(
                text = if (!isPayeeNameMissing) payload.payeeName!! else "Unspecified Merchant / Individual",
                style = AurumTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AurumTheme.colors.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // VPA Chip with Copy Action
            val context = androidx.compose.ui.platform.LocalContext.current
            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AurumTheme.colors.surfaceRaised)
                    .border(1.dp, AurumTheme.colors.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("UPI ID", payload.vpa)
                        clipboard?.setPrimaryClip(clip)
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        android.widget.Toast.makeText(context, "UPI ID copied: ${payload.vpa}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = payload.vpa,
                    style = AurumTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = AurumTheme.colors.brand
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📋",
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Account Type Chip (Merchant vs Personal)
            if (payload.isMerchant) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AurumTheme.colors.success.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "✓ Verified Merchant (${payload.mcc})",
                        style = AurumTheme.typography.labelSmall,
                        color = AurumTheme.colors.success,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AurumTheme.colors.surfaceRaised)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "👤 Personal Account (P2P)",
                        style = AurumTheme.typography.labelSmall,
                        color = AurumTheme.colors.onSurfaceMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notice for Personal Accounts regarding UPI App Intent restrictions
            if (!payload.isMerchant) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AurumTheme.colors.surfaceRaised.copy(alpha = 0.7f))
                        .border(1.dp, AurumTheme.colors.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "ℹ️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Some UPI apps restrict direct intent payments to personal accounts. If blocked by PhonePe/GPay, use 'Copy UPI ID & Open App'.",
                            style = AurumTheme.typography.bodySmall,
                            color = AurumTheme.colors.onSurfaceMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security Warning Banner if Payee Name is missing
            if (isPayeeNameMissing) {
                SecurityWarningBanner(
                    message = "Payee name not found in QR. Confirm the UPI ID carefully to avoid sending money to the wrong person."
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Amount Card or Dynamic Notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AurumTheme.colors.surfaceRaised)
                    .padding(20.dp)
            ) {
                if (isPresetAmount) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Amount to Pay",
                            style = AurumTheme.typography.labelMedium,
                            color = AurumTheme.colors.onSurfaceMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = InrFormatter.formatWithSymbol(payload.amount!!),
                            style = AurumTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = AurumTheme.colors.accent
                        )
                        Text(
                            text = "Fixed by merchant QR",
                            style = AurumTheme.typography.bodySmall,
                            color = AurumTheme.colors.onSurfaceMuted
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Dynamic amount",
                            tint = AurumTheme.colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Open Amount QR",
                                style = AurumTheme.typography.titleSmall,
                                color = AurumTheme.colors.onSurface
                            )
                            Text(
                                text = "You will enter the amount in the next step.",
                                style = AurumTheme.typography.bodySmall,
                                color = AurumTheme.colors.onSurfaceMuted
                            )
                        }
                    }
                }
            }

            // Note / Reference Details if present
            if (!payload.note.isNullOrBlank() || !payload.txnRef.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AurumTheme.colors.surfaceRaised.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    if (!payload.note.isNullOrBlank()) {
                        Text(
                            text = "Note: ${payload.note}",
                            style = AurumTheme.typography.bodySmall,
                            color = AurumTheme.colors.onSurfaceMuted
                        )
                    }
                    if (!payload.txnRef.isNullOrBlank()) {
                        Text(
                            text = "Ref: ${payload.txnRef}",
                            style = AurumTheme.typography.bodySmall,
                            color = AurumTheme.colors.onSurfaceMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action CTAs
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AurumTheme.colors.brand,
                    contentColor = AurumTheme.colors.onBrand
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isPresetAmount) "Continue to Payment" else "Enter Amount",
                    style = AurumTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AurumTheme.colors.onSurfaceMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Cancel & Scan Again",
                    style = AurumTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SecurityWarningBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AurumTheme.colors.warning.copy(alpha = 0.15f))
            .border(1.dp, AurumTheme.colors.warning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = AurumTheme.colors.warning,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = AurumTheme.typography.bodySmall,
                color = AurumTheme.colors.onSurface
            )
        }
    }
}

