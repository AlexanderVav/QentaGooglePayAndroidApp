package com.example.qentapayandroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class SuccessActivity extends AppCompatActivity {

    private KonfettiView konfettiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        // 1. Hide Action Bar for full immersion
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        konfettiView = findViewById(R.id.konfettiView);

        // 2. Start the Confetti Animation
        startConfetti();

        // 3. Jump to the next page after 3 seconds (3000ms)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Navigate back to MainActivity (or wherever you want)
            Intent intent = new Intent(SuccessActivity.this, MainActivity.class);

            // Clear the back stack so the user can't go back to the success screen
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish(); // Destroys this activity

        }, 3000);
    }

    private void startConfetti() {
        // Define a "Party" (Configuration for the particles)
        Party party = new PartyFactory(new Emitter(100, TimeUnit.MILLISECONDS).max(100))
                .spread(360)
                .shapes(Arrays.asList(nl.dionsegijn.konfetti.core.models.Shape.Square.INSTANCE, nl.dionsegijn.konfetti.core.models.Shape.Circle.INSTANCE))
                .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                .setSpeedBetween(0f, 30f)
                .position(new Position.Relative(0.5, 0.3)) // Center-Top of screen
                .build();

        konfettiView.start(party);
    }
}