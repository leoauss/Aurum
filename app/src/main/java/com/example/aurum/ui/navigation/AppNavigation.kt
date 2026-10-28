package com.example.aurum.ui.navigation

import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aurum.ui.amount.AmountScreen
import com.example.aurum.ui.confirm.ConfirmPayeeScreen
import com.example.aurum.ui.history.HistoryScreen
import com.example.aurum.ui.history.HistoryViewModel
import com.example.aurum.ui.picker.UpiAppPickerSheet
import com.example.aurum.ui.result.ResultScreen
import com.example.aurum.ui.scan.PayUiState
import com.example.aurum.ui.scan.PaymentFlowViewModel
import com.example.aurum.ui.scan.ScanScreen

object Routes {
    const val SCAN = "scan"
    const val CONFIRM = "confirm"
    const val AMOUNT = "amount"
    const val RESULT = "result/{txnId}"
    const val HISTORY = "history"

    fun resultRoute(txnId: String): String = "result/$txnId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    paymentViewModel: PaymentFlowViewModel,
    onLaunchPaymentIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val uiState by paymentViewModel.uiState.collectAsState()

    // Observe result outcome to navigate automatically to Result screen
    LaunchedEffect(uiState) {
        if (uiState is PayUiState.Result) {
            val txnId = paymentViewModel.activeTxnId ?: "latest"
            navController.navigate(Routes.resultRoute(txnId)) {
                popUpTo(Routes.SCAN) { inclusive = false }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SCAN,
        modifier = modifier
    ) {
        // ── 1. SCAN SCREEN ──────────────────────────────────────────────────
        composable(Routes.SCAN) {
            ScanScreen(
                uiState = uiState,
                onQrDetected = { rawQr ->
                    val success = paymentViewModel.onQrScanned(rawQr)
                    if (success) {
                        navController.navigate(Routes.CONFIRM)
                    }
                    success
                },
                onGalleryImageSelected = { uri ->
                    paymentViewModel.onGalleryImageSelected(navController.context, uri)
                    if (paymentViewModel.activePayload != null) {
                        navController.navigate(Routes.CONFIRM)
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY)
                },
                onDismissError = {
                    paymentViewModel.resetToScanning()
                },
                onReconcileAction = { txn, isPaid ->
                    paymentViewModel.onUserManualConfirm(txn.id, isPaid)
                }
            )
        }

        // ── 2. CONFIRM PAYEE SCREEN ─────────────────────────────────────────
        composable(Routes.CONFIRM) {
            val payload = paymentViewModel.activePayload
            if (payload == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack(Routes.SCAN, false)
                }
            } else {
                ConfirmPayeeScreen(
                    payload = payload,
                    onContinue = {
                        paymentViewModel.onConfirmContinue()
                        if (payload.amount == null) {
                            navController.navigate(Routes.AMOUNT)
                        }
                    },
                    onCancel = {
                        paymentViewModel.resetToScanning()
                        navController.popBackStack(Routes.SCAN, false)
                    }
                )
            }
        }

        // ── 3. AMOUNT ENTRY SCREEN ──────────────────────────────────────────
        composable(Routes.AMOUNT) {
            val payload = paymentViewModel.activePayload
            val enterAmountState = uiState as? PayUiState.EnterAmount

            if (payload == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack(Routes.SCAN, false)
                }
            } else {
                AmountScreen(
                    payload = payload,
                    input = enterAmountState?.input ?: "",
                    error = enterAmountState?.error,
                    onKeyPressed = { key ->
                        paymentViewModel.onKeypadKeyPressed(key)
                    },
                    onQuickAdd = { amount ->
                        paymentViewModel.onQuickAdd(amount)
                    },
                    onProceed = {
                        paymentViewModel.onAmountConfirmed()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ── 4. RESULT SCREEN ────────────────────────────────────────────────
        composable(
            route = Routes.RESULT,
            arguments = listOf(navArgument("txnId") { type = NavType.StringType })
        ) {
            val resultState = uiState as? PayUiState.Result
            if (resultState != null) {
                ResultScreen(
                    outcome = resultState.outcome,
                    transaction = resultState.transaction,
                    onScanAnother = {
                        paymentViewModel.resetToScanning()
                           navController.navigate(Routes.SCAN) {
                            popUpTo(Routes.SCAN) { inclusive = true }
                        }
                    },
                    onViewHistory = {
                        navController.navigate(Routes.HISTORY)
                    },
                    onManualConfirm = { markAsPaid ->
                        paymentViewModel.activeTxnId?.let { id ->
                            paymentViewModel.onUserManualConfirm(id, markAsPaid)
                        }
                    }
                )
            }
        }

        // ── 5. HISTORY SCREEN ───────────────────────────────────────────────
        composable(Routes.HISTORY) {
            val historyViewModel: HistoryViewModel = hiltViewModel()
            val transactions by historyViewModel.transactions.collectAsState()

            HistoryScreen(
                transactions = transactions,
                onClearHistory = {
                    historyViewModel.clearAllHistory()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }

    // Modal UPI App Picker Sheet (visible when state is ChooseApp)
    if (uiState is PayUiState.ChooseApp) {
        val chooseAppState = uiState as PayUiState.ChooseApp
        UpiAppPickerSheet(
            payload = chooseAppState.payload,
            amount = chooseAppState.amount,
            apps = chooseAppState.apps,
            lastUsedPackage = chooseAppState.lastUsedPackage,
            onAppSelected = { app ->
                paymentViewModel.initiatePayment(app) { intent ->
                    onLaunchPaymentIntent(intent)
                }
            },
            onCopyAndOpen = { app ->
                paymentViewModel.copyUpiIdAndLaunchApp(navController.context, app)
            },
            onDismiss = {
                // If user dismissed sheet, return to previous state
                if (paymentViewModel.activePayload?.amount != null) {
                    navController.popBackStack(Routes.CONFIRM, false)
                }
            }
        )
    }

    // Modal Confirmation Dialog when returning from UPI app in AwaitingResult state
    if (uiState is PayUiState.AwaitingResult) {
        val awaiting = uiState as PayUiState.AwaitingResult
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { /* user must select outcome */ },
            title = {
                androidx.compose.material3.Text(
                    text = "Confirm Payment Status",
                    style = com.example.aurum.ui.theme.AurumTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            },
            text = {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text(
                        text = "Did your payment of ${com.example.aurum.ui.amount.InrFormatter.formatWithSymbol(awaiting.amount)} to ${awaiting.payload.payeeName?.takeIf { it.isNotBlank() } ?: awaiting.payload.vpa} complete in ${awaiting.selectedApp.label}?",
                        style = com.example.aurum.ui.theme.AurumTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        paymentViewModel.onUserManualConfirm(awaiting.txnId, markAsPaid = true)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = com.example.aurum.ui.theme.AurumTheme.colors.success
                    )
                ) {
                    androidx.compose.material3.Text("Yes, Paid")
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        paymentViewModel.onUserManualConfirm(awaiting.txnId, markAsPaid = false)
                    }
                ) {
                    androidx.compose.material3.Text(
                        text = "No, Failed",
                        color = com.example.aurum.ui.theme.AurumTheme.colors.error
                    )
                }
            },
            containerColor = com.example.aurum.ui.theme.AurumTheme.colors.surfaceRaised
        )
    }
}
