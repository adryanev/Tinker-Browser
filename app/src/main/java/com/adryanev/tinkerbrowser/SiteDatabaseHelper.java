package com.adryanev.tinkerbrowser;

import android.content.ContentValues;
import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Patterns;

/**
 * Created by AdryanEV on 05/06/2016.
 */
public class SiteDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "tinkerbrowser";
    private static final int DB_VERSION = 2;


    public SiteDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE blockedSite (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "website TEXT); ";
        db.execSQL(sql);

        insertSite(db,"http://www.kaskus.co.id/");
        insertSite(db,"http://kumpulbagi.id/");

    }

    private void insertSite(SQLiteDatabase db, String s) {
        ContentValues siteValues = new ContentValues();
        siteValues.put("website",s);
        db.insert("blockedSite",null,siteValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
