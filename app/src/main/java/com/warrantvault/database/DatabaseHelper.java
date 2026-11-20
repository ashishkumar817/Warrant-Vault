package com.warrantvault.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "warrant_vault.db";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_WARRANTIES = "warranties";
    public static final String TABLE_DOCUMENTS = "documents";
    public static final String TABLE_SERVICE_RECORDS = "service_records";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    // Column names - Users
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "full_name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD_HASH = "password_hash";
    public static final String COL_USER_CREATED_AT = "created_at";

    // Column names - Products
    public static final String COL_PROD_ID = "id";
    public static final String COL_PROD_USER_ID = "user_id";
    public static final String COL_PROD_NAME = "name";
    public static final String COL_PROD_BRAND = "brand";
    public static final String COL_PROD_CATEGORY = "category";
    public static final String COL_PROD_MODEL_NUMBER = "model_number";
    public static final String COL_PROD_SERIAL_NUMBER = "serial_number";
    public static final String COL_PROD_PURCHASE_DATE = "purchase_date";
    public static final String COL_PROD_PURCHASE_PRICE = "purchase_price";
    public static final String COL_PROD_SELLER = "seller";
    public static final String COL_PROD_INVOICE_NUMBER = "invoice_number";
    public static final String COL_PROD_IMAGE_PATH = "image_path";
    public static final String COL_PROD_CREATED_AT = "created_at";

    // Column names - Warranties
    public static final String COL_WARR_ID = "id";
    public static final String COL_WARR_PRODUCT_ID = "product_id";
    public static final String COL_WARR_START_DATE = "start_date";
    public static final String COL_WARR_END_DATE = "end_date";
    public static final String COL_WARR_PERIOD = "warranty_period";
    public static final String COL_WARR_PROVIDER = "provider";
    public static final String COL_WARR_STATUS = "status";

    // Column names - Documents
    public static final String COL_DOC_ID = "id";
    public static final String COL_DOC_PRODUCT_ID = "product_id";
    public static final String COL_DOC_TYPE = "document_type";
    public static final String COL_DOC_FILE_PATH = "file_path";
    public static final String COL_DOC_CREATED_AT = "created_at";

    // Column names - Service Records
    public static final String COL_SR_ID = "id";
    public static final String COL_SR_PRODUCT_ID = "product_id";
    public static final String COL_SR_DATE = "service_date";
    public static final String COL_SR_ISSUE = "issue";
    public static final String COL_SR_CENTER = "service_center";
    public static final String COL_SR_STATUS = "status";
    public static final String COL_SR_NOTES = "notes";
    public static final String COL_SR_CREATED_AT = "created_at";

    // Column names - Notifications
    public static final String COL_NOTIF_ID = "id";
    public static final String COL_NOTIF_PRODUCT_ID = "product_id";
    public static final String COL_NOTIF_TITLE = "title";
    public static final String COL_NOTIF_MESSAGE = "message";
    public static final String COL_NOTIF_DATE = "notification_date";
    public static final String COL_NOTIF_IS_READ = "is_read";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_NAME + " TEXT NOT NULL, "
                + COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COL_USER_PASSWORD_HASH + " TEXT NOT NULL, "
                + COL_USER_CREATED_AT + " TEXT NOT NULL);";

        // Products Table
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + " ("
                + COL_PROD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PROD_USER_ID + " INTEGER NOT NULL, "
                + COL_PROD_NAME + " TEXT NOT NULL, "
                + COL_PROD_BRAND + " TEXT, "
                + COL_PROD_CATEGORY + " TEXT, "
                + COL_PROD_MODEL_NUMBER + " TEXT, "
                + COL_PROD_SERIAL_NUMBER + " TEXT, "
                + COL_PROD_PURCHASE_DATE + " TEXT NOT NULL, "
                + COL_PROD_PURCHASE_PRICE + " REAL DEFAULT 0, "
                + COL_PROD_SELLER + " TEXT, "
                + COL_PROD_INVOICE_NUMBER + " TEXT, "
                + COL_PROD_IMAGE_PATH + " TEXT, "
                + COL_PROD_CREATED_AT + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_PROD_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID
                + ") ON DELETE CASCADE);";

        // Warranties Table
        String createWarrantiesTable = "CREATE TABLE " + TABLE_WARRANTIES + " ("
                + COL_WARR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_WARR_PRODUCT_ID + " INTEGER NOT NULL, "
                + COL_WARR_START_DATE + " TEXT NOT NULL, "
                + COL_WARR_END_DATE + " TEXT NOT NULL, "
                + COL_WARR_PERIOD + " TEXT, "
                + COL_WARR_PROVIDER + " TEXT, "
                + COL_WARR_STATUS + " TEXT, "
                + "FOREIGN KEY (" + COL_WARR_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COL_PROD_ID
                + ") ON DELETE CASCADE);";

        // Documents Table
        String createDocumentsTable = "CREATE TABLE " + TABLE_DOCUMENTS + " ("
                + COL_DOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DOC_PRODUCT_ID + " INTEGER NOT NULL, "
                + COL_DOC_TYPE + " TEXT NOT NULL, "
                + COL_DOC_FILE_PATH + " TEXT NOT NULL, "
                + COL_DOC_CREATED_AT + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_DOC_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COL_PROD_ID
                + ") ON DELETE CASCADE);";

        // Service Records Table
        String createServiceRecordsTable = "CREATE TABLE " + TABLE_SERVICE_RECORDS + " ("
                + COL_SR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SR_PRODUCT_ID + " INTEGER NOT NULL, "
                + COL_SR_DATE + " TEXT NOT NULL, "
                + COL_SR_ISSUE + " TEXT, "
                + COL_SR_CENTER + " TEXT, "
                + COL_SR_STATUS + " TEXT, "
                + COL_SR_NOTES + " TEXT, "
                + COL_SR_CREATED_AT + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_SR_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COL_PROD_ID
                + ") ON DELETE CASCADE);";

        // Notifications Table
        String createNotificationsTable = "CREATE TABLE " + TABLE_NOTIFICATIONS + " ("
                + COL_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NOTIF_PRODUCT_ID + " INTEGER, "
                + COL_NOTIF_TITLE + " TEXT NOT NULL, "
                + COL_NOTIF_MESSAGE + " TEXT NOT NULL, "
                + COL_NOTIF_DATE + " TEXT NOT NULL, "
                + COL_NOTIF_IS_READ + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY (" + COL_NOTIF_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COL_PROD_ID
                + ") ON DELETE CASCADE);";

        db.execSQL(createUsersTable);
        db.execSQL(createProductsTable);
        db.execSQL(createWarrantiesTable);
        db.execSQL(createDocumentsTable);
        db.execSQL(createServiceRecordsTable);
        db.execSQL(createNotificationsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WARRANTIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
