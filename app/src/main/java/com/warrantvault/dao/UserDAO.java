package com.warrantvault.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.warrantvault.database.DatabaseHelper;
import com.warrantvault.models.User;
import com.warrantvault.utils.DateUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDAO {
    private final DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context.getApplicationContext());
    }

    public static String hashPassword(String password) {
        if (password == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toString(password.hashCode());
        }
    }

    public long registerUser(User user, String plainPassword) {
        if (user == null || user.getEmail() == null || plainPassword == null) {
            return -1;
        }
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_USER_NAME, user.getFullName() != null ? user.getFullName().trim() : "User");
            values.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail().trim().toLowerCase());
            values.put(DatabaseHelper.COL_USER_PASSWORD_HASH, hashPassword(plainPassword));
            values.put(DatabaseHelper.COL_USER_CREATED_AT, user.getCreatedAt() != null ? user.getCreatedAt() : DateUtils.getTodayDb());

            return db.insert(DatabaseHelper.TABLE_USERS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public User loginUser(String email, String plainPassword) {
        if (email == null || plainPassword == null) {
            return null;
        }
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String hash = hashPassword(plainPassword);
            String selection = DatabaseHelper.COL_USER_EMAIL + " = ? AND " + DatabaseHelper.COL_USER_PASSWORD_HASH + " = ?";
            String[] selectionArgs = {email.trim().toLowerCase(), hash};

            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            User user = null;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    user = mapCursorToUser(cursor);
                }
                cursor.close();
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isEmailRegistered(String email) {
        if (email == null) return false;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COL_USER_ID},
                    DatabaseHelper.COL_USER_EMAIL + " = ?",
                    new String[]{email.trim().toLowerCase()},
                    null,
                    null,
                    null
            );
            boolean exists = (cursor != null && cursor.getCount() > 0);
            if (cursor != null) cursor.close();
            return exists;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUserByEmail(String email) {
        if (email == null) return null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    DatabaseHelper.COL_USER_EMAIL + " = ?",
                    new String[]{email.trim().toLowerCase()},
                    null,
                    null,
                    null
            );
            User user = null;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    user = mapCursorToUser(cursor);
                }
                cursor.close();
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getUserById(long id) {
        if (id <= 0) return null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    DatabaseHelper.COL_USER_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );
            User user = null;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    user = mapCursorToUser(cursor);
                }
                cursor.close();
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        if (email == null || newPassword == null || newPassword.length() < 6) {
            return false;
        }
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_USER_PASSWORD_HASH, hashPassword(newPassword));
            int rows = db.update(DatabaseHelper.TABLE_USERS, values,
                    DatabaseHelper.COL_USER_EMAIL + " = ?",
                    new String[]{email.trim().toLowerCase()});
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapCursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD_HASH)));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_CREATED_AT)));
        return user;
    }
}
