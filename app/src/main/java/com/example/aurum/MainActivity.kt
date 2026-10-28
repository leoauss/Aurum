package com.example.aurum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.aurum.ui.navigation.AppNavigation
import com.example.aurum.ui.scan.PaymentFlowViewModel
import com.example.aurum.ui.theme.AurumTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val paymentViewModel: PaymentFlowViewModel by viewModels()

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val extras = mutableMapOf<String, String?>()

        // Extract bundled extras from returning UPI application Intent
        result.data?.extras?.let { bundle ->
            for (key in bundle.keySet()) {
                extras[key] = bundle.getString(key)
            }
        }

        // Standard response query string returned by many UPI apps (GPay, PhonePe, Paytm)
        result.data?.getStringExtra("response")?.let { rawResponse ->
            extras["response"] = rawResponse
        }

        paymentViewModel.onPaymentResultReceived(extras)
    }

    override fun onPause() {
        super.onPause()
        paymentViewModel.onAppPaused()
    }

    override fun onResume() {
        super.onResume()
        paymentViewModel.onAppResumed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AurumTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AurumTheme.colors.background
                ) {
                    AppNavigation(
                        paymentViewModel = paymentViewModel,
                        onLaunchPaymentIntent = { intent ->
                            paymentLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }
}
