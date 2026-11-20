package com.warrantvault.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.warrantvault.databinding.ItemOnboardingSlideBinding;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder> {

    public static class SlideItem {
        public int illustrationResId;
        public String title;
        public String description;

        public SlideItem(int illustrationResId, String title, String description) {
            this.illustrationResId = illustrationResId;
            this.title = title;
            this.description = description;
        }
    }

    private final List<SlideItem> items;

    public OnboardingAdapter(List<SlideItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOnboardingSlideBinding binding = ItemOnboardingSlideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new SlideViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        private final ItemOnboardingSlideBinding binding;

        public SlideViewHolder(ItemOnboardingSlideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SlideItem item) {
            binding.ivIllustration.setImageResource(item.illustrationResId);
            binding.tvTitle.setText(item.title);
            binding.tvDescription.setText(item.description);
        }
    }
}
