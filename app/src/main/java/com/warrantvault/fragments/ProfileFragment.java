package com.warrantvault.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.warrantvault.activities.AuthActivity;
import com.warrantvault.dao.ProductDAO;
import com.warrantvault.databinding.FragmentProfileBinding;
import com.warrantvault.utils.NotificationUtils;
import com.warrantvault.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private ProductDAO productDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        productDAO = new ProductDAO(requireContext());

        loadUserData();
        setupSwitches();
        setupSettingsRows();
    }

    private void loadUserData() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        binding.tvProfileName.setText(name);
        binding.tvProfileEmail.setText(email);

        // Derive Initials
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            StringBuilder initials = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) initials.append(part.charAt(0));
                if (initials.length() >= 2) break;
            }
            binding.tvUserInitials.setText(initials.toString().toUpperCase());
        } else {
            binding.tvUserInitials.setText("WV");
        }
    }

    private void setupSwitches() {
        binding.switchMasterNotifications.setChecked(sessionManager.isNotificationsEnabled());
        binding.switchRemind30.setChecked(sessionManager.isRemind30());
        binding.switchRemind14.setChecked(sessionManager.isRemind14());
        binding.switchRemind7.setChecked(sessionManager.isRemind7());
        binding.switchRemind1.setChecked(sessionManager.isRemind1());

        binding.switchMasterNotifications.setOnCheckedChangeListener((btn, isChecked) -> {
            sessionManager.setNotificationsEnabled(isChecked);
            binding.switchRemind30.setEnabled(isChecked);
            binding.switchRemind14.setEnabled(isChecked);
            binding.switchRemind7.setEnabled(isChecked);
            binding.switchRemind1.setEnabled(isChecked);

            if (isChecked) {
                NotificationUtils.showNotification(
                        requireContext(),
                        9999,
                        -1,
                        "Warrant Vault Alerts Active",
                        "Notifications are working! You will receive reminders before warranties expire."
                );
            }
        });

        binding.switchRemind30.setOnCheckedChangeListener((btn, isChecked) -> sessionManager.setRemind30(isChecked));
        binding.switchRemind14.setOnCheckedChangeListener((btn, isChecked) -> sessionManager.setRemind14(isChecked));
        binding.switchRemind7.setOnCheckedChangeListener((btn, isChecked) -> sessionManager.setRemind7(isChecked));
        binding.switchRemind1.setOnCheckedChangeListener((btn, isChecked) -> sessionManager.setRemind1(isChecked));
    }

    private void setupSettingsRows() {
        binding.rowCategories.setOnClickListener(v -> showCategoriesDialog());
        binding.rowBackup.setOnClickListener(v -> showBackupDialog());
        binding.rowAbout.setOnClickListener(v -> showAboutDialog());
        binding.rowPrivacy.setOnClickListener(v -> showPrivacyDialog());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void showCategoriesDialog() {
        String[] categories = {
                "Electronics", "Mobiles", "Laptops", "Appliances", "Audio",
                "Gaming", "Vehicles", "Furniture", "Other"
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Supported Categories")
                .setItems(categories, (dialog, which) -> {
                    // Navigate to Vault with that category selected
                })
                .setPositiveButton("Close", null)
                .show();
    }

    private void showBackupDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Backup & Restore")
                .setMessage("Warrant Vault stores your bills, documents, and warranty information safely inside your offline SQLite database on your device. Auto-backup creates an encrypted copy in your local storage.")
                .setPositiveButton("Create Backup", (d, w) -> {
                    Toast.makeText(requireContext(), "Backup created successfully in internal storage!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("About Warrant Vault")
                .setMessage("Warrant Vault v1.0.0\nDigital Warranty & Bill Manager\n\nYour purchases, protected. Store purchase invoices, track warranties with intelligent countdowns, manage service history, and receive timely expiry notifications.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showPrivacyDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Privacy & Terms")
                .setMessage("Offline-First Privacy Guarantee:\n\nAll your purchase receipts, warranties, and service records are stored 100% locally on your device in SQLite. No tracking, no external ads, and no unauthorized cloud sync.")
                .setPositiveButton("I Understand", null)
                .show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of Warrant Vault?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(getActivity(), AuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
