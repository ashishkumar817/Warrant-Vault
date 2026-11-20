package com.warrantvault.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.warrantvault.database.DatabaseHelper;
import com.warrantvault.models.Warranty;

public class WarrantyDAO {
    private final DatabaseHelper dbHelper;

    public WarrantyDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertWarranty(Warranty warranty) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WARR_PRODUCT_ID, warranty.getProductId());
        values.put(DatabaseHelper.COL_WARR_START_DATE, warranty.getStartDate());
        values.put(DatabaseHelper.COL_WARR_END_DATE, warranty.getEndDate());
        values.put(DatabaseHelper.COL_WARR_PERIOD, warranty.getWarrantyPeriod());
        values.put(DatabaseHelper.COL_WARR_PROVIDER, warranty.getProvider());
        values.put(DatabaseHelper.COL_WARR_STATUS, warranty.getStatus());

        return db.insert(DatabaseHelper.TABLE_WARRANTIES, null, values);
    }

    public Warranty getWarrantyByProductId(long productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_WARRANTIES,
                null,
                DatabaseHelper.COL_WARR_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)},
                null,
                null,
                null
        );

        Warranty warranty = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                warranty = mapCursorToWarranty(cursor);
            }
            cursor.close();
        }
        return warranty;
    }

    public int updateWarranty(Warranty warranty) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WARR_START_DATE, warranty.getStartDate());
        values.put(DatabaseHelper.COL_WARR_END_DATE, warranty.getEndDate());
        values.put(DatabaseHelper.COL_WARR_PERIOD, warranty.getWarrantyPeriod());
        values.put(DatabaseHelper.COL_WARR_PROVIDER, warranty.getProvider());
        values.put(DatabaseHelper.COL_WARR_STATUS, warranty.getStatus());

        return db.update(
                DatabaseHelper.TABLE_WARRANTIES,
                values,
                DatabaseHelper.COL_WARR_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(warranty.getProductId())}
        );
    }

    private Warranty mapCursorToWarranty(Cursor cursor) {
        Warranty warranty = new Warranty();
        warranty.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WARR_ID)));
        warranty.setProductId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WARR_PRODUCT_ID)));
        warranty.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WARR_START_DATE)));
        warranty.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WARR_END_DATE)));
        warranty.setWarrantyPeriod(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WARR_PERIOD)));
        warranty.setProvider(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WARR_PROVIDER)));
        warranty.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WARR_STATUS)));
        return warranty;
    }
}
