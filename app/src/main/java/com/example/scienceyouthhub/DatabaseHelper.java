package com.example.scienceyouthhub;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite database helper for managing local storage.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ScienceYouthHub.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Users (id TEXT PRIMARY KEY, type TEXT, name TEXT, age INTEGER)");
        db.execSQL("CREATE TABLE Activities (id TEXT PRIMARY KEY, name TEXT, category TEXT, ageRange TEXT, description TEXT, days TEXT, maxParticipants INTEGER, instructorId TEXT)");
        db.execSQL("CREATE TABLE UserActivities (userId TEXT, activityId TEXT, PRIMARY KEY (userId, activityId))");
        db.execSQL("CREATE TABLE Feedbacks (id TEXT PRIMARY KEY, activityId TEXT, userId TEXT, score INTEGER, comment TEXT)");
        db.execSQL("CREATE TABLE Photos (id TEXT PRIMARY KEY, activityId TEXT, date TEXT, image TEXT)");
        Log.d("DatabaseHelper", "Users table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Activities");
        db.execSQL("DROP TABLE IF EXISTS UserActivities");
        db.execSQL("DROP TABLE IF EXISTS Feedbacks");
        db.execSQL("DROP TABLE IF EXISTS Photos");
        onCreate(db);
    }
}