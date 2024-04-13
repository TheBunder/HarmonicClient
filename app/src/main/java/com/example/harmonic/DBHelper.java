package com.example.harmonic;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;

import java.security.SecureRandom;


public class DBHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "users";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_SALT = "salt";

    private static final String TAG = "DBHelper";

    public DBHelper(Context context) {
        super(context, "Login.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        MyDB.execSQL("create Table users(username TEXT primary key, salt BLOB, password Text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) { // Check for version change
            MyDB.execSQL("drop Table if exists users");
            onCreate(MyDB);  // Call onCreate to create the table with the new column
        }
    }

    private static final int ITERATION_COUNT = 1024; // Adjust as needed (higher for slower attacks)
    private static final int KEY_LENGTH = 256; // Adjust as needed

    public Boolean insertData(String username, String password) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Log.v(TAG, password);

        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt); // Generate random salt

        String hashedPassword = hashPassword(password, salt);
        Log.v(TAG, hashedPassword);

        if (hashedPassword != null) {
            contentValues.put(COLUMN_USERNAME, username);
            contentValues.put(COLUMN_PASSWORD, hashedPassword);
            contentValues.put(COLUMN_SALT, salt); // Add salt to content values

            try {
                long result = MyDB.insert(TABLE_NAME, null, contentValues);
                return result != -1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private String hashPassword(String password, byte[] salt) {
        if (salt == null || salt.length == 0) {
            salt = new byte[32];  // Generate new salt if not provided
            new SecureRandom().nextBytes(salt);
        }

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            // Hash password with salt using PBKDF2
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKey key = factory.generateSecret(spec);
            byte[] hashedBytes = key.getEncoded();
            // Convert hashed bytes to a String for storage (consider using Base64 encoding)
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle error appropriately
        }
    }


    public boolean checkUserName(String username) {
        SQLiteDatabase MyDB = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = MyDB.rawQuery(sql, new String[]{username});
        try {
            return cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
    }

    public Boolean checkUsernamePassword(String username, String password) {
        SQLiteDatabase MyDB = this.getReadableDatabase();
        String sql = "SELECT salt FROM " + TABLE_NAME + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = MyDB.rawQuery(sql, new String[]{username});
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                @SuppressLint("Range") byte[] salt = cursor.getBlob(cursor.getColumnIndex(COLUMN_SALT)); // Get salt from cursor

                String hashedPassword = hashPassword(password, salt); // Hash password with retrieved salt
                assert hashedPassword != null;

                // Use prepared statement to prevent SQL injection
                sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
                cursor = MyDB.rawQuery(sql, new String[]{username, hashedPassword});
                return cursor.getCount() > 0;
            } else {
                return false; // Username not found
            }
        } finally {
            cursor.close();
        }
    }


}
