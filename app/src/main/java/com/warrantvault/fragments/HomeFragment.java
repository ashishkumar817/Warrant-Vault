package com.warrantvault.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.warrantvault.R;
import com.warrantvault.activities.MainActivity;
import com.warrantvault.activities.ProductDetailsActivity;
import com.warrantvault.activities.VerifyProductActivity;
import com.warrantvault.adapters.ExpiringSoonAdapter;
import com.warrantvault.adapters.ProductAdapter;
import com.warrantvault.dao.NotificationDAO;
import com.warrantvault.dao.ProductDAO;
import com.warrantvault.databinding.FragmentHomeBinding;
import com.warrantvault.models.Product;
import com.warrantvault.utils.SessionManager;

import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ProductDAO productDAO;
    private NotificationDAO notificationDAO;
    private SessionManager sessionManager;

    private ExpiringSoonAdapter expiringAdapter;
    private ProductAdapter recentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productDAO = new ProductDAO(requireContext());
        notificationDAO = new NotificationDAO(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupGreeting();
        setupAdapters();
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHomeData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadHomeData();
        }
    }

    private void setupGreeting() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String greeting = "Good morning";
        if (hour >= 12 && hour < 17) {
            greeting = "Good afternoon";
        } else if (hour >= 17) {
            greeting = "Good evening";
        }

        String name = sessionManager.getUserName();
        if (name == null || name.isEmpty()) name = "Ashish";
        binding.tvGreeting.setText(greeting + ", " + name + " 👋");
    }

    private void setupAdapters() {
        expiringAdapter = new ExpiringSoonAdapter(product -> openProductDetails(product.getId()));
        binding.rvExpiringSoon.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpiringSoon.setAdapter(expiringAdapter);

        recentAdapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                openProductDetails(product.getId());
            }

            @Override
            public void onProductEdit(Product product) {
                Intent intent = new Intent(getActivity(), VerifyProductActivity.class);
                intent.putExtra("edit_product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onProductDelete(Product product) {
                productDAO.deleteProduct(product.getId());
                loadHomeData();
            }
        });
        binding.rvRecentlyAdded.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentlyAdded.setAdapter(recentAdapter);
    }

    private void setupClickListeners() {
        binding.btnHomeAddProduct.setOnClickListener(v -> {
            new AddProductBottomSheet().show(getParentFragmentManager(), "add_product_sheet");
        });

        binding.flNotificationBell.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_alerts);
            }
        });

        binding.flSearchBar.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_vault);
            }
        });

        binding.btnSeeAllExpiring.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_alerts);
            }
        });

        binding.btnSeeAllRecent.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_vault);
            }
        });

        binding.cardTotalProducts.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).navigateToTab(R.id.nav_vault);
        });
        binding.cardActiveProducts.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).navigateToTab(R.id.nav_vault);
        });
        binding.cardExpiringProducts.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).navigateToTab(R.id.nav_alerts);
        });
        binding.cardExpiredProducts.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).navigateToTab(R.id.nav_alerts);
        });
    }

    public void loadHomeData() {
        if (binding == null) return;
        long userId = sessionManager.getUserId();

        // 1. Unread notifications count
        int unread = notificationDAO.getUnreadCount(userId);
        if (unread > 0) {
            binding.tvUnreadBadge.setVisibility(View.VISIBLE);
            binding.tvUnreadBadge.setText(String.valueOf(unread));
        } else {
            binding.tvUnreadBadge.setVisibility(View.GONE);
        }

        // 2. Summary counts
        ProductDAO.ProductCounts counts = productDAO.getProductCounts(userId);
        binding.tvCountTotal.setText(String.valueOf(counts.total));
        binding.tvCountActive.setText(String.valueOf(counts.active));
        binding.tvCountExpiring.setText(String.valueOf(counts.expiringSoon));
        binding.tvCountExpired.setText(String.valueOf(counts.expired));

        // 3. Expiring soon
        List<Product> expiringList = productDAO.getExpiringSoonProducts(userId, 30);
        expiringAdapter.setProducts(expiringList);
        binding.llExpiringSection.setVisibility(expiringList.isEmpty() ? View.GONE : View.VISIBLE);
        binding.rvExpiringSoon.setVisibility(expiringList.isEmpty() ? View.GONE : View.VISIBLE);

        // 4. Recently added (limit 5)
        List<Product> recentList = productDAO.getRecentProducts(userId, 5);
        recentAdapter.setProducts(recentList);
    }

    private void openProductDetails(long prodId) {
        Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
        intent.putExtra("product_id", prodId);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
