package com.example.aurum.ui.amount

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.aurum.ui.theme.AurumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountScreen(
    payload: UpiPayload,
    input: String,
    error: String?,
    onKeyPressed: (String) -> Unit,
    onQuickAdd: (Long) -> Unit,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveFormattedAmount = InrFormatter.formatInputLive(input)
    val isValid = error == null && input.isNotBlank() && input != "0" && input != "0." && input != "0.0" && input != "0.00"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AurumTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Enter Amount",
                        style = AurumTheme.typography.titleMedium,
                        color = AurumTheme.colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Payee summary header
            PayeeBadgeHeader(
                payeeName = payload.payeeName,
                vpa = payload.vpa,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Large Formatted Amount Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = liveFormattedAmount,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (error != null) AurumTheme.colors.error else AurumTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Validation Error",
                            tint = AurumTheme.colors.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = error,
                            style = AurumTheme.typography.bodySmall,
                            color = AurumTheme.colors.error
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Maximum payment limit: ₹1,00,000",
                        style = AurumTheme.typography.bodySmall,
                        color = AurumTheme.colors.onSurfaceMuted
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Quick Add Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100L, 500L, 1000L, 2000L).forEach { addAmount ->
                    QuickAddChip(
                        amount = addAmount,
                        onClick = { onQuickAdd(addAmount) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Keypad
            NumericKeypad(
                onKeyPressed = onKeyPressed,
                modifier = Modifier.fillMaxWidth()
            )

            // Proceed Button
            Button(
                onClick = onProceed,
                enabled = isValid,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AurumTheme.colors.brand,
                    contentColor = AurumTheme.colors.onBrand,
                    disabledContainerColor = AurumTheme.colors.surfaceRaised,
                    disabledContentColor = AurumTheme.colors.onSurfaceMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (isValid) "Proceed to Pay $liveFormattedAmount" else "Enter Valid Amount",
                    style = AurumTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PayeeBadgeHeader(
    payeeName: String?,
    vpa: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AurumTheme.colors.surfaceRaised)
            .border(1.dp, AurumTheme.colors.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AurumTheme.colors.brandVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (payeeName?.take(1) ?: vpa.take(1)).uppercase(),
                    color = AurumTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payeeName?.takeIf { it.isNotBlank() } ?: "Unknown Payee",
                    style = AurumTheme.typography.titleSmall,
                    color = AurumTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = vpa,
                    style = AurumTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = AurumTheme.colors.onSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickAddChip(
    amount: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AurumTheme.colors.surfaceRaised)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+₹$amount",
            style = AurumTheme.typography.labelMedium,
            color = AurumTheme.colors.accent,
            fontWeight = FontWeight.SemiBold
        )
    }
}

