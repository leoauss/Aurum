package com.example.aurum.ui.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurum.domain.model.Transaction
import com.example.aurum.domain.model.TransactionStatus
import com.example.aurum.ui.amount.InrFormatter
import com.example.aurum.ui.theme.AurumTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AurumTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Payment History",
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
                actions = {
                    if (transactions.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear History",
                                tint = AurumTheme.colors.onSurfaceMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AurumTheme.colors.background
                )
            )
        }
    ) { innerPadding ->
        if (transactions.isEmpty()) {
            EmptyHistoryView(modifier = Modifier.padding(innerPadding).fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = transactions, key = { it.id }) { txn ->
                    TransactionItemCard(transaction = txn)
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text(text = "Clear History?") },
                text = { Text(text = "Are you sure you want to delete all transaction records? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearDialog = false
                            onClearHistory()
                        }
                    ) {
                        Text("Delete All", color = AurumTheme.colors.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = AurumTheme.colors.surfaceRaised
            )
        }
    }
}

@Composable
private fun TransactionItemCard(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(transaction.createdAt) { dateFormat.format(Date(transaction.createdAt)) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AurumTheme.colors.surfaceRaised)
            .border(1.dp, AurumTheme.colors.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.payeeName?.takeIf { it.isNotBlank() } ?: "Direct UPI Transfer",
                        style = AurumTheme.typography.titleSmall,
                        color = AurumTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = transaction.vpa,
                        style = AurumTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = AurumTheme.colors.onSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = InrFormatter.formatPaiseWithSymbol(transaction.amountPaise),
                    style = AurumTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AurumTheme.colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedDate,
                    style = AurumTheme.typography.labelSmall,
                    color = AurumTheme.colors.onSurfaceMuted
                )

                StatusChip(status = transaction.status)
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: TransactionStatus,
    modifier: Modifier = Modifier
) {
    val (label, bg, textCol) = when (status) {
        TransactionStatus.SUCCESS -> Triple("Success", AurumTheme.colors.success.copy(alpha = 0.15f), AurumTheme.colors.success)
        TransactionStatus.USER_CONFIRMED_PAID -> Triple("Paid (Manual)", AurumTheme.colors.success.copy(alpha = 0.15f), AurumTheme.colors.success)
        TransactionStatus.PENDING -> Triple("Pending", AurumTheme.colors.warning.copy(alpha = 0.15f), AurumTheme.colors.warning)
        TransactionStatus.INITIATED -> Triple("Initiated", AurumTheme.colors.warning.copy(alpha = 0.15f), AurumTheme.colors.warning)
        TransactionStatus.FAILED -> Triple("Failed", AurumTheme.colors.error.copy(alpha = 0.15f), AurumTheme.colors.error)
        TransactionStatus.UNCONFIRMED -> Triple("Unconfirmed", AurumTheme.colors.outline.copy(alpha = 0.3f), AurumTheme.colors.onSurfaceMuted)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, textCol.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = AurumTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textCol
        )
    }
}

@Composable
private fun EmptyHistoryView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AurumTheme.colors.surfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = AurumTheme.colors.onSurfaceMuted,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Payment History",
            style = AurumTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AurumTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Transactions you initiate will appear here.",
            style = AurumTheme.typography.bodySmall,
            color = AurumTheme.colors.onSurfaceMuted
        )
    }
}

