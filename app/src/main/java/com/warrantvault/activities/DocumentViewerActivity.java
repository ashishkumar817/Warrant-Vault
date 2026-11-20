package com.warrantvault.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.warrantvault.databinding.ActivityDocumentViewerBinding;

import java.io.File;

public class DocumentViewerActivity extends AppCompatActivity {

    private ActivityDocumentViewerBinding binding;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imagePath = getIntent().getStringExtra("image_path");
        String title = getIntent().getStringExtra("title");
        if (title != null) {
            binding.toolbar.setTitle(title);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (imagePath != null && new File(imagePath).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            binding.ivDocumentFull.setImageBitmap(bitmap);

            binding.btnShare.setOnClickListener(v -> shareDocument());
        } else {
            Toast.makeText(this, "Document preview unavailable", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void shareDocument() {
        try {
            File file = new File(imagePath);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Document"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to share file", Toast.LENGTH_SHORT).show();
        }
    }
}
