package com.warrantvault.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.warrantvault.R;
import com.warrantvault.databinding.ItemExpiringSoonCardBinding;
import com.warrantvault.models.Product;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExpiringSoonAdapter extends RecyclerView.Adapter<ExpiringSoonAdapter.ViewHolder> {

    public interface OnExpiringProductClickListener {
        void onExpiringProductClick(Product product);
    }

    private final List<Product> list = new ArrayList<>();
    private final OnExpiringProductClickListener listener;

    public ExpiringSoonAdapter(OnExpiringProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.list.clear();
        if (products != null) {
            this.list.addAll(products);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpiringSoonCardBinding binding = ItemExpiringSoonCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemExpiringSoonCardBinding binding;

        public ViewHolder(ItemExpiringSoonCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product, OnExpiringProductClickListener listener) {
            Context context = itemView.getContext();
            binding.tvProductName.setText(product.getName());

            String pDate = (product.getPurchaseDate() != null) ? product.getPurchaseDate() : "";
            binding.tvPurchaseDate.setText("Purchased on " + pDate);

            long days = product.getDaysRemaining();
            if (days > 0) {
                binding.tvExpiryWarning.setText("Warranty expires in " + days + " days");
            } else {
                binding.tvExpiryWarning.setText("Warranty has expired");
            }

            // Category Icon
            String category = product.getCategory();
            if (product.getImagePath() != null && new File(product.getImagePath()).exists()) {
                try {
                    Bitmap bmp = BitmapFactory.decodeFile(product.getImagePath());
                    binding.ivIcon.setImageBitmap(bmp);
                    binding.ivIcon.setImageTintList(null);
                } catch (Exception e) {
                    setIconByCategory(category);
                }
            } else {
                setIconByCategory(category);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onExpiringProductClick(product);
            });
        }

        private void setIconByCategory(String category) {
            int iconRes = R.drawable.ic_cat_electronics;
            if ("Mobiles".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_mobiles;
            else if ("Laptops".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_laptops;
            else if ("Appliances".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_appliances;
            else if ("Audio".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_audio;
            else if ("Gaming".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_gaming;
            else if ("Vehicles".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_vehicles;
            else if ("Furniture".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_furniture;
            else if ("Other".equalsIgnoreCase(category)) iconRes = R.drawable.ic_cat_other;

            binding.ivIcon.setImageResource(iconRes);
            binding.ivIcon.setImageTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.status_expiring));
        }
    }
}
