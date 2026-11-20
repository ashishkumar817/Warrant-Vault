package com.warrantvault.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.warrantvault.R;
import com.warrantvault.activities.AddProductActivity;
import com.warrantvault.activities.ScanBillActivity;
import com.warrantvault.activities.VerifyProductActivity;
import com.warrantvault.databinding.BottomSheetAddProductBinding;
import com.warrantvault.utils.FileUtils;

public class AddProductBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddProductBinding binding;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_WarrantVault_BottomSheetDialog);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && getContext() != null) {
                        String localPath = FileUtils.copyUriToInternalStorage(getContext(), uri, "UPLOADED_BILL");
                        Intent intent = new Intent(getActivity(), VerifyProductActivity.class);
                        intent.putExtra("bill_image_path", localPath);
                        intent.putExtra("run_ocr", true);
                        startActivity(intent);
                        dismiss();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Scan Bill (Camera)
        binding.cardScanBill.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanBillActivity.class);
            startActivity(intent);
            dismiss();
        });

        // 2. Upload Bill (Gallery/Storage)
        binding.cardUploadBill.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        // 3. Add Manually
        binding.cardAddManually.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddProductActivity.class);
            startActivity(intent);
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
