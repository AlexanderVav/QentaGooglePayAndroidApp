package com.example.qentapayandroid;

import static com.example.qentapayandroid.MainActivity.NGROK_URL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

public class PaymentActivityWebView extends AppCompatActivity {
    private static final CharSequence SHOPPER_URL_RESULT = "return.php#success";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_payment);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        if (WebViewFeature.isFeatureSupported(
                WebViewFeature.PAYMENT_REQUEST)) {
            WebSettingsCompat.setPaymentRequestEnabled(webSettings, true);
        }

        setupWebView();
        setupBackButtonHandler();

        // Start URL -> set to localhost if you are using a simulator
        //otherwise you can use zrok or ngrok to create a tunnel for your device
        webView.loadUrl(NGROK_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                readVisibleText(view);

                String jsListener =
                        "var checkVisibleText = function() {" +
                                "   setTimeout(function() {" +
                                "       try {" +
                                "           /* WICHTIG: innerText liest nur das Sichtbare */ " +
                                "           var visibleText = document.body.innerText;" +
                                "           window.AndroidBridge.sendData(visibleText);" +
                                "       } catch(e) { console.error(e); }" +
                                "   }, 800);" +
                                "};" +
                                "document.addEventListener('click', checkVisibleText, true);" +
                                "document.addEventListener('touchend', checkVisibleText, true);";

                view.evaluateJavascript(jsListener, null);
            }
        });
    }

    private void readVisibleText(WebView view) {
        view.evaluateJavascript("window.AndroidBridge.sendData(document.body.innerText);", null);
    }

    private void readPageContent(WebView view) {
        view.evaluateJavascript("window.AndroidBridge.sendData(document.documentElement.outerHTML);", null);
    }

    private void setupBackButtonHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        });
    }

    public class WebAppInterface {

        @JavascriptInterface
        public void sendData(String data) {

            runOnUiThread(() -> {
                String currentUrl = webView.getUrl();
                if (currentUrl != null) {
                    Uri uri = Uri.parse(currentUrl);
                    if (uri.getQueryParameterNames().contains(SHOPPER_URL_RESULT)) {
                        finishPayment(true);
                        return;
                    }
                }

                if (data == null || data.isEmpty())
                    return;

                String visibleText = data.trim();
                Log.d("VISIBLE_TEXT", "Gelesen: " + visibleText);

                //search for paymentState on sucuess page
                if (visibleText.contains("paymentState") && visibleText.contains("SUCCESS")) {
                    Log.d("CHECK_TEXT", "Payment state is ok");
                    finishPayment(true);
                } else if (visibleText.contains("FAILURE") || visibleText.contains("NOTOK")) {
                    finishPayment(false);
                }
            });
        }

        private void finishPayment(boolean success) {
            if (success) {
                Intent intent = new Intent(PaymentActivityWebView.this, SuccessActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
            } else {
                Toast.makeText(PaymentActivityWebView.this, "Zahlung fehlgeschlagen", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    }
}