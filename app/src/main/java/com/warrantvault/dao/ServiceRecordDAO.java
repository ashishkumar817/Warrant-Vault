package com.warrantvault.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.warrantvault.database.DatabaseHelper;
import com.warrantvault.models.ServiceRecord;

import java.util.ArrayList;
import java.util.List;

public class ServiceRecordDAO {
    private final DatabaseHelper dbHelper;

    public ServiceRecordDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertServiceRecord(ServiceRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SR_PRODUCT_ID, record.getProductId());
        values.put(DatabaseHelper.COL_SR_DATE, record.getServiceDate());
        values.put(DatabaseHelper.COL_SR_ISSUE, record.getIssue());
        values.put(DatabaseHelper.COL_SR_CENTER, record.getServiceCenter());
        values.put(DatabaseHelper.COL_SR_STATUS, record.getStatus());
        values.put(DatabaseHelper.COL_SR_NOTES, record.getNotes());
        values.put(DatabaseHelper.COL_SR_CREATED_AT, record.getCreatedAt());

        return db.insert(DatabaseHelper.TABLE_SERVICE_RECORDS, null, values);
    }

    public List<ServiceRecord> getRecordsByProductId(long productId) {
        List<ServiceRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_SERVICE_RECORDS,
                null,
                DatabaseHelper.COL_SR_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)},
                null,
                null,
                DatabaseHelper.COL_SR_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                ServiceRecord r = new ServiceRecord();
                r.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SR_ID)));
                r.setProductId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SR_PRODUCT_ID)));
                r.setServiceDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SR_DATE)));
                r.setIssue(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SR_ISSUE)));
                r.setServiceCenter(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SR_CENTER)));
                r.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SR_STATUS)));
                r.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SR_NOTES)));
                r.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SR_CREATED_AT)));
                list.add(r);
            }
            cursor.close();
        }
        return list;
    }

    public boolean updateServiceRecord(ServiceRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SR_DATE, record.getServiceDate());
        values.put(DatabaseHelper.COL_SR_ISSUE, record.getIssue());
        values.put(DatabaseHelper.COL_SR_CENTER, record.getServiceCenter());
        values.put(DatabaseHelper.COL_SR_STATUS, record.getStatus());
        values.put(DatabaseHelper.COL_SR_NOTES, record.getNotes());

        int rows = db.update(
                DatabaseHelper.TABLE_SERVICE_RECORDS,
                values,
                DatabaseHelper.COL_SR_ID + " = ?",
                new String[]{String.valueOf(record.getId())}
        );
        return rows > 0;
    }

    public boolean deleteServiceRecord(long recordId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_SERVICE_RECORDS,
                DatabaseHelper.COL_SR_ID + " = ?",
                new String[]{String.valueOf(recordId)}
        );
        return rows > 0;
    }
}
