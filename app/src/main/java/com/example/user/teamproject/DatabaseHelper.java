package com.example.user.teamproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_Name = "profile";
    private static final String Col1 = "ID";
    private static final String Col2 = "image";
    private static final String Col3 = "username";
    private static final String Col4 = "password";
    private static final String Col5 = "name";

    public DatabaseHelper(Context context) {
        super(context, TABLE_Name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_Name + "(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Col2 + " BLOB, " +
                Col3 + " TEXT, " +
                Col4 + " TEXT, " +
                Col5 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE　IF EXISTS " + TABLE_Name);
        onCreate(db);
    }

    public boolean addData(byte image[], String username, String password, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Col2, image);
        contentValues.put(Col3, username);
        contentValues.put(Col4, password);
        contentValues.put(Col5, name);

        long result = db.insert(TABLE_Name, null, contentValues);

        //if data as insert incorrectly, it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Name;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    //delete data from db
    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_Name, "ID = ?",new String[] {id});
    }
}
