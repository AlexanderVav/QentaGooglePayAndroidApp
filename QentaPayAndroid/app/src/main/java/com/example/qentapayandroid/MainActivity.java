package com.example.qentapayandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button payButton;
    public static final String NGROK_URL = "https://xib52gsaxp67.share.zrok.io";

    private final ActivityResultLauncher<Intent> paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri data = result.getData().getData();
                    handlePaymentResult(data);
                } else {
                    Toast.makeText(this, "Vorgang abgebrochen", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        payButton = findViewById(R.id.btnGooglePay);

        payButton.setOnClickListener(v -> {
            //switch for Webview vs Chrome Custom Tabs
            boolean isUsingWebView = false;
            Intent intent = null;

            if (isUsingWebView) {
                intent = new Intent(MainActivity.this, PaymentActivityWebView.class);
            }
            else {
                intent = new Intent(MainActivity.this, PaymentActivityChromeCustomTabs.class);
            }
            paymentLauncher.launch(intent);
        });
    }

    private void handlePaymentResult(Uri data) {
        if (data == null)
            return;

        String status = data.getQueryParameter("paymentState");
        if (status == null) {
            Toast.makeText(this, "Status unbekannt", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (status) {
            case "SUCCESS":
                Toast.makeText(this, "Bezahlung erfolgreich!", Toast.LENGTH_LONG).show();
                break;
            case "NOTOK":
                Toast.makeText(this, "Abbruch durch System", Toast.LENGTH_SHORT).show();
                break;
            case "FAILURE":
                Toast.makeText(this, "Fehler aufgetreten.", Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(this, "Vorgang beendet (" + status + ")", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}