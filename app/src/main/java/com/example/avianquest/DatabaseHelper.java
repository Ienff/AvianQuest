package com.example.avianquest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BirdSurvey.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SAMPLE_POINTS = "sample_points";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_BIRD_SPECIES = "bird_species";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_HABITAT_TYPE = "habitat_type";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_REMARKS = "remarks";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_SAMPLE_POINTS + " (" +
            COLUMN_ID + " TEXT PRIMARY KEY," +
            COLUMN_TIME + " TEXT," +
            COLUMN_LATITUDE + " REAL," +
            COLUMN_LONGITUDE + " REAL," +
            COLUMN_BIRD_SPECIES + " TEXT," +
            COLUMN_GENDER + " TEXT," +
            COLUMN_QUANTITY + " INTEGER," +
            COLUMN_HABITAT_TYPE + " TEXT," +
            COLUMN_DISTANCE + " INTEGER," +
            COLUMN_STATUS + " TEXT," +
            COLUMN_REMARKS + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPLE_POINTS);
        onCreate(db);
    }
}