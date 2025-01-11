package io.github.abdelkarimain.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import io.github.abdelkarimain.auth.utils.ThemePreferences;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate and setContentView
        boolean isDarkMode = ThemePreferences.isDarkMode(this);
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGoToLogin = findViewById(R.id.btnGoToLogin);
        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}