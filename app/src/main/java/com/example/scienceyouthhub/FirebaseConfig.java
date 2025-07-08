package com.example.scienceyouthhub;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Singleton class for Firebase Firestore configuration.
 */
public class FirebaseConfig {
    private static FirebaseConfig instance;
    private final FirebaseFirestore db;

    private FirebaseConfig() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseConfig getInstance() {
        if (instance == null) {
            instance = new FirebaseConfig();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore() {
        Log.d("FirebaseConfig", "Firestore instance accessed");
        return db;
    }
}