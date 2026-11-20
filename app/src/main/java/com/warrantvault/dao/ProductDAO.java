package com.warrantvault.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.warrantvault.database.DatabaseHelper;
import com.warrantvault.models.Product;
import com.warrantvault.models.Warranty;
import com.warrantvault.utils.WarrantyUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public static class ProductCounts {
        public int total;
        public int active;
        public int expiringSoon;
        public int expired;
    }

    private final DatabaseHelper dbHelper;
    private final WarrantyDAO warrantyDAO;

    public ProductDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.warrantyDAO = new WarrantyDAO(context);
    }

    public long insertProduct(Product product, Warranty warranty) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        long productId = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_PROD_USER_ID, product.getUserId());
            values.put(DatabaseHelper.COL_PROD_NAME, product.getName());
            values.put(DatabaseHelper.COL_PROD_BRAND, product.getBrand());
            values.put(DatabaseHelper.COL_PROD_CATEGORY, product.getCategory());
            values.put(DatabaseHelper.COL_PROD_MODEL_NUMBER, product.getModelNumber());
            values.put(DatabaseHelper.COL_PROD_SERIAL_NUMBER, product.getSerialNumber());
            values.put(DatabaseHelper.COL_PROD_PURCHASE_DATE, product.getPurchaseDate());
            values.put(DatabaseHelper.COL_PROD_PURCHASE_PRICE, product.getPurchasePrice());
            values.put(DatabaseHelper.COL_PROD_SELLER, product.getSeller());
            values.put(DatabaseHelper.COL_PROD_INVOICE_NUMBER, product.getInvoiceNumber());
            values.put(DatabaseHelper.COL_PROD_IMAGE_PATH, product.getImagePath());
            values.put(DatabaseHelper.COL_PROD_CREATED_AT, product.getCreatedAt());

            productId = db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values);

            if (productId != -1 && warranty != null) {
                warranty.setProductId(productId);
                ContentValues wValues = new ContentValues();
                wValues.put(DatabaseHelper.COL_WARR_PRODUCT_ID, productId);
                wValues.put(DatabaseHelper.COL_WARR_START_DATE, warranty.getStartDate());
                wValues.put(DatabaseHelper.COL_WARR_END_DATE, warranty.getEndDate());
                wValues.put(DatabaseHelper.COL_WARR_PERIOD, warranty.getWarrantyPeriod());
                wValues.put(DatabaseHelper.COL_WARR_PROVIDER, warranty.getProvider());
                wValues.put(DatabaseHelper.COL_WARR_STATUS, warranty.getStatus());

                db.insert(DatabaseHelper.TABLE_WARRANTIES, null, wValues);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return productId;
    }

    public Product getProductById(long productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PRODUCTS,
                null,
                DatabaseHelper.COL_PROD_ID + " = ?",
                new String[]{String.valueOf(productId)},
                null,
                null,
                null
        );

        Product product = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                product = mapCursorToProduct(cursor);
                Warranty warranty = warrantyDAO.getWarrantyByProductId(productId);
                product.setWarranty(warranty);
                WarrantyUtils.computeWarrantyMetrics(product);
            }
            cursor.close();
        }
        return product;
    }

    public List<Product> getAllProducts(long userId) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PRODUCTS,
                null,
                DatabaseHelper.COL_PROD_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                DatabaseHelper.COL_PROD_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = mapCursorToProduct(cursor);
                Warranty warranty = warrantyDAO.getWarrantyByProductId(product.getId());
                product.setWarranty(warranty);
                WarrantyUtils.computeWarrantyMetrics(product);
                list.add(product);
            }
            cursor.close();
        }
        return list;
    }

    public List<Product> getProductsByCategory(long userId, String category) {
        if (category == null || category.equalsIgnoreCase("All")) {
            return getAllProducts(userId);
        }
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PRODUCTS,
                null,
                DatabaseHelper.COL_PROD_USER_ID + " = ? AND LOWER(" + DatabaseHelper.COL_PROD_CATEGORY + ") = LOWER(?)",
                new String[]{String.valueOf(userId), category},
                null,
                null,
                DatabaseHelper.COL_PROD_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = mapCursorToProduct(cursor);
                Warranty warranty = warrantyDAO.getWarrantyByProductId(product.getId());
                product.setWarranty(warranty);
                WarrantyUtils.computeWarrantyMetrics(product);
                list.add(product);
            }
            cursor.close();
        }
        return list;
    }

    public List<Product> searchProducts(long userId, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts(userId);
        }
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String pattern = "%" + query.trim().toLowerCase() + "%";

        String selection = DatabaseHelper.COL_PROD_USER_ID + " = ? AND ("
                + "LOWER(" + DatabaseHelper.COL_PROD_NAME + ") LIKE ? OR "
                + "LOWER(" + DatabaseHelper.COL_PROD_BRAND + ") LIKE ? OR "
                + "LOWER(" + DatabaseHelper.COL_PROD_MODEL_NUMBER + ") LIKE ? OR "
                + "LOWER(" + DatabaseHelper.COL_PROD_SERIAL_NUMBER + ") LIKE ? OR "
                + "LOWER(" + DatabaseHelper.COL_PROD_INVOICE_NUMBER + ") LIKE ? OR "
                + "LOWER(" + DatabaseHelper.COL_PROD_SELLER + ") LIKE ? OR "
                + "LOWER(" + DatabaseHelper.COL_PROD_CATEGORY + ") LIKE ?)";

        String[] selectionArgs = {
                String.valueOf(userId), pattern, pattern, pattern, pattern, pattern, pattern, pattern
        };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PRODUCTS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COL_PROD_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = mapCursorToProduct(cursor);
                Warranty warranty = warrantyDAO.getWarrantyByProductId(product.getId());
                product.setWarranty(warranty);
                WarrantyUtils.computeWarrantyMetrics(product);
                list.add(product);
            }
            cursor.close();
        }
        return list;
    }

    public List<Product> getExpiringSoonProducts(long userId, int withinDays) {
        List<Product> all = getAllProducts(userId);
        List<Product> filtered = new ArrayList<>();
        for (Product p : all) {
            if (WarrantyUtils.STATUS_EXPIRING.equals(p.getStatus())) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public List<Product> getRecentProducts(long userId, int limit) {
        List<Product> all = getAllProducts(userId);
        if (all.size() <= limit) {
            return all;
        }
        return all.subList(0, limit);
    }

    public ProductCounts getProductCounts(long userId) {
        List<Product> all = getAllProducts(userId);
        ProductCounts counts = new ProductCounts();
        counts.total = all.size();
        for (Product p : all) {
            if (WarrantyUtils.STATUS_ACTIVE.equals(p.getStatus())) {
                counts.active++;
            } else if (WarrantyUtils.STATUS_EXPIRING.equals(p.getStatus())) {
                counts.expiringSoon++;
            } else if (WarrantyUtils.STATUS_EXPIRED.equals(p.getStatus())) {
                counts.expired++;
            }
        }
        return counts;
    }

    public boolean updateProduct(Product product, Warranty warranty) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_PROD_NAME, product.getName());
            values.put(DatabaseHelper.COL_PROD_BRAND, product.getBrand());
            values.put(DatabaseHelper.COL_PROD_CATEGORY, product.getCategory());
            values.put(DatabaseHelper.COL_PROD_MODEL_NUMBER, product.getModelNumber());
            values.put(DatabaseHelper.COL_PROD_SERIAL_NUMBER, product.getSerialNumber());
            values.put(DatabaseHelper.COL_PROD_PURCHASE_DATE, product.getPurchaseDate());
            values.put(DatabaseHelper.COL_PROD_PURCHASE_PRICE, product.getPurchasePrice());
            values.put(DatabaseHelper.COL_PROD_SELLER, product.getSeller());
            values.put(DatabaseHelper.COL_PROD_INVOICE_NUMBER, product.getInvoiceNumber());
            if (product.getImagePath() != null) {
                values.put(DatabaseHelper.COL_PROD_IMAGE_PATH, product.getImagePath());
            }

            int rows = db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.COL_PROD_ID + " = ?",
                    new String[]{String.valueOf(product.getId())}
            );

            if (warranty != null) {
                warranty.setProductId(product.getId());
                warrantyDAO.updateWarranty(warranty);
            }

            db.setTransactionSuccessful();
            success = (rows > 0);
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public boolean deleteProduct(long productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        try {
            db.delete(DatabaseHelper.TABLE_WARRANTIES, DatabaseHelper.COL_WARR_PRODUCT_ID + " = ?", new String[]{String.valueOf(productId)});
            db.delete(DatabaseHelper.TABLE_DOCUMENTS, DatabaseHelper.COL_DOC_PRODUCT_ID + " = ?", new String[]{String.valueOf(productId)});
            db.delete(DatabaseHelper.TABLE_SERVICE_RECORDS, DatabaseHelper.COL_SR_PRODUCT_ID + " = ?", new String[]{String.valueOf(productId)});
            db.delete(DatabaseHelper.TABLE_NOTIFICATIONS, DatabaseHelper.COL_NOTIF_PRODUCT_ID + " = ?", new String[]{String.valueOf(productId)});
            int rows = db.delete(
                    DatabaseHelper.TABLE_PRODUCTS,
                    DatabaseHelper.COL_PROD_ID + " = ?",
                    new String[]{String.valueOf(productId)}
            );
            db.setTransactionSuccessful();
            success = rows > 0;
        } finally {
            db.endTransaction();
        }
        return success;
    }

    private Product mapCursorToProduct(Cursor cursor) {
        Product p = new Product();
        p.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_ID)));
        p.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_USER_ID)));
        p.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_NAME)));
        p.setBrand(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_BRAND)));
        p.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_CATEGORY)));
        p.setModelNumber(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_MODEL_NUMBER)));
        p.setSerialNumber(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_SERIAL_NUMBER)));
        p.setPurchaseDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_PURCHASE_DATE)));
        p.setPurchasePrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_PURCHASE_PRICE)));
        p.setSeller(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_SELLER)));
        p.setInvoiceNumber(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_INVOICE_NUMBER)));
        p.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_IMAGE_PATH)));
        p.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_CREATED_AT)));
        return p;
    }
}
