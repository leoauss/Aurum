package com.example.aurum.ui.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurum.domain.model.PaymentOutcome
import com.example.aurum.domain.model.Transaction
import com.example.aurum.ui.amount.InrFormatter
import com.example.aurum.ui.theme.AurumTheme

@Composable
fun ResultScreen(
    outcome: PaymentOutcome,
    transaction: Transaction?,
    onScanAnother: () -> Unit,
    onViewHistory: () -> Unit,
    onManualConfirm: ((markAsPaid: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.4f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AurumTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            when (outcome) {
                is PaymentOutcome.Success -> {
                    ResultStatusBadge(
                        icon = Icons.Default.Check,
                        iconColor = AurumTheme.colors.success,
                        badgeBackground = AurumTheme.colors.success.copy(alpha = 0.15f),
                        badgeBorder = AurumTheme.colors.success.copy(alpha = 0.5f),
                        title = "Payment Successful",
                        subtitle = "Reported by your UPI app",
                        scale = scaleAnim.value
                    )
                }
                is PaymentOutcome.Pending -> {
                    ResultStatusBadge(
                        icon = Icons.Default.Info,
                        iconColor = AurumTheme.colors.warning,
                        badgeBackground = AurumTheme.colors.warning.copy(alpha = 0.15f),
                        badgeBorder = AurumTheme.colors.warning.copy(alpha = 0.5f),
                        title = "Payment Pending",
                        subtitle = "Processing with your bank",
                        scale = scaleAnim.value
                    )
                }
                is PaymentOutcome.Failed -> {
                    ResultStatusBadge(
                        icon = Icons.Default.Close,
                        iconColor = AurumTheme.colors.error,
                        badgeBackground = AurumTheme.colors.error.copy(alpha = 0.15f),
                        badgeBorder = AurumTheme.colors.error.copy(alpha = 0.5f),
                        title = "Payment Failed",
                        subtitle = outcome.message ?: "Transaction declined or cancelled",
                        scale = scaleAnim.value
                    )
                }
                is PaymentOutcome.Unconfirmed -> {
                    ResultStatusBadge(
                        icon = Icons.Default.Warning,
                        iconColor = AurumTheme.colors.onSurfaceMuted,
                        badgeBackground = AurumTheme.colors.surfaceRaised,
                        badgeBorder = AurumTheme.colors.outline,
                        title = "Status Unconfirmed",
                        subtitle = "External app did not provide confirmation",
                        scale = scaleAnim.value
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Display
            if (transaction != null) {
                Text(
                    text = InrFormatter.formatPaiseWithSymbol(transaction.amountPaise),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = AurumTheme.colors.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Paid to ${transaction.payeeName?.takeIf { it.isNotBlank() } ?: transaction.vpa}",
                    style = AurumTheme.typography.titleMedium,
                    color = AurumTheme.colors.onSurfaceMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Detail Card
                TransactionDetailCard(
                    transaction = transaction,
                    outcome = outcome,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Manual Action Box for Unconfirmed and Pending states
            if ((outcome is PaymentOutcome.Unconfirmed || outcome is PaymentOutcome.Pending) && onManualConfirm != null) {
                Spacer(modifier = Modifier.height(20.dp))
                ManualConfirmationCard(
                    onConfirmPaid = { onManualConfirm(true) },
                    onConfirmFailed = { onManualConfirm(false) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Primary Bottom Actions
            Button(
                onClick = onScanAnother,
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
                    text = "Scan Another QR",
                    style = AurumTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onViewHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "View Payment History",
                    style = AurumTheme.typography.titleSmall,
                    color = AurumTheme.colors.accent
                )
            }
        }
    }
}

@Composable
private fun ResultStatusBadge(
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    badgeBackground: androidx.compose.ui.graphics.Color,
    badgeBorder: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(80.dp)
                .clip(CircleShape)
                .background(badgeBackground)
                .border(2.dp, badgeBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = AurumTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AurumTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = AurumTheme.typography.bodyMedium,
            color = AurumTheme.colors.onSurfaceMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TransactionDetailCard(
    transaction: Transaction,
    outcome: PaymentOutcome,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AurumTheme.colors.surfaceRaised)
            .border(1.dp, AurumTheme.colors.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailRow(label = "Payee VPA", value = transaction.vpa, isMonospace = true)

            val txnRef = (outcome as? PaymentOutcome.Success)?.txnId ?: transaction.upiTxnId
            if (!txnRef.isNullOrBlank()) {
                DetailRow(label = "UPI Ref ID", value = txnRef, isMonospace = true)
            }

            val approval = (outcome as? PaymentOutcome.Success)?.approvalRefNo
            if (!approval.isNullOrBlank()) {
                DetailRow(label = "Approval RRN", value = approval, isMonospace = true)
            }

            val respCode = when (outcome) {
                is PaymentOutcome.Success -> outcome.responseCode
                is PaymentOutcome.Pending -> outcome.responseCode
                is PaymentOutcome.Failed -> outcome.responseCode
                is PaymentOutcome.Unconfirmed -> null
            }
            if (!respCode.isNullOrBlank()) {
                DetailRow(label = "Response Code", value = respCode)
            }

            if (!transaction.appPackage.isNullOrBlank()) {
                DetailRow(label = "App Used", value = transaction.appPackage)
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AurumTheme.typography.bodySmall,
            color = AurumTheme.colors.onSurfaceMuted
        )
        Text(
            text = value,
            style = AurumTheme.typography.bodyMedium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Medium,
            color = AurumTheme.colors.onSurface
        )
    }
}

@Composable
private fun ManualConfirmationCard(
    onConfirmPaid: () -> Unit,
    onConfirmFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AurumTheme.colors.warning.copy(alpha = 0.1f))
            .border(1.dp, AurumTheme.colors.warning.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Did the payment complete?",
                style = AurumTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AurumTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Check your UPI app or bank SMS, then update the record below:",
                style = AurumTheme.typography.bodySmall,
                color = AurumTheme.colors.onSurfaceMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onConfirmFailed,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("No, Failed", color = AurumTheme.colors.error)
                }
                Button(
                    onClick = onConfirmPaid,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AurumTheme.colors.success
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yes, Paid")
                }
            }
        }
    }
}

