package com.example.scienceyouthhub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database helper for managing local storage.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ScienceYouthHub.db";
    private static final int DB_VERSION = 2;  // Увеличили версию для добавления isDirty

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Users (id TEXT PRIMARY KEY, type TEXT, name TEXT, age INTEGER, isDirty INTEGER DEFAULT 1)");
        db.execSQL("CREATE TABLE Activities (id TEXT PRIMARY KEY, name TEXT, category TEXT, ageRange TEXT, description TEXT, days TEXT, maxParticipants INTEGER, instructorId TEXT)");
        db.execSQL("CREATE TABLE UserActivities (userId TEXT, activityId TEXT, PRIMARY KEY (userId, activityId))");
        db.execSQL("CREATE TABLE Feedbacks (id TEXT PRIMARY KEY, activityId TEXT, userId TEXT, score INTEGER, comment TEXT)");
        db.execSQL("CREATE TABLE Photos (id TEXT PRIMARY KEY, activityId TEXT, date TEXT, image TEXT)");
        Log.d("DatabaseHelper", "Tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE Users ADD COLUMN isDirty INTEGER DEFAULT 1");
        }
        // Если нужно, дроп и recreate для других версий
    }

    // Метод для вставки/обновления пользователя
    public void insertOrUpdateUser(UserModel user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", user.getId());
        values.put("type", user.getType());
        values.put("name", user.getName());
        values.put("age", user.getAge());
        values.put("isDirty", 1);  // Всегда грязный при локальном сохранении

        long result = db.insertWithOnConflict("Users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (result == -1) {
            Log.e("DatabaseHelper", "Failed to insert/update user");
        }
    }

    // Получить пользователя по ID
    public UserModel getUserById(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("Users", null, "id = ?", new String[]{userId}, null, null, null);
        if (cursor.moveToFirst()) {
            UserModel user = new UserModel(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("age")),
                    cursor.getString(cursor.getColumnIndexOrThrow("type"))
            );
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }

    // Получить "грязных" пользователей для синхронизации
    public List<UserModel> getDirtyUsers() {
        List<UserModel> dirtyUsers = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("Users", null, "isDirty = 1", null, null, null, null);
        while (cursor.moveToNext()) {
            UserModel user = new UserModel(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("age")),
                    cursor.getString(cursor.getColumnIndexOrThrow("type"))
            );
            dirtyUsers.add(user);
        }
        cursor.close();
        return dirtyUsers;
    }

    // Сброс isDirty после синхронизации
    public void markUserSynced(String userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isDirty", 0);
        db.update("Users", values, "id = ?", new String[]{userId});
    }

    public void logAllUsers() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users", null);
        while (cursor.moveToNext()) {
            Log.d("DatabaseHelper", "User: id=" + cursor.getString(0)
                    + ", type=" + cursor.getString(1)
                    + ", name=" + cursor.getString(2)
                    + ", age=" + cursor.getInt(3)
                    + ", isDirty=" + cursor.getInt(4));
        }
        cursor.close();
    }

}