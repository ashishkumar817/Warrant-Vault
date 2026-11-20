package com.warrantvault.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.warrantvault.R;
import com.warrantvault.activities.ProductDetailsActivity;
import com.warrantvault.activities.VerifyProductActivity;
import com.warrantvault.adapters.ProductAdapter;
import com.warrantvault.dao.ProductDAO;
import com.warrantvault.databinding.FragmentVaultBinding;
import com.warrantvault.models.Product;
import com.warrantvault.utils.SessionManager;

import java.util.Collections;
import java.util.List;

public class VaultFragment extends Fragment {

    private FragmentVaultBinding binding;
    private ProductDAO productDAO;
    private SessionManager sessionManager;
    private ProductAdapter adapter;

    private String selectedCategory = "All";
    private String currentSearchQuery = "";
    private int currentSortIndex = 0; // 0: Expiry, 1: Name, 2: Date, 3: Price

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVaultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productDAO = new ProductDAO(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupAdapter();
        setupSearch();
        setupCategoryChips();
        setupSortFilter();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVaultProducts();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadVaultProducts();
        }
    }

    private void setupAdapter() {
        adapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onProductEdit(Product product) {
                Intent intent = new Intent(getActivity(), VerifyProductActivity.class);
                intent.putExtra("edit_product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onProductDelete(Product product) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Product")
                        .setMessage("Delete " + product.getName() + " from Vault?")
                        .setPositiveButton("Delete", (d, w) -> {
                            productDAO.deleteProduct(product.getId());
                            loadVaultProducts();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.rvVaultProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvVaultProducts.setAdapter(adapter);

        binding.btnEmptyAddProduct.setOnClickListener(v -> {
            new AddProductBottomSheet().show(getParentFragmentManager(), "add_product_sheet");
        });
    }

    private void setupSearch() {
        binding.etVaultSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                binding.btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                loadVaultProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etVaultSearch.setText("");
        });
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedCategory = chip.getText().toString();
                    loadVaultProducts();
                }
            }
        });
    }

    private void setupSortFilter() {
        String[] sortOptions = {"Warranty Expiry (Soonest first)", "Product Name (A-Z)", "Purchase Date (Newest first)", "Purchase Price (High to Low)"};
        binding.btnSortFilter.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Sort Products")
                    .setSingleChoiceItems(sortOptions, currentSortIndex, (dialog, which) -> {
                        currentSortIndex = which;
                        loadVaultProducts();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    public void loadVaultProducts() {
        if (binding == null) return;
        long userId = sessionManager.getUserId();
        List<Product> list;

        if (!currentSearchQuery.isEmpty()) {
            list = productDAO.searchProducts(userId, currentSearchQuery);
            if (!"All".equalsIgnoreCase(selectedCategory)) {
                list.removeIf(p -> !selectedCategory.equalsIgnoreCase(p.getCategory()));
            }
        } else if (!"All".equalsIgnoreCase(selectedCategory)) {
            list = productDAO.getProductsByCategory(userId, selectedCategory);
        } else {
            list = productDAO.getAllProducts(userId);
        }

        // Apply Sorting
        if (currentSortIndex == 0) {
            // Expiry soonest first
            Collections.sort(list, (a, b) -> Long.compare(a.getDaysRemaining(), b.getDaysRemaining()));
        } else if (currentSortIndex == 1) {
            // Name A-Z
            Collections.sort(list, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        } else if (currentSortIndex == 2) {
            // Purchase date newest
            Collections.sort(list, (a, b) -> Long.compare(b.getId(), a.getId()));
        } else if (currentSortIndex == 3) {
            // Price high to low
            Collections.sort(list, (a, b) -> Double.compare(b.getPurchasePrice(), a.getPurchasePrice()));
        }

        adapter.setProducts(list);
        binding.tvProductCountBadge.setText(list.size() + (list.size() == 1 ? " item" : " items"));

        if (list.isEmpty()) {
            binding.llEmptyVault.setVisibility(View.VISIBLE);
            binding.rvVaultProducts.setVisibility(View.GONE);
        } else {
            binding.llEmptyVault.setVisibility(View.GONE);
            binding.rvVaultProducts.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
