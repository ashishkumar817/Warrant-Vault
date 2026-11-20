package com.warrantvault;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class WarrantVaultApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Prevent dark mode inversion from turning text white on white cards
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
