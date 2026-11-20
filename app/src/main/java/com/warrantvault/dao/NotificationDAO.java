package com.warrantvault.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.warrantvault.database.DatabaseHelper;
import com.warrantvault.models.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private final DatabaseHelper dbHelper;

    public NotificationDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertNotification(NotificationItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NOTIF_PRODUCT_ID, item.getProductId());
        values.put(DatabaseHelper.COL_NOTIF_TITLE, item.getTitle());
        values.put(DatabaseHelper.COL_NOTIF_MESSAGE, item.getMessage());
        values.put(DatabaseHelper.COL_NOTIF_DATE, item.getNotificationDate());
        values.put(DatabaseHelper.COL_NOTIF_IS_READ, item.isRead() ? 1 : 0);

        return db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, values);
    }

    public List<NotificationItem> getAllNotifications(long userId) {
        List<NotificationItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT n.*, p." + DatabaseHelper.COL_PROD_NAME + " as product_name "
                + "FROM " + DatabaseHelper.TABLE_NOTIFICATIONS + " n "
                + "LEFT JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON n." + DatabaseHelper.COL_NOTIF_PRODUCT_ID + " = p." + DatabaseHelper.COL_PROD_ID + " "
                + "WHERE p." + DatabaseHelper.COL_PROD_USER_ID + " = ? OR n." + DatabaseHelper.COL_NOTIF_PRODUCT_ID + " IS NULL "
                + "ORDER BY n." + DatabaseHelper.COL_NOTIF_ID + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                NotificationItem item = mapCursor(cursor);
                list.add(item);
            }
            cursor.close();
        }
        return list;
    }

    public int getUnreadCount(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NOTIFICATIONS + " n "
                + "LEFT JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON n." + DatabaseHelper.COL_NOTIF_PRODUCT_ID + " = p." + DatabaseHelper.COL_PROD_ID + " "
                + "WHERE (p." + DatabaseHelper.COL_PROD_USER_ID + " = ? OR n." + DatabaseHelper.COL_NOTIF_PRODUCT_ID + " IS NULL) AND n." + DatabaseHelper.COL_NOTIF_IS_READ + " = 0";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public void markAsRead(long notifId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NOTIF_IS_READ, 1);
        db.update(DatabaseHelper.TABLE_NOTIFICATIONS, values, DatabaseHelper.COL_NOTIF_ID + " = ?", new String[]{String.valueOf(notifId)});
    }

    public void markAllAsRead(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NOTIF_IS_READ, 1);
        db.update(DatabaseHelper.TABLE_NOTIFICATIONS, values, null, null);
    }

    private NotificationItem mapCursor(Cursor cursor) {
        NotificationItem item = new NotificationItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_ID)));
        int prodIdx = cursor.getColumnIndex(DatabaseHelper.COL_NOTIF_PRODUCT_ID);
        if (prodIdx != -1 && !cursor.isNull(prodIdx)) {
            item.setProductId(cursor.getLong(prodIdx));
        }
        item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_TITLE)));
        item.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_MESSAGE)));
        item.setNotificationDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_DATE)));
        item.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_IS_READ)) == 1);

        int nameIdx = cursor.getColumnIndex("product_name");
        if (nameIdx != -1 && !cursor.isNull(nameIdx)) {
            item.setProductName(cursor.getString(nameIdx));
        }
        return item;
    }
}
