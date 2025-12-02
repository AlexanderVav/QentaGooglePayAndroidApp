package com.example.qentapayandroid

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var payButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        payButton = findViewById(R.id.btnGooglePay)
        webView = findViewById(R.id.webView)

        setupWebView()
        setupBackButtonHandler()

        payButton.setOnClickListener {
            startPaymentMockup()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()

                if (url.contains("maskedPan")) {
                    handlePaymentResult(Uri.parse(url))
                    return true
                }

                // Solange "allclear" NICHT in der URL steht,
                // laden wir alles ganz normal weiter (auch Hobex-Seiten).
                return false
            }
        }
    }

    private fun startPaymentMockup() {
        payButton.visibility = View.GONE
        webView.visibility = View.VISIBLE

        // Deine Start-URL
        val url = "https://8a47d0cec79c.ngrok-free.app"
        webView.loadUrl(url)
    }

    private fun handlePaymentResult(data: Uri) {
        webView.visibility = View.GONE
        payButton.visibility = View.VISIBLE

        // FIX: Hier laden wir "about:blank".
        // Wenn du hier wieder die Start-URL lädst, startet die Zahlung im Hintergrund neu!
        webView.loadUrl("about:blank")

        val status = data.getQueryParameter("paymentState")

        when (status) {
            "SUCCESS" -> Toast.makeText(this, "Bezahlung erfolgreich!", Toast.LENGTH_LONG).show()
            "NOTOK" -> Toast.makeText(this, "Abbruch", Toast.LENGTH_SHORT).show()
            "FAILURE" -> Toast.makeText(this, "Fehler aufgetreten.", Toast.LENGTH_LONG).show()
            // Falls kein Status-Parameter da ist, aber "allclear" gefunden wurde:
            else -> Toast.makeText(this, "Vorgang beendet (All Clear)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBackButtonHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.visibility == View.VISIBLE) {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        // Wenn man nicht mehr zurück kann -> Abbruch
                        webView.visibility = View.GONE
                        payButton.visibility = View.VISIBLE
                        webView.loadUrl("about:blank")
                        Toast.makeText(this@MainActivity, "Abgebrochen", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}