package com.warrantvault.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.warrantvault.database.DatabaseHelper;
import com.warrantvault.models.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {
    private final DatabaseHelper dbHelper;

    public DocumentDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertDocument(Document doc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_DOC_PRODUCT_ID, doc.getProductId());
        values.put(DatabaseHelper.COL_DOC_TYPE, doc.getDocumentType());
        values.put(DatabaseHelper.COL_DOC_FILE_PATH, doc.getFilePath());
        values.put(DatabaseHelper.COL_DOC_CREATED_AT, doc.getCreatedAt());

        return db.insert(DatabaseHelper.TABLE_DOCUMENTS, null, values);
    }

    public List<Document> getDocumentsByProductId(long productId) {
        List<Document> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_DOCUMENTS,
                null,
                DatabaseHelper.COL_DOC_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)},
                null,
                null,
                DatabaseHelper.COL_DOC_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Document doc = new Document();
                doc.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_ID)));
                doc.setProductId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_PRODUCT_ID)));
                doc.setDocumentType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_TYPE)));
                doc.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_FILE_PATH)));
                doc.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_CREATED_AT)));

                if (doc.getFilePath() != null) {
                    File file = new File(doc.getFilePath());
                    doc.setFileName(file.getName());
                    long bytes = file.length();
                    if (bytes > 1024 * 1024) {
                        doc.setFileSize(String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0)));
                    } else if (bytes > 0) {
                        doc.setFileSize((bytes / 1024) + " KB");
                    } else {
                        doc.setFileSize("Document");
                    }
                }
                list.add(doc);
            }
            cursor.close();
        }
        return list;
    }

    public Document getDocumentById(long docId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_DOCUMENTS,
                null,
                DatabaseHelper.COL_DOC_ID + " = ?",
                new String[]{String.valueOf(docId)},
                null,
                null,
                null
        );

        Document doc = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                doc = new Document();
                doc.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_ID)));
                doc.setProductId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_PRODUCT_ID)));
                doc.setDocumentType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_TYPE)));
                doc.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_FILE_PATH)));
                doc.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_CREATED_AT)));
            }
            cursor.close();
        }
        return doc;
    }

    public boolean deleteDocument(long docId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_DOCUMENTS,
                DatabaseHelper.COL_DOC_ID + " = ?",
                new String[]{String.valueOf(docId)}
        );
        return rows > 0;
    }
}
