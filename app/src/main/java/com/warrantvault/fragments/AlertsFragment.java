package com.warrantvault.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.warrantvault.R;
import com.warrantvault.activities.ProductDetailsActivity;
import com.warrantvault.adapters.NotificationAdapter;
import com.warrantvault.dao.NotificationDAO;
import com.warrantvault.databinding.FragmentAlertsBinding;
import com.warrantvault.models.NotificationItem;
import com.warrantvault.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {

    private FragmentAlertsBinding binding;
    private NotificationDAO notificationDAO;
    private SessionManager sessionManager;
    private NotificationAdapter adapter;

    private int selectedTab = 0; // 0: Upcoming, 1: Past, 2: All

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAlertsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationDAO = new NotificationDAO(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupAdapter();
        setupTabs();
        setupActions();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlerts();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadAlerts();
        }
    }

    private void setupAdapter() {
        adapter = new NotificationAdapter(item -> {
            notificationDAO.markAsRead(item.getId());
            if (item.getProductId() > 0) {
                Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                intent.putExtra("product_id", item.getProductId());
                startActivity(intent);
            }
        });

        binding.rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAlerts.setAdapter(adapter);
    }

    private void setupTabs() {
        binding.tabUpcoming.setOnClickListener(v -> switchTab(0));
        binding.tabPast.setOnClickListener(v -> switchTab(1));
        binding.tabAll.setOnClickListener(v -> switchTab(2));
    }

    private void switchTab(int tab) {
        selectedTab = tab;

        binding.tabUpcoming.setBackground(tab == 0 ? ContextCompat.getDrawable(requireContext(), R.drawable.bg_search_bar) : null);
        binding.tabUpcoming.setTextColor(ContextCompat.getColor(requireContext(), tab == 0 ? R.color.brand_blue : R.color.text_secondary));

        binding.tabPast.setBackground(tab == 1 ? ContextCompat.getDrawable(requireContext(), R.drawable.bg_search_bar) : null);
        binding.tabPast.setTextColor(ContextCompat.getColor(requireContext(), tab == 1 ? R.color.brand_blue : R.color.text_secondary));

        binding.tabAll.setBackground(tab == 2 ? ContextCompat.getDrawable(requireContext(), R.drawable.bg_search_bar) : null);
        binding.tabAll.setTextColor(ContextCompat.getColor(requireContext(), tab == 2 ? R.color.brand_blue : R.color.text_secondary));

        loadAlerts();
    }

    private void setupActions() {
        binding.btnMarkAllRead.setOnClickListener(v -> {
            notificationDAO.markAllAsRead(sessionManager.getUserId());
            loadAlerts();
        });
    }

    public void loadAlerts() {
        if (binding == null) return;
        long userId = sessionManager.getUserId();
        List<NotificationItem> all = notificationDAO.getAllNotifications(userId);
        List<NotificationItem> filtered = new ArrayList<>();

        for (NotificationItem item : all) {
            String msg = item.getMessage().toLowerCase();
            boolean isExpired = msg.contains("expired");

            if (selectedTab == 0) {
                // Upcoming
                if (!isExpired) filtered.add(item);
            } else if (selectedTab == 1) {
                // Past
                if (isExpired) filtered.add(item);
            } else {
                // All
                filtered.add(item);
            }
        }

        adapter.setItems(filtered);

        if (filtered.isEmpty()) {
            binding.llEmptyAlerts.setVisibility(View.VISIBLE);
            binding.rvAlerts.setVisibility(View.GONE);
        } else {
            binding.llEmptyAlerts.setVisibility(View.GONE);
            binding.rvAlerts.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
