package io.github.abdelkarimain.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.github.abdelkarimain.auth.utils.ThemePreferences;

public class SplashActivity extends AppCompatActivity {

    View mContentView;
    LottieAnimationView lottieLoader;
    FirebaseAuth auth;
    FirebaseUser user;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate and setContentView
        boolean isDarkMode = ThemePreferences.isDarkMode(this);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        lottieLoader = findViewById(R.id.lottieLoader);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        mContentView = findViewById(R.id.lottieLoader);

        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user != null) {
                    if (user.isEmailVerified()) {
                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }
        }, 3000);
    }
}