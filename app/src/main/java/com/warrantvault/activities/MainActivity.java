package com.warrantvault.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.warrantvault.R;
import com.warrantvault.databinding.ActivityMainBinding;
import com.warrantvault.fragments.AddProductBottomSheet;
import com.warrantvault.fragments.AlertsFragment;
import com.warrantvault.fragments.HomeFragment;
import com.warrantvault.fragments.ProfileFragment;
import com.warrantvault.fragments.VaultFragment;
import com.warrantvault.utils.NotificationUtils;
import com.warrantvault.workers.WarrantyReminderWorker;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final HomeFragment homeFragment = new HomeFragment();
    private final VaultFragment vaultFragment = new VaultFragment();
    private final AlertsFragment alertsFragment = new AlertsFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private Fragment activeFragment = homeFragment;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    NotificationUtils.createNotificationChannel(this);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Create channel & request runtime permission on Android 13+ (API 33+)
        NotificationUtils.createNotificationChannel(this);
        checkNotificationPermission();

        // Ensure periodic warranty notifications worker is running
        WarrantyReminderWorker.schedulePeriodicReminders(this);

        setupFragments();
        setupNavigation();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupFragments() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragmentContainer, alertsFragment, "alerts").hide(alertsFragment)
                .add(R.id.fragmentContainer, vaultFragment, "vault").hide(vaultFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit();
        activeFragment = homeFragment;
    }

    private void setupNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.nav_vault) {
                switchFragment(vaultFragment);
                return true;
            } else if (itemId == R.id.nav_alerts) {
                switchFragment(alertsFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });

        // Center Prominent Add Button
        binding.fabAddProduct.setOnClickListener(v -> {
            new AddProductBottomSheet().show(getSupportFragmentManager(), "add_product_sheet");
        });
    }

    private void switchFragment(Fragment target) {
        if (activeFragment != target) {
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
        }
    }

    public void navigateToTab(int menuId) {
        binding.bottomNav.setSelectedItemId(menuId);
    }
}
