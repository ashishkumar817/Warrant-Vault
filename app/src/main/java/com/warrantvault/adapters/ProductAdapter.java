package com.warrantvault.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.warrantvault.R;
import com.warrantvault.databinding.ItemProductCardBinding;
import com.warrantvault.models.Product;
import com.warrantvault.utils.WarrantyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onProductEdit(Product product);
        void onProductDelete(Product product);
    }

    private final List<Product> productList = new ArrayList<>();
    private final OnProductClickListener listener;

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.productList.clear();
        if (products != null) {
            this.productList.addAll(products);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductCardBinding binding = ItemProductCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductCardBinding binding;

        public ProductViewHolder(ItemProductCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product, OnProductClickListener listener) {
            Context context = itemView.getContext();

            binding.tvProductName.setText(product.getName());

            String brand = (product.getBrand() != null && !product.getBrand().isEmpty()) ? product.getBrand() : "Brand";
            String category = (product.getCategory() != null && !product.getCategory().isEmpty()) ? product.getCategory() : "Product";
            binding.tvBrandCategory.setText(brand + " • " + category);

            // Icon by Category or Custom Image
            if (product.getImagePath() != null && new File(product.getImagePath()).exists()) {
                try {
                    Bitmap bmp = BitmapFactory.decodeFile(product.getImagePath());
                    binding.ivProductIcon.setImageBitmap(bmp);
                    binding.ivProductIcon.setImageTintList(null);
                } catch (Exception e) {
                    setCategoryIcon(category);
                }
            } else {
                setCategoryIcon(category);
            }

            // Status Styling
            String status = product.getStatus();
            long days = product.getDaysRemaining();

            if (WarrantyUtils.STATUS_ACTIVE.equals(status)) {
                binding.tvWarrantyStatus.setText(context.getString(R.string.warranty_active));
                binding.tvWarrantyStatus.setBackgroundResource(R.drawable.bg_badge_active);
                binding.tvWarrantyStatus.setTextColor(ContextCompat.getColor(context, R.color.status_active_text));
                binding.tvDaysRemaining.setText(days + " days remaining");
                binding.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.status_active_text));
            } else if (WarrantyUtils.STATUS_EXPIRING.equals(status)) {
                binding.tvWarrantyStatus.setText(context.getString(R.string.warranty_expiring_soon));
                binding.tvWarrantyStatus.setBackgroundResource(R.drawable.bg_badge_expiring);
                binding.tvWarrantyStatus.setTextColor(ContextCompat.getColor(context, R.color.status_expiring_text));
                binding.tvDaysRemaining.setText("Expires in " + days + " days");
                binding.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.status_expiring_text));
            } else if (WarrantyUtils.STATUS_NO_WARRANTY.equals(status)) {
                binding.tvWarrantyStatus.setText(context.getString(R.string.warranty_none));
                binding.tvWarrantyStatus.setBackgroundResource(R.drawable.bg_badge_expired);
                binding.tvWarrantyStatus.setTextColor(ContextCompat.getColor(context, R.color.status_expired_text));
                binding.tvDaysRemaining.setText("No Warranty");
                binding.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            } else {
                binding.tvWarrantyStatus.setText(context.getString(R.string.warranty_expired));
                binding.tvWarrantyStatus.setBackgroundResource(R.drawable.bg_badge_expired);
                binding.tvWarrantyStatus.setTextColor(ContextCompat.getColor(context, R.color.status_expired_text));
                binding.tvDaysRemaining.setText("Warranty Expired");
                binding.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.status_expired_text));
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });

            // Overflow menu (Edit, Delete)
            binding.btnMoreOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, binding.btnMoreOptions);
                popup.getMenu().add(0, 1, 0, context.getString(R.string.edit_product));
                popup.getMenu().add(0, 2, 1, context.getString(R.string.delete_product));
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        if (listener != null) listener.onProductEdit(product);
                        return true;
                    } else if (item.getItemId() == 2) {
                        if (listener != null) listener.onProductDelete(product);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        private void setCategoryIcon(String category) {
            int iconRes = R.drawable.ic_cat_electronics;
            if ("Mobiles".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_mobiles;
            else if ("Laptops".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_laptops;
            else if ("Appliances".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_appliances;
            else if ("Audio".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_audio;
            else if ("Gaming".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_gaming;
            else if ("Vehicles".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_vehicles;
            else if ("Furniture".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_furniture;
            else if ("Other".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_other;

            binding.ivProductIcon.setImageResource(iconRes);
            binding.ivProductIcon.setImageTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.brand_blue));
        }
    }
}
