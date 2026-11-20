package com.warrantvault.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.warrantvault.R;
import com.warrantvault.databinding.ActivityScanBillBinding;
import com.warrantvault.utils.BillOcrParser;
import com.warrantvault.utils.FileUtils;

import java.io.File;

public class ScanBillActivity extends AppCompatActivity {

    private ActivityScanBillBinding binding;
    private File photoFile;
    private Uri photoUri;
    private boolean isFlashOn = false;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBillBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupLaunchers();
        setupClickListeners();
    }

    private void setupLaunchers() {
        // Camera picture launcher
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && photoFile != null && photoFile.exists()) {
                        processImageForOcr(photoFile.getAbsolutePath());
                    } else {
                        Toast.makeText(this, "Capture cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Gallery picker launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String localPath = FileUtils.copyUriToInternalStorage(this, uri, "SCANNED_BILL");
                        if (localPath != null) {
                            processImageForOcr(localPath);
                        }
                    }
                }
        );

        // Permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required to scan bills", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnFlash.setOnClickListener(v -> {
            isFlashOn = !isFlashOn;
            binding.btnFlash.setImageResource(isFlashOn ? R.drawable.ic_flash_off : R.drawable.ic_flash_on);
            Toast.makeText(this, isFlashOn ? "Flash ON" : "Flash OFF", Toast.LENGTH_SHORT).show();
        });

        binding.btnCapture.setOnClickListener(v -> checkPermissionAndLaunchCamera());

        binding.btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        binding.btnCameraHelp.setOnClickListener(v -> showDemoScanDialog());
    }

    private void checkPermissionAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            photoFile = FileUtils.createImageFile(this);
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(photoUri);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback for emulator without fileprovider setup
            showDemoScanDialog();
        }
    }

    private void showDemoScanDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Bill Scanner")
                .setMessage("Would you like to scan a sample Samsung Galaxy S25 / Dell Monitor invoice to test ML Kit Text Recognition?")
                .setPositiveButton("Scan Demo Bill", (d, w) -> generateAndProcessSampleBill())
                .setNeutralButton("Choose from Gallery", (d, w) -> galleryLauncher.launch("image/*"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void generateAndProcessSampleBill() {
        binding.flProcessing.setVisibility(View.VISIBLE);

        // Generate a synthetic invoice bitmap for offline testing
        Bitmap bitmap = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(22);
        paint.setAntiAlias(true);

        canvas.drawText("Samsung Experience Store", 40, 60, paint);
        canvas.drawText("MG Road, Bengaluru", 40, 90, paint);
        canvas.drawText("Phone: 080-12345678", 40, 120, paint);
        canvas.drawText("------------------------------------", 40, 150, paint);
        canvas.drawText("Invoice No: INV-98273", 40, 190, paint);
        canvas.drawText("Date: 12 Aug 2026", 40, 220, paint);
        canvas.drawText("Customer: Ashish Kumar", 40, 250, paint);
        canvas.drawText("------------------------------------", 40, 280, paint);
        canvas.drawText("Product: Samsung Galaxy S25", 40, 320, paint);
        canvas.drawText("Model: SM-S931B", 40, 350, paint);
        canvas.drawText("Serial No: SM-98273", 40, 380, paint);
        canvas.drawText("Warranty: 12 Months", 40, 410, paint);
        canvas.drawText("Price: ₹79,999", 40, 440, paint);
        canvas.drawText("Total: ₹79,999.00", 40, 480, paint);
        canvas.drawText("------------------------------------", 40, 520, paint);
        canvas.drawText("Thank you for your purchase!", 40, 560, paint);

        String savedPath = FileUtils.saveBitmapToFile(this, bitmap);
        processImageForOcr(savedPath);
    }

    private void processImageForOcr(String imagePath) {
        binding.flProcessing.setVisibility(View.VISIBLE);

        try {
            File imgFile = new File(imagePath);
            Uri uri = Uri.fromFile(imgFile);
            InputImage inputImage = InputImage.fromFilePath(this, uri);

            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(inputImage)
                    .addOnSuccessListener(visionText -> {
                        binding.flProcessing.setVisibility(View.GONE);
                        String text = visionText.getText();
                        BillOcrParser.ExtractedBillData extracted = BillOcrParser.parse(text);

                        Intent intent = new Intent(ScanBillActivity.this, VerifyProductActivity.class);
                        intent.putExtra("bill_image_path", imagePath);
                        intent.putExtra("product_name", extracted.productName);
                        intent.putExtra("brand", extracted.brand);
                        intent.putExtra("category", extracted.category);
                        intent.putExtra("model_number", extracted.modelNumber);
                        intent.putExtra("serial_number", extracted.serialNumber);
                        intent.putExtra("purchase_date", extracted.purchaseDate);
                        intent.putExtra("purchase_price", extracted.purchasePrice);
                        intent.putExtra("seller", extracted.seller);
                        intent.putExtra("invoice_number", extracted.invoiceNumber);
                        intent.putExtra("warranty_period", extracted.warrantyPeriod);
                        intent.putExtra("is_ocr_extracted", true);

                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        binding.flProcessing.setVisibility(View.GONE);
                        Toast.makeText(this, "OCR failed: " + e.getMessage() + ". You can enter details manually.", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(ScanBillActivity.this, VerifyProductActivity.class);
                        intent.putExtra("bill_image_path", imagePath);
                        startActivity(intent);
                        finish();
                    });
        } catch (Exception e) {
            binding.flProcessing.setVisibility(View.GONE);
            e.printStackTrace();
            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
