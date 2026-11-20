package com.warrantvault.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

import com.warrantvault.databinding.ActivitySplashBinding;
import com.warrantvault.utils.SessionManager;
import com.warrantvault.workers.WarrantyReminderWorker;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Subtle professional entrance animation
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(900);
        binding.llBranding.startAnimation(fadeIn);

        // Schedule background warranty reminders
        WarrantyReminderWorker.schedulePeriodicReminders(getApplicationContext());

        // Navigate after brief delay
        new Handler(Looper.getMainLooper()).postDelayed(this::checkNavigation, 1400);
    }

    private void checkNavigation() {
        SessionManager sessionManager = new SessionManager(this);
        com.warrantvault.dao.UserDAO userDAO = new com.warrantvault.dao.UserDAO(this);

        Intent intent;
        if (sessionManager.isLoggedIn()) {
            if (userDAO.getUserById(sessionManager.getUserId()) != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                sessionManager.logout();
                intent = new Intent(SplashActivity.this, AuthActivity.class);
            }
        } else if (sessionManager.isOnboardingCompleted()) {
            intent = new Intent(SplashActivity.this, AuthActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
