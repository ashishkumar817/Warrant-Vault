package com.warrantvault.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.warrantvault.R;
import com.warrantvault.databinding.ItemDocumentBinding;
import com.warrantvault.models.Document;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocViewHolder> {

    public interface OnDocumentActionListener {
        void onDocumentOpen(Document document);
        void onDocumentDelete(Document document);
    }

    private final List<Document> documents = new ArrayList<>();
    private final OnDocumentActionListener listener;

    public DocumentAdapter(OnDocumentActionListener listener) {
        this.listener = listener;
    }

    public void setDocuments(List<Document> docs) {
        this.documents.clear();
        if (docs != null) {
            this.documents.addAll(docs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDocumentBinding binding = ItemDocumentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new DocViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DocViewHolder holder, int position) {
        holder.bind(documents.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {
        private final ItemDocumentBinding binding;

        public DocViewHolder(ItemDocumentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Document document, OnDocumentActionListener listener) {
            binding.tvDocTitle.setText(document.getDocumentType());

            String size = (document.getFileSize() != null) ? document.getFileSize() : "Document";
            String date = (document.getCreatedAt() != null) ? document.getCreatedAt() : "";
            binding.tvDocSubtitle.setText(date + " • " + size);

            if ("Purchase Invoice".equalsIgnoreCase(document.getDocumentType())) {
                binding.ivDocIcon.setImageResource(R.drawable.ic_document);
            } else if ("Warranty Card".equalsIgnoreCase(document.getDocumentType())) {
                binding.ivDocIcon.setImageResource(R.drawable.ic_shield_check);
            } else {
                binding.ivDocIcon.setImageResource(R.drawable.ic_edit_note);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onDocumentOpen(document);
            });

            binding.btnDocDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDocumentDelete(document);
            });
        }
    }
}
