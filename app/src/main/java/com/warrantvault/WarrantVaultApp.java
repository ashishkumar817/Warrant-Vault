package com.warrantvault;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

import com.warrantvault.utils.NotificationUtils;

public class WarrantVaultApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Prevent dark mode inversion from turning text white on white cards
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Pre-create notification channel on app startup
        NotificationUtils.createNotificationChannel(this);
    }
}
