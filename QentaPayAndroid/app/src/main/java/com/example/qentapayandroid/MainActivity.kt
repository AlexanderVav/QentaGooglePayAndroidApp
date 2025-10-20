package com.example.qentapayandroid

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*

class MainActivity : AppCompatActivity() {

    private lateinit var paymentsClient: PaymentsClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize PaymentsClient (TEST environment)
        paymentsClient = Wallet.getPaymentsClient(
            this,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build()
        )

        // Button from layout
        val googlePayButton = findViewById<Button>(R.id.btnGooglePay)

        googlePayButton.setOnClickListener {
            checkGooglePayAvailability()
        }
    }

    private fun checkGooglePayAvailability() {
        val request = IsReadyToPayRequest.fromJson(
            """
            {
              "allowedPaymentMethods": [
                {
                  "type": "CARD",
                  "parameters": {
                    "allowedAuthMethods": ["PAN_ONLY","CRYPTOGRAM_3DS"],
                    "allowedCardNetworks": ["VISA","MASTERCARD"]
                  }
                }
              ]
            }
            """.trimIndent()
        )

        paymentsClient.isReadyToPay(request)
            .addOnCompleteListener { task ->
                try {
                    val result = task.getResult(ApiException::class.java)
                    if (result == true) {
                        Toast.makeText(this, "Google Pay is available", Toast.LENGTH_SHORT).show()
                        // Here you would launch the PaymentDataRequest flow
                    } else {
                        Toast.makeText(this, "Google Pay not available", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Error checking Google Pay: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}