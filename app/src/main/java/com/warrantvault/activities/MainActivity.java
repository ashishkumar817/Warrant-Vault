package com.warrantvault.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
import com.warrantvault.workers.WarrantyReminderWorker;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final HomeFragment homeFragment = new HomeFragment();
    private final VaultFragment vaultFragment = new VaultFragment();
    private final AlertsFragment alertsFragment = new AlertsFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ensure periodic warranty notifications worker is running
        WarrantyReminderWorker.schedulePeriodicReminders(this);

        setupFragments();
        setupNavigation();
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
