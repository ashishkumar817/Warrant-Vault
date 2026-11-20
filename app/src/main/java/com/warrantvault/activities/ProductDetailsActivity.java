package com.warrantvault.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.warrantvault.R;
import com.warrantvault.adapters.DocumentAdapter;
import com.warrantvault.adapters.ServiceRecordAdapter;
import com.warrantvault.dao.DocumentDAO;
import com.warrantvault.dao.ProductDAO;
import com.warrantvault.dao.ServiceRecordDAO;
import com.warrantvault.databinding.ActivityProductDetailsBinding;
import com.warrantvault.models.Document;
import com.warrantvault.models.Product;
import com.warrantvault.models.ServiceRecord;
import com.warrantvault.models.Warranty;
import com.warrantvault.utils.DateUtils;
import com.warrantvault.utils.FileUtils;
import com.warrantvault.utils.WarrantyUtils;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {

    private ActivityProductDetailsBinding binding;
    private ProductDAO productDAO;
    private DocumentDAO documentDAO;
    private ServiceRecordDAO serviceRecordDAO;

    private long productId = -1;
    private Product currentProduct;

    private DocumentAdapter documentAdapter;
    private ServiceRecordAdapter serviceRecordAdapter;

    private String pendingDocPath = null;
    private TextView pendingStatusView = null;
    private ActivityResultLauncher<String> docPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productDAO = new ProductDAO(this);
        documentDAO = new DocumentDAO(this);
        serviceRecordDAO = new ServiceRecordDAO(this);

        setupToolbar();
        setupLaunchers();
        setupAdapters();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductDetails();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            openEditProduct();
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            confirmDeleteProduct();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupLaunchers() {
        docPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pendingDocPath = FileUtils.copyUriToInternalStorage(this, uri, "DOC_ATTACHMENT");
                        if (pendingStatusView != null && pendingDocPath != null) {
                            pendingStatusView.setVisibility(View.VISIBLE);
                            pendingStatusView.setText("Selected: " + new File(pendingDocPath).getName());
                        }
                    }
                }
        );
    }

    private void setupAdapters() {
        documentAdapter = new DocumentAdapter(new DocumentAdapter.OnDocumentActionListener() {
            @Override
            public void onDocumentOpen(Document document) {
                openDocument(document);
            }

            @Override
            public void onDocumentDelete(Document document) {
                confirmDeleteDocument(document);
            }
        });
        binding.rvDocuments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDocuments.setAdapter(documentAdapter);

        serviceRecordAdapter = new ServiceRecordAdapter(this::showEditServiceRecordDialog);
        binding.rvServiceHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvServiceHistory.setAdapter(serviceRecordAdapter);
    }

    private void setupClickListeners() {
        binding.btnAddDocument.setOnClickListener(v -> showAddDocumentDialog());
        binding.btnAddServiceRecord.setOnClickListener(v -> showAddServiceRecordDialog());
        binding.btnEditProduct.setOnClickListener(v -> openEditProduct());
        binding.btnDeleteProduct.setOnClickListener(v -> confirmDeleteProduct());
    }

    private void loadProductDetails() {
        currentProduct = productDAO.getProductById(productId);
        if (currentProduct == null) {
            finish();
            return;
        }

        binding.tvProductName.setText(currentProduct.getName());
        String brand = (currentProduct.getBrand() != null && !currentProduct.getBrand().isEmpty()) ? currentProduct.getBrand() : "Brand";
        String category = (currentProduct.getCategory() != null && !currentProduct.getCategory().isEmpty()) ? currentProduct.getCategory() : "Product";
        binding.tvBrandCategory.setText(brand + " • " + category);

        // Icon or custom image
        if (currentProduct.getImagePath() != null && new File(currentProduct.getImagePath()).exists()) {
            try {
                Bitmap bmp = BitmapFactory.decodeFile(currentProduct.getImagePath());
                binding.ivProductImage.setImageBitmap(bmp);
                binding.ivProductImage.setImageTintList(null);
            } catch (Exception e) {
                setCategoryIcon(category);
            }
        } else {
            setCategoryIcon(category);
        }

        // Warranty Hero
        Warranty warranty = currentProduct.getWarranty();
        String status = currentProduct.getStatus();
        long days = currentProduct.getDaysRemaining();

        if (warranty != null) {
            binding.tvTimelineStart.setText(warranty.getStartDate());
            binding.tvTimelineEnd.setText(warranty.getEndDate());
        }

        binding.progressWarranty.setProgress(currentProduct.getElapsedPercentage());

        if (WarrantyUtils.STATUS_ACTIVE.equals(status)) {
            binding.tvStatusBadge.setText(R.string.warranty_active);
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_active);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_active_text));
            binding.tvDaysRemainingHero.setText(days + " DAYS REMAINING");
            binding.tvDaysRemainingHero.setTextColor(ContextCompat.getColor(this, R.color.status_active_text));
            binding.progressWarranty.setIndicatorColor(ContextCompat.getColor(this, R.color.status_active));
            binding.llWarrantyCardBg.setBackgroundResource(R.drawable.bg_summary_card_green);
        } else if (WarrantyUtils.STATUS_EXPIRING.equals(status)) {
            binding.tvStatusBadge.setText(R.string.warranty_expiring_soon);
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_expiring);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_expiring_text));
            binding.tvDaysRemainingHero.setText(days + " DAYS REMAINING");
            binding.tvDaysRemainingHero.setTextColor(ContextCompat.getColor(this, R.color.status_expiring_text));
            binding.progressWarranty.setIndicatorColor(ContextCompat.getColor(this, R.color.status_expiring));
            binding.llWarrantyCardBg.setBackgroundResource(R.drawable.bg_summary_card_orange);
        } else if (WarrantyUtils.STATUS_NO_WARRANTY.equals(status)) {
            binding.tvStatusBadge.setText(R.string.warranty_none);
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_expired);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_expired_text));
            binding.tvDaysRemainingHero.setText("NO WARRANTY");
            binding.tvDaysRemainingHero.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            binding.progressWarranty.setIndicatorColor(ContextCompat.getColor(this, R.color.text_hint));
            binding.progressWarranty.setProgress(0);
            binding.llWarrantyCardBg.setBackgroundResource(R.drawable.bg_summary_card_red);
        } else {
            binding.tvStatusBadge.setText(R.string.warranty_expired);
            binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_expired);
            binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_expired_text));
            binding.tvDaysRemainingHero.setText("EXPIRED");
            binding.tvDaysRemainingHero.setTextColor(ContextCompat.getColor(this, R.color.status_expired_text));
            binding.progressWarranty.setIndicatorColor(ContextCompat.getColor(this, R.color.status_expired));
            binding.llWarrantyCardBg.setBackgroundResource(R.drawable.bg_summary_card_red);
        }

        // Purchase Details
        binding.tvDetailPurchaseDate.setText(currentProduct.getPurchaseDate() != null ? currentProduct.getPurchaseDate() : "-");
        if (currentProduct.getPurchasePrice() > 0) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            binding.tvDetailPurchasePrice.setText(nf.format(currentProduct.getPurchasePrice()));
        } else {
            binding.tvDetailPurchasePrice.setText("-");
        }
        binding.tvDetailSeller.setText(currentProduct.getSeller() != null && !currentProduct.getSeller().isEmpty() ? currentProduct.getSeller() : "-");
        binding.tvDetailInvoiceNumber.setText(currentProduct.getInvoiceNumber() != null && !currentProduct.getInvoiceNumber().isEmpty() ? currentProduct.getInvoiceNumber() : "-");
        binding.tvDetailModelNumber.setText(currentProduct.getModelNumber() != null && !currentProduct.getModelNumber().isEmpty() ? currentProduct.getModelNumber() : "-");
        binding.tvDetailSerialNumber.setText(currentProduct.getSerialNumber() != null && !currentProduct.getSerialNumber().isEmpty() ? currentProduct.getSerialNumber() : "-");

        // Load Documents
        List<Document> docs = documentDAO.getDocumentsByProductId(productId);
        documentAdapter.setDocuments(docs);
        binding.tvEmptyDocuments.setVisibility(docs.isEmpty() ? View.VISIBLE : View.GONE);

        // Load Service History
        List<ServiceRecord> records = serviceRecordDAO.getRecordsByProductId(productId);
        serviceRecordAdapter.setRecords(records);
        binding.tvEmptyServiceHistory.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
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

        binding.ivProductImage.setImageResource(iconRes);
        binding.ivProductImage.setImageTintList(ContextCompat.getColorStateList(this, R.color.brand_blue));
    }

    private void openEditProduct() {
        Intent intent = new Intent(this, VerifyProductActivity.class);
        intent.putExtra("edit_product_id", productId);
        startActivity(intent);
    }

    private void confirmDeleteProduct() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + currentProduct.getName() + " and all its documents and service history?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    productDAO.deleteProduct(productId);
                    Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openDocument(Document doc) {
        if (doc.getFilePath() != null && new File(doc.getFilePath()).exists()) {
            Intent intent = new Intent(this, DocumentViewerActivity.class);
            intent.putExtra("image_path", doc.getFilePath());
            intent.putExtra("title", doc.getDocumentType());
            startActivity(intent);
        } else {
            Toast.makeText(this, doc.getDocumentType() + " is saved in Warrant Vault (" + doc.getCreatedAt() + ")", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteDocument(Document doc) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Document")
                .setMessage("Delete " + doc.getDocumentType() + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    documentDAO.deleteDocument(doc.getId());
                    loadProductDetails();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddDocumentDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_document, null);
        com.google.android.material.textfield.MaterialAutoCompleteTextView actvDocType = view.findViewById(R.id.actvDocType);
        View btnSelectFile = view.findViewById(R.id.btnSelectFile);
        pendingStatusView = view.findViewById(R.id.tvSelectedFileStatus);
        pendingDocPath = null;

        String[] docTypes = {"Purchase Invoice", "Warranty Card", "User Manual", "Extended Warranty", "Service Invoice", "Other"};
        actvDocType.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown_menu, docTypes));
        actvDocType.setText(docTypes[0], false);

        btnSelectFile.setOnClickListener(v -> docPickerLauncher.launch("image/*"));

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Attach", (dialog, which) -> {
                    String type = actvDocType.getText().toString().trim();
                    String path = (pendingDocPath != null) ? pendingDocPath : "doc_" + System.currentTimeMillis();
                    Document doc = new Document(productId, type, path, DateUtils.getTodayDisplay());
                    documentDAO.insertDocument(doc);
                    Toast.makeText(this, "Document attached", Toast.LENGTH_SHORT).show();
                    loadProductDetails();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showAddServiceRecordDialog() {
        showServiceRecordDialog(null);
    }

    private void showEditServiceRecordDialog(ServiceRecord existingRecord) {
        showServiceRecordDialog(existingRecord);
    }

    private void showServiceRecordDialog(ServiceRecord existingRecord) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service_record, null);
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        TextInputEditText etIssue = view.findViewById(R.id.etIssue);
        TextInputEditText etServiceCenter = view.findViewById(R.id.etServiceCenter);
        TextInputLayout tilStatus = view.findViewById(R.id.tilStatus);
        com.google.android.material.textfield.MaterialAutoCompleteTextView actvStatus = view.findViewById(R.id.actvStatus);
        TextInputEditText etNotes = view.findViewById(R.id.etNotes);

        String[] statuses = {"Submitted", "Under Inspection", "Repairing", "Completed", "Replaced"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_menu, statuses);
        actvStatus.setAdapter(statusAdapter);

        if (existingRecord != null) {
            if (tvDialogTitle != null) tvDialogTitle.setText("Edit Service Record");
            etIssue.setText(existingRecord.getIssue());
            etServiceCenter.setText(existingRecord.getServiceCenter());
            actvStatus.setText(existingRecord.getStatus(), false);
            etNotes.setText(existingRecord.getNotes());
        } else {
            if (tvDialogTitle != null) tvDialogTitle.setText("Add Service Record");
            actvStatus.setText(statuses[0], false);
        }

        // Dismiss keyboard when status dropdown is tapped so keyboard doesn't hide bottom options
        tilStatus.setEndIconOnClickListener(v -> {
            hideKeyboard(view);
            actvStatus.showDropDown();
        });
        actvStatus.setOnClickListener(v -> {
            hideKeyboard(view);
            actvStatus.showDropDown();
        });
        actvStatus.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                hideKeyboard(view);
                actvStatus.showDropDown();
            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton(existingRecord != null ? "Update" : "Save Record", (dialog, which) -> {
                    String issue = etIssue.getText() != null ? etIssue.getText().toString().trim() : "";
                    String center = etServiceCenter.getText() != null ? etServiceCenter.getText().toString().trim() : "";
                    String status = actvStatus.getText().toString().trim();
                    String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

                    if (issue.isEmpty()) issue = "General Service";
                    if (status.isEmpty()) status = "Submitted";

                    if (existingRecord != null) {
                        existingRecord.setIssue(issue);
                        existingRecord.setServiceCenter(center);
                        existingRecord.setStatus(status);
                        existingRecord.setNotes(notes);
                        serviceRecordDAO.updateServiceRecord(existingRecord);
                        Toast.makeText(this, "Service record updated", Toast.LENGTH_SHORT).show();
                    } else {
                        ServiceRecord record = new ServiceRecord(
                                productId, DateUtils.getTodayDisplay(), issue, center, status, notes, DateUtils.getTodayDisplay()
                        );
                        serviceRecordDAO.insertServiceRecord(record);
                        Toast.makeText(this, "Service record added", Toast.LENGTH_SHORT).show();
                    }
                    loadProductDetails();
                })
                .setNegativeButton("Cancel", null);

        if (existingRecord != null) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                serviceRecordDAO.deleteServiceRecord(existingRecord.getId());
                Toast.makeText(this, "Service record deleted", Toast.LENGTH_SHORT).show();
                loadProductDetails();
            });
        }

        builder.show();
    }
}
