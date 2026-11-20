package com.warrantvault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.warrantvault.R;
import com.warrantvault.databinding.ItemAlertBinding;
import com.warrantvault.models.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.AlertViewHolder> {

    public interface OnAlertClickListener {
        void onAlertClick(NotificationItem item);
    }

    private final List<NotificationItem> items = new ArrayList<>();
    private final OnAlertClickListener listener;

    public NotificationAdapter(OnAlertClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<NotificationItem> list) {
        this.items.clear();
        if (list != null) {
            this.items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAlertBinding binding = ItemAlertBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AlertViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        private final ItemAlertBinding binding;

        public AlertViewHolder(ItemAlertBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NotificationItem item, OnAlertClickListener listener) {
            Context context = itemView.getContext();
            String title = (item.getProductName() != null && !item.getProductName().isEmpty())
                    ? item.getProductName()
                    : item.getTitle();
            binding.tvAlertTitle.setText(title);
            binding.tvAlertMessage.setText(item.getMessage());
            binding.tvAlertDate.setText(item.getNotificationDate());

            String lower = item.getMessage().toLowerCase();
            if (lower.contains("expired")) {
                binding.flAlertIndicator.setBackgroundResource(R.drawable.bg_summary_card_red);
                binding.ivAlertBell.setImageResource(R.drawable.ic_alert_triangle);
                binding.ivAlertBell.setImageTintList(ContextCompat.getColorStateList(context, R.color.status_expired));
                binding.tvAlertMessage.setTextColor(ContextCompat.getColor(context, R.color.status_expired_text));
            } else if (lower.contains("expires") || lower.contains("warning") || lower.contains("alert")) {
                binding.flAlertIndicator.setBackgroundResource(R.drawable.bg_summary_card_orange);
                binding.ivAlertBell.setImageResource(R.drawable.ic_clock);
                binding.ivAlertBell.setImageTintList(ContextCompat.getColorStateList(context, R.color.status_expiring));
                binding.tvAlertMessage.setTextColor(ContextCompat.getColor(context, R.color.status_expiring_text));
            } else {
                binding.flAlertIndicator.setBackgroundResource(R.drawable.bg_summary_card_green);
                binding.ivAlertBell.setImageResource(R.drawable.ic_shield_check);
                binding.ivAlertBell.setImageTintList(ContextCompat.getColorStateList(context, R.color.status_active));
                binding.tvAlertMessage.setTextColor(ContextCompat.getColor(context, R.color.status_active_text));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAlertClick(item);
            });
        }
    }
}
