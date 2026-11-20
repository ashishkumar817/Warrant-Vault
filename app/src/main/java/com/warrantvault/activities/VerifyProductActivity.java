package com.warrantvault.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import com.warrantvault.R;
import com.warrantvault.dao.DocumentDAO;
import com.warrantvault.dao.NotificationDAO;
import com.warrantvault.dao.ProductDAO;
import com.warrantvault.databinding.ActivityVerifyProductBinding;
import com.warrantvault.models.Document;
import com.warrantvault.models.NotificationItem;
import com.warrantvault.models.Product;
import com.warrantvault.models.Warranty;
import com.warrantvault.utils.DateUtils;
import com.warrantvault.utils.NotificationUtils;
import com.warrantvault.utils.SessionManager;
import com.warrantvault.utils.WarrantyUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class VerifyProductActivity extends AppCompatActivity {

    private ActivityVerifyProductBinding binding;
    private ProductDAO productDAO;
    private DocumentDAO documentDAO;
    private NotificationDAO notificationDAO;
    private SessionManager sessionManager;

    private String billImagePath;
    private long editProductId = -1;
    private String currentPeriod = "12 Months";

    private final String[] CATEGORIES = {
            "Electronics", "Mobiles", "Laptops", "Appliances", "Audio", "Gaming", "Vehicles", "Furniture", "Other"
    };

    private final String[] PERIODS = {
            "No Warranty", "6 Months", "12 Months", "18 Months", "2 Years", "3 Years", "5 Years", "Custom"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productDAO = new ProductDAO(this);
        documentDAO = new DocumentDAO(this);
        notificationDAO = new NotificationDAO(this);
        sessionManager = new SessionManager(this);

        setupToolbar();
        setupDropdowns();
        setupDatePickers();
        populateInitialData();
        setupSaveButton();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = getCurrentFocus();
        if (imm != null && focus != null) {
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }

    private void setupDropdowns() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_menu, CATEGORIES);
        binding.actvCategory.setAdapter(catAdapter);
        binding.actvCategory.setText(CATEGORIES[0], false);

        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_menu, PERIODS);
        binding.actvWarrantyPeriod.setAdapter(periodAdapter);
        binding.actvWarrantyPeriod.setText("12 Months", false);

        // Dismiss keyboard before showing dropdown menu
        binding.actvCategory.setOnClickListener(v -> hideKeyboard());
        binding.actvCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) hideKeyboard();
        });

        binding.actvWarrantyPeriod.setOnClickListener(v -> hideKeyboard());
        binding.actvWarrantyPeriod.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) hideKeyboard();
        });

        binding.actvWarrantyPeriod.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            if ("No Warranty".equals(selected)) {
                currentPeriod = "No Warranty";
                handleNoWarrantySelected();
            } else if ("Custom".equals(selected)) {
                handleCustomWarrantySelected();
            } else {
                currentPeriod = selected;
                binding.etWarrantyEndDate.setFocusable(false);
                binding.etWarrantyEndDate.setClickable(true);
                calculateExpiry();
            }
        });
    }

    private void handleNoWarrantySelected() {
        binding.etWarrantyEndDate.setText("N/A");
        binding.etWarrantyEndDate.setFocusable(false);
        binding.etWarrantyEndDate.setClickable(false);
        binding.tvCalculatedSummary.setText("No Warranty Coverage");
    }

    private void handleCustomWarrantySelected() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom_warranty, null);
        TextInputEditText etDuration = view.findViewById(R.id.etDuration);
        AutoCompleteTextView actvUnit = view.findViewById(R.id.actvUnit);

        String[] units = {"Months", "Years"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_menu, units);
        actvUnit.setAdapter(unitAdapter);
        actvUnit.setText(units[0], false);

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Calculate", (dialog, which) -> {
                    String durationStr = etDuration.getText() != null ? etDuration.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(durationStr) || "0".equals(durationStr)) {
                        Toast.makeText(this, "Please enter a valid warranty duration", Toast.LENGTH_SHORT).show();
                        binding.actvWarrantyPeriod.setText(currentPeriod, false);
                        return;
                    }

                    int amount = 1;
                    try {
                        amount = Integer.parseInt(durationStr);
                    } catch (NumberFormatException ignored) {
                    }

                    String unit = actvUnit.getText().toString().trim();
                    if (TextUtils.isEmpty(unit)) unit = "Months";

                    String customPeriod = amount + " " + unit;
                    currentPeriod = customPeriod;

                    binding.actvWarrantyPeriod.setText(customPeriod, false);
                    binding.etWarrantyEndDate.setFocusable(false);
                    binding.etWarrantyEndDate.setClickable(true);

                    String pDate = binding.etPurchaseDate.getText().toString().trim();
                    if (pDate.isEmpty()) {
                        pDate = DateUtils.getTodayDisplay();
                        binding.etPurchaseDate.setText(pDate);
                    }

                    String endDate = DateUtils.calculateEndDate(pDate, amount, unit);
                    binding.etWarrantyEndDate.setText(endDate);
                    updateSummaryBanner(endDate);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    binding.actvWarrantyPeriod.setText(currentPeriod, false);
                })
                .setOnCancelListener(dialog -> {
                    binding.actvWarrantyPeriod.setText(currentPeriod, false);
                })
                .show();
    }

    private void setupDatePickers() {
        binding.etPurchaseDate.setOnClickListener(v -> showDatePicker(true));
        binding.tilPurchaseDate.setEndIconOnClickListener(v -> showDatePicker(true));

        binding.etWarrantyEndDate.setOnClickListener(v -> showDatePicker(false));
        binding.tilWarrantyEndDate.setEndIconOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isPurchaseDate) {
        Calendar cal = Calendar.getInstance();
        String currentStr = isPurchaseDate ? binding.etPurchaseDate.getText().toString() : binding.etWarrantyEndDate.getText().toString();
        if (!TextUtils.isEmpty(currentStr)) {
            cal.setTime(DateUtils.parseDate(currentStr));
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    String dateStr = DateUtils.formatDate(selected.getTime());
                    if (isPurchaseDate) {
                        binding.etPurchaseDate.setText(dateStr);
                        calculateExpiry();
                    } else {
                        binding.etWarrantyEndDate.setText(dateStr);
                        updateSummaryBanner(dateStr);
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void calculateExpiry() {
        String period = binding.actvWarrantyPeriod.getText().toString().trim();

        // Skip calculation for special options
        if ("No Warranty".equals(period)) {
            handleNoWarrantySelected();
            return;
        }
        if ("Custom".equals(period)) {
            handleCustomWarrantySelected();
            return;
        }

        String pDate = binding.etPurchaseDate.getText().toString().trim();
        if (pDate.isEmpty()) {
            pDate = DateUtils.getTodayDisplay();
            binding.etPurchaseDate.setText(pDate);
        }

        int amount = 12;
        String unit = "Months";

        if (period.toLowerCase().contains("year")) {
            unit = "Years";
        } else if (period.toLowerCase().contains("day")) {
            unit = "Days";
        } else {
            unit = "Months";
        }

        String digits = period.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            try {
                amount = Integer.parseInt(digits);
            } catch (Exception ignored) {
            }
        }

        String endDate = DateUtils.calculateEndDate(pDate, amount, unit);
        binding.etWarrantyEndDate.setText(endDate);
        updateSummaryBanner(endDate);
    }

    private void updateSummaryBanner(String endDate) {
        String period = binding.actvWarrantyPeriod.getText().toString().trim();
        if ("No Warranty".equals(period)) {
            binding.tvCalculatedSummary.setText("No Warranty Coverage");
        } else {
            binding.tvCalculatedSummary.setText("Coverage until " + endDate + " (" + period + ")");
        }
    }

    private void populateInitialData() {
        Intent intent = getIntent();
        editProductId = intent.getLongExtra("edit_product_id", -1);
        billImagePath = intent.getStringExtra("bill_image_path");

        if (editProductId != -1) {
            // Edit mode
            binding.toolbar.setTitle("Edit Product");
            binding.cardOcrNotice.setVisibility(View.GONE);
            Product p = productDAO.getProductById(editProductId);
            if (p != null) {
                binding.etProductName.setText(p.getName());
                binding.etBrand.setText(p.getBrand());
                binding.actvCategory.setText(p.getCategory(), false);
                binding.etModelNumber.setText(p.getModelNumber());
                binding.etSerialNumber.setText(p.getSerialNumber());
                binding.etPurchaseDate.setText(p.getPurchaseDate());
                binding.etPurchasePrice.setText(p.getPurchasePrice() > 0 ? String.valueOf(p.getPurchasePrice()) : "");
                binding.etSeller.setText(p.getSeller());
                binding.etInvoiceNumber.setText(p.getInvoiceNumber());
                if (p.getWarranty() != null) {
                    currentPeriod = p.getWarranty().getWarrantyPeriod();
                    binding.actvWarrantyPeriod.setText(currentPeriod, false);
                    binding.etWarrantyEndDate.setText(p.getWarranty().getEndDate());
                }
                billImagePath = p.getImagePath();
            }
        } else {
            // New entry / OCR entry
            boolean isOcr = intent.getBooleanExtra("is_ocr_extracted", false);
            if (!isOcr) {
                binding.cardOcrNotice.setVisibility(View.GONE);
            }

            if (intent.hasExtra("product_name")) {
                binding.etProductName.setText(intent.getStringExtra("product_name"));
            }
            if (intent.hasExtra("brand")) {
                binding.etBrand.setText(intent.getStringExtra("brand"));
            }
            if (intent.hasExtra("category")) {
                binding.actvCategory.setText(intent.getStringExtra("category"), false);
            }
            if (intent.hasExtra("model_number")) {
                binding.etModelNumber.setText(intent.getStringExtra("model_number"));
            }
            if (intent.hasExtra("serial_number")) {
                binding.etSerialNumber.setText(intent.getStringExtra("serial_number"));
            }
            if (intent.hasExtra("purchase_date")) {
                binding.etPurchaseDate.setText(intent.getStringExtra("purchase_date"));
            } else {
                binding.etPurchaseDate.setText(DateUtils.getTodayDisplay());
            }
            if (intent.hasExtra("purchase_price")) {
                binding.etPurchasePrice.setText(intent.getStringExtra("purchase_price"));
            }
            if (intent.hasExtra("seller")) {
                binding.etSeller.setText(intent.getStringExtra("seller"));
            }
            if (intent.hasExtra("invoice_number")) {
                binding.etInvoiceNumber.setText(intent.getStringExtra("invoice_number"));
            }
            if (intent.hasExtra("warranty_period")) {
                currentPeriod = intent.getStringExtra("warranty_period");
                binding.actvWarrantyPeriod.setText(currentPeriod, false);
            }

            calculateExpiry();
        }

        // Show thumbnail if bill image exists
        if (billImagePath != null && new File(billImagePath).exists()) {
            binding.cardOcrNotice.setVisibility(View.VISIBLE);
            try {
                Bitmap bmp = BitmapFactory.decodeFile(billImagePath);
                binding.ivBillThumbnail.setImageBitmap(bmp);
            } catch (Exception ignored) {
            }

            binding.cardOcrNotice.setOnClickListener(v -> {
                Intent viewIntent = new Intent(this, DocumentViewerActivity.class);
                viewIntent.putExtra("image_path", billImagePath);
                viewIntent.putExtra("title", "Scanned Bill");
                startActivity(viewIntent);
            });
        }
    }

    private void setupSaveButton() {
        binding.btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = binding.etProductName.getText() != null ? binding.etProductName.getText().toString().trim() : "";
        String brand = binding.etBrand.getText() != null ? binding.etBrand.getText().toString().trim() : "";
        String category = binding.actvCategory.getText().toString().trim();
        String modelNumber = binding.etModelNumber.getText() != null ? binding.etModelNumber.getText().toString().trim() : "";
        String serialNumber = binding.etSerialNumber.getText() != null ? binding.etSerialNumber.getText().toString().trim() : "";
        String purchaseDate = binding.etPurchaseDate.getText() != null ? binding.etPurchaseDate.getText().toString().trim() : "";
        String priceStr = binding.etPurchasePrice.getText() != null ? binding.etPurchasePrice.getText().toString().trim() : "";
        String seller = binding.etSeller.getText() != null ? binding.etSeller.getText().toString().trim() : "";
        String invoiceNumber = binding.etInvoiceNumber.getText() != null ? binding.etInvoiceNumber.getText().toString().trim() : "";
        String warrantyPeriod = binding.actvWarrantyPeriod.getText().toString().trim();
        String warrantyEndDate = binding.etWarrantyEndDate.getText() != null ? binding.etWarrantyEndDate.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            binding.tilProductName.setError("Product name is required");
            return;
        } else {
            binding.tilProductName.setError(null);
        }

        if (TextUtils.isEmpty(purchaseDate)) {
            binding.tilPurchaseDate.setError("Purchase date is required");
            return;
        } else {
            binding.tilPurchaseDate.setError(null);
        }

        // Handle "No Warranty" — no end date needed
        boolean isNoWarranty = "No Warranty".equals(warrantyPeriod);
        if (isNoWarranty) {
            warrantyEndDate = purchaseDate; // Set end = start so DB is consistent
        } else if (TextUtils.isEmpty(warrantyEndDate) || "N/A".equals(warrantyEndDate)) {
            calculateExpiry();
            warrantyEndDate = binding.etWarrantyEndDate.getText().toString().trim();
        }

        double price = 0.0;
        if (!priceStr.isEmpty()) {
            try {
                price = Double.parseDouble(priceStr);
            } catch (Exception ignored) {
            }
        }

        long userId = sessionManager.getUserId();

        if (editProductId != -1) {
            // Update
            Product p = new Product(
                    editProductId, userId, name, brand, category, modelNumber, serialNumber,
                    purchaseDate, price, seller, invoiceNumber, billImagePath, DateUtils.getTodayDb()
            );
            String editStatus = isNoWarranty ? WarrantyUtils.STATUS_NO_WARRANTY : WarrantyUtils.STATUS_ACTIVE;
            Warranty w = new Warranty(
                    editProductId, purchaseDate, warrantyEndDate, warrantyPeriod, brand, editStatus
            );
            boolean updated = productDAO.updateProduct(p, w);
            if (updated) {
                Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Insert New Product
            Product product = new Product(
                    0, userId, name, brand, category, modelNumber, serialNumber,
                    purchaseDate, price, seller, invoiceNumber, billImagePath, DateUtils.getTodayDb()
            );

            String newStatus = isNoWarranty ? WarrantyUtils.STATUS_NO_WARRANTY : WarrantyUtils.STATUS_ACTIVE;
            Warranty warranty = new Warranty(
                    0, purchaseDate, warrantyEndDate, warrantyPeriod, brand, newStatus
            );

            long newId = productDAO.insertProduct(product, warranty);

            if (newId != -1) {
                // Attach bill as Purchase Invoice document if available
                if (billImagePath != null && new File(billImagePath).exists()) {
                    Document doc = new Document(newId, "Purchase Invoice", billImagePath, DateUtils.getTodayDisplay());
                    documentDAO.insertDocument(doc);
                }

                // Add welcoming notification
                notificationDAO.insertNotification(new NotificationItem(
                        newId, "Product Added", name + " was added to your Warrant Vault.", DateUtils.getTodayDisplay(), false
                ));

                // Show Android system push notification if notifications enabled
                if (sessionManager.isNotificationsEnabled()) {
                    String notifyTitle = isNoWarranty ? "Product Protected" : "Warranty Active";
                    String notifyMsg = isNoWarranty
                            ? name + " has been added to your Warrant Vault."
                            : name + " is now covered until " + warrantyEndDate + ". We'll remind you before expiry.";
                    NotificationUtils.showNotification(this, (int) newId, newId, notifyTitle, notifyMsg);
                }

                Toast.makeText(this, "Product saved successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, ProductDetailsActivity.class);
                intent.putExtra("product_id", newId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
