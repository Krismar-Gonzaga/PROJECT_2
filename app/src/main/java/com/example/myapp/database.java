package com.example.myapp;

import static java.security.AccessController.getContext;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class database extends SQLiteOpenHelper {
    // Database and table names
    public static final String DATABASE_NAME = "BODEGA";
    public static final String TABLE_USER = "User";
    public static final String TABLE_PRODUCT = "product";
    public static final String TABLE_CHECKOUT = "checkout";
    public static final String TABLE_SOLD = "sold";

    // User table columns
    public static final String COL_USER_ID = "user_id";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_TYPE = "type";
    public static final String COL_USERNAME = "username";
    public static final String COL_STORE_NAME = "store_name";
    public static final String COL_STARTED = "started";

    // Product table columns
    public static final String COL_PRODUCT_ID = "product_id";
    public static final String COL_PRODUCT_NAME = "name";
    public static final String COL_PRICE = "price";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_OVerQUANTITY = "total_quantity";

    // Checkout table columns
    public static final String COL_CHECKOUT_ID = "checkout_id";
    public static final String COL_TOTAL_PRICE = "total_price";
    private static final String COL_PRODUCT_IMAGE = "product_image";

    public database(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }



    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create User table
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COL_PASSWORD + " TEXT NOT NULL,"
                + COL_TYPE + " TEXT NOT NULL, "
                + COL_USERNAME + " TEXT NOT NULL,"
                + COL_STORE_NAME + " TEXT NOT NULL,"
                + COL_STARTED + " TEXT DEFAULT (datetime('now','localtime'))"
                + ")";
        db.execSQL(CREATE_USER_TABLE);

        // Create Product table with foreign key to User
        String CREATE_PRODUCT_TABLE = "CREATE TABLE " + TABLE_PRODUCT + "("
                + COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USER_ID + " INTEGER NOT NULL,"
                + COL_PRODUCT_NAME + " TEXT NOT NULL,"
                + COL_PRICE + " FLOAT NOT NULL,"
                + COL_QUANTITY + " INTEGER NOT NULL,"
                + COL_OVerQUANTITY + " INTEGER NOT NULL,"
                + COL_PRODUCT_IMAGE + " BLOB,"
                + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES "
                + TABLE_USER + "(" + COL_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_PRODUCT_TABLE);

        // Create Checkout table with foreign key to Product
        String CREATE_CHECKOUT_TABLE = "CREATE TABLE " + TABLE_CHECKOUT + "("
                + COL_CHECKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_PRODUCT_ID + " INTEGER NOT NULL,"
                + COL_PRODUCT_NAME + " TEXT NOT NULL,"
                + COL_PRICE + " FLOAT NOT NULL,"
                + COL_QUANTITY + " INTEGER NOT NULL,"
                + COL_TOTAL_PRICE + " FLOAT NOT NULL,"
                + COL_PRODUCT_IMAGE + " BLOB,"
                + "FOREIGN KEY(" + COL_PRODUCT_ID + ") REFERENCES "
                + TABLE_PRODUCT + "(" + COL_PRODUCT_ID + ")"
                + ")";
        db.execSQL(CREATE_CHECKOUT_TABLE);

        String CREATE_SOLD_TABLE = "CREATE TABLE " + TABLE_SOLD + "("
                + COL_CHECKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_PRODUCT_ID + " INTEGER NOT NULL,"
                + COL_PRODUCT_NAME + " TEXT NOT NULL,"
                + COL_PRICE + " FLOAT NOT NULL,"
                + COL_QUANTITY + " INTEGER NOT NULL,"
                + COL_TOTAL_PRICE + " FLOAT NOT NULL,"
                + COL_PRODUCT_IMAGE + " BLOB,"
                + "FOREIGN KEY(" + COL_PRODUCT_ID + ") REFERENCES "
                + TABLE_PRODUCT + "(" + COL_PRODUCT_ID + ")"
                + ")";
        db.execSQL(CREATE_SOLD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKOUT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOLD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // User methods
    public boolean insertdata(User account) {
        SQLiteDatabase db = this.getWritableDatabase();
        try (Cursor cursor = db.query(TABLE_USER,
                new String[]{COL_EMAIL},
                COL_EMAIL + "=?",
                new String[]{account.getEmailAddress()},
                null, null, null)) {

            if (cursor.getCount() > 0) return false;

            ContentValues values = new ContentValues();
            values.put(COL_EMAIL, account.getEmailAddress());
            values.put(COL_PASSWORD, account.getPassword());
            values.put(COL_TYPE, account.getType());
            values.put(COL_USERNAME, account.getName());
            values.put(COL_STORE_NAME, account.getStore_name());


            return db.insert(TABLE_USER, null, values) != -1;
        } finally {
            db.close();
        }
    }

    public Cursor getUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USER, null);
    }

    // Product methods
    public boolean add_product(String userId, String product_name, float product_price,
                               int product_quantity, byte[] productImage) {
        SQLiteDatabase db = null;
        Cursor cursor = getProduct();

        // Check for duplicate product names
        while (cursor.moveToNext()) {
            if (cursor.getString(2).equals(product_name)) {
                cursor.close();
                return false;
            }
        }
        cursor.close();

        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            // Verify user exists first
            cursor = db.query(TABLE_USER,
                    new String[]{COL_USER_ID},
                    COL_USER_ID + "=?",
                    new String[]{userId},
                    null, null, null);

            boolean userExists = cursor.getCount() > 0;
            cursor.close();

            if (!userExists) {
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(COL_USER_ID, Integer.parseInt(userId));
            values.put(COL_PRODUCT_NAME, product_name);
            values.put(COL_PRICE, product_price);
            values.put(COL_QUANTITY, product_quantity);
            values.put(COL_OVerQUANTITY, product_quantity);

            // Add the image if it exists
            if (productImage != null) {
                values.put(COL_PRODUCT_IMAGE, productImage);
            }

            long result = db.insert(TABLE_PRODUCT, null, values);
            db.setTransactionSuccessful();
            return result != -1;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error adding product", e);
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    public Cursor getProduct() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCT,
                new String[]{COL_PRODUCT_ID, COL_USER_ID, COL_PRODUCT_NAME,
                        COL_PRICE, COL_QUANTITY, COL_OVerQUANTITY, COL_PRODUCT_IMAGE},
                null, null, null, null, null);
    }

    // Checkout methods
    public boolean add_checkoutproduct(String productId, String product_name,
                                       String product_price, String product_quantity,
                                       String total_price, byte[] productImage) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            // Check for existing product in checkout
            cursor = db.query(TABLE_CHECKOUT,
                    new String[]{COL_PRODUCT_ID},
                    COL_PRODUCT_ID + "=?",
                    new String[]{productId},
                    null, null, null);

            if (cursor.getCount() > 0) {
                return false; // Product already in checkout
            }
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COL_PRODUCT_ID, productId);
            values.put(COL_PRODUCT_NAME, product_name);
            values.put(COL_PRICE, product_price);
            values.put(COL_QUANTITY, product_quantity);
            values.put(COL_TOTAL_PRICE, total_price);

            // Add product image if available
            if (productImage != null) {
                values.put(COL_PRODUCT_IMAGE, productImage);
            }

            long result = db.insert(TABLE_CHECKOUT, null, values);
            db.setTransactionSuccessful();
            return result != -1;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error adding checkout product", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    public boolean add_sold_product(String productId, String productName,
                                  String productPrice, String quantity,
                                  String totalPrice, byte[] productImage) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(COL_PRODUCT_ID, productId);
            values.put(COL_PRODUCT_NAME, productName);
            values.put(COL_PRICE, productPrice);
            values.put(COL_QUANTITY, quantity);
            values.put(COL_TOTAL_PRICE, totalPrice);

            // Add image if provided
            if (productImage != null) {
                values.put(COL_PRODUCT_IMAGE, productImage);
            }

            long result = db.insert(TABLE_SOLD, null, values);
            db.setTransactionSuccessful();
            return result != -1;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error adding sold product: " + productName, e);
            return false;
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                    db.close();
                } catch (Exception e) {
                    Log.e("DB_ERROR", "Error closing database", e);
                }
            }
        }
    }
    public Cursor getSoldProducts() {
        SQLiteDatabase db = null;
        try {
            db = this.getReadableDatabase();
            return db.query(TABLE_SOLD,
                    new String[]{
                            COL_CHECKOUT_ID,    // Primary key of sold items table
                            COL_PRODUCT_ID,     // Reference to original product
                            COL_PRODUCT_NAME,   // Product name at time of sale
                            COL_PRICE,          // Price at time of sale
                            COL_QUANTITY,       // Quantity sold
                            COL_TOTAL_PRICE,    // Total price (price * quantity)
                            COL_PRODUCT_IMAGE   // Product image at time of sale
                    },
                    null,       // WHERE clause
                    null,       // WHERE args
                    null,       // GROUP BY
                    null,       // HAVING
                    null        // ORDER BY
            );
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error retrieving sold products", e);
            return null;
        }
    }

    public Cursor getCheckoutItems() {
        SQLiteDatabase db;
            db = this.getReadableDatabase();
            return db.query(TABLE_CHECKOUT,
                    new String[]{
                            COL_CHECKOUT_ID,    // Primary key of checkout table
                            COL_PRODUCT_ID,     // Reference to product
                            COL_PRODUCT_NAME,   // Product name
                            COL_PRICE,          // Unit price
                            COL_QUANTITY,       // Quantity in cart
                            COL_TOTAL_PRICE,    // price * quantity
                            COL_PRODUCT_IMAGE   // Product image
                    },
                    null, null, null, null, null);
    }

    public boolean delete_checkout_Product(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_CHECKOUT,
                    COL_CHECKOUT_ID + " = ?",
                    new String[]{id});
            return rowsDeleted > 0;
        } finally {
            db.close();
        }
    }

    public boolean deleteProduct(String id) {
        
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_PRODUCT,
                    COL_PRODUCT_ID + " = ?",
                    new String[]{id});
            return rowsDeleted > 0;
        } finally {
            db.close();
        }
    }
    public boolean update_checkout_Product(productobject product) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_QUANTITY, product.getQuantity());


            int rowsUpdated = db.update(TABLE_PRODUCT, values,
                    COL_PRODUCT_ID + " = ?",
                    new String[]{product.getId()});
            return rowsUpdated > 0;
        } finally {
            db.close();
        }
    }
    public boolean update_Product(productobject product) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_PRODUCT_NAME, product.getName());
            values.put(COL_PRICE, product.getPrice());
            values.put(COL_QUANTITY, product.getQuantity());
            values.put(COL_OVerQUANTITY, product.getQuantity());


            int rowsUpdated = db.update(TABLE_PRODUCT, values,
                    COL_PRODUCT_ID + " = ?",
                    new String[]{product.getId()});
            return rowsUpdated > 0;
        } finally {
            db.close();
        }
    }

    public boolean updatecheckout(String id, String newQuantity, String newTotalPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_QUANTITY, newQuantity);
            values.put(COL_TOTAL_PRICE, newTotalPrice);

            int rowsUpdated = db.update(TABLE_CHECKOUT, values,
                    COL_CHECKOUT_ID + " = ?",
                    new String[]{id});
            return rowsUpdated > 0;
        } finally {
            db.close();
        }
    }




}