package com.example.qentapayandroid;

import static com.example.qentapayandroid.MainActivity.NGROK_URL;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

public class PaymentActivityChromeCustomTabs extends AppCompatActivity {

    private static final String TAG = "PaymentActivityCCT";

    // Tracks if the Custom Tab is currently active
    private boolean customTabLaunched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Optional: Keep this if you want a background placeholder behind the Chrome Custom Tab
        setContentView(R.layout.activity_payment);

        // Only launch on fresh creation to avoid infinite loops on configuration changes
        if (savedInstanceState == null) {
            launchCustomTab(NGROK_URL);
        }
    }

    private void launchCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true);
        builder.setInstantAppsEnabled(true);

        CustomTabsIntent customTabsIntent = builder.build();

        try {
            customTabsIntent.launchUrl(this, Uri.parse(url));
            customTabLaunched = true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Custom Tab: " + e.getMessage());
            Toast.makeText(this, "Browser konnte nicht gestartet werden", Toast.LENGTH_SHORT).show();
            finishPayment(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Triggered if the user manually taps the "X" or the system back button inside the Custom Tab
        if (customTabLaunched) {
            customTabLaunched = false;
            Log.d(TAG, "User closed Custom Tab manually. Canceling payment.");

            finishPayment(true);
        }
    }

    /**
     * Intercepts the redirect deep link from the AndroidManifest intent-filter
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            customTabLaunched = false; // Reset state flag
            handlePaymentCallback(uri);
        }
    }

    private void handlePaymentCallback(Uri uri) {
        Log.d(TAG, "Callback URL received: " + uri.toString());

        // Strategy A: Via Query parameter (e.g., return.php?status=SUCCESS)
        String statusParam = uri.getQueryParameter("status");

        // Strategy B: Via URL fragment (e.g., return.php#success)
        String fragment = uri.getFragment();

        if ("SUCCESS".equalsIgnoreCase(statusParam) || (fragment != null && fragment.contains("success"))) {
            Log.d(TAG, "Payment status parsed: SUCCESS");
            finishPayment(true);
        } else if ("FAILURE".equalsIgnoreCase(statusParam) || (fragment != null && fragment.contains("fail")) || (fragment != null && fragment.contains("notok"))) {
            Log.d(TAG, "Payment status parsed: FAILURE");
            finishPayment(false);
        } else {
            Log.w(TAG, "Unknown payment status, defaulting to failure.");
            finishPayment(false);
        }
    }

    private void finishPayment(boolean success) {
        if (success) {
            Intent intent = new Intent(PaymentActivityChromeCustomTabs.this, SuccessActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Zahlung fehlgeschlagen", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}