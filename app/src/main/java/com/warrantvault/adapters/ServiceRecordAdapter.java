package com.warrantvault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.warrantvault.R;
import com.warrantvault.databinding.ItemServiceRecordBinding;
import com.warrantvault.models.ServiceRecord;

import java.util.ArrayList;
import java.util.List;

public class ServiceRecordAdapter extends RecyclerView.Adapter<ServiceRecordAdapter.RecordViewHolder> {

    public interface OnRecordClickListener {
        void onRecordClick(ServiceRecord record);
    }

    private final List<ServiceRecord> records = new ArrayList<>();
    private OnRecordClickListener listener;

    public ServiceRecordAdapter() {
    }

    public ServiceRecordAdapter(OnRecordClickListener listener) {
        this.listener = listener;
    }

    public void setOnRecordClickListener(OnRecordClickListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<ServiceRecord> list) {
        this.records.clear();
        if (list != null) {
            this.records.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServiceRecordBinding binding = ItemServiceRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new RecordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        ServiceRecord record = records.get(position);
        holder.bind(record);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecordClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        private final ItemServiceRecordBinding binding;

        public RecordViewHolder(ItemServiceRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ServiceRecord record) {
            Context context = itemView.getContext();
            binding.tvServiceIssue.setText(record.getIssue());
            binding.tvServiceCenter.setText(record.getServiceCenter());
            binding.tvServiceDate.setText(record.getServiceDate());

            String status = record.getStatus();
            binding.tvServiceStatus.setText(status);

            if ("Completed".equalsIgnoreCase(status) || "Replaced".equalsIgnoreCase(status)) {
                binding.tvServiceStatus.setBackgroundResource(R.drawable.bg_badge_active);
                binding.tvServiceStatus.setTextColor(ContextCompat.getColor(context, R.color.status_active_text));
            } else if ("Repairing".equalsIgnoreCase(status) || "Under Inspection".equalsIgnoreCase(status)) {
                binding.tvServiceStatus.setBackgroundResource(R.drawable.bg_badge_expiring);
                binding.tvServiceStatus.setTextColor(ContextCompat.getColor(context, R.color.status_expiring_text));
            } else {
                binding.tvServiceStatus.setBackgroundResource(R.drawable.bg_summary_card_blue);
                binding.tvServiceStatus.setTextColor(ContextCompat.getColor(context, R.color.brand_blue));
            }

            if (record.getNotes() != null && !record.getNotes().trim().isEmpty()) {
                binding.tvServiceNotes.setText(record.getNotes());
                binding.tvServiceNotes.setVisibility(View.VISIBLE);
            } else {
                binding.tvServiceNotes.setVisibility(View.GONE);
            }
        }
    }
}
