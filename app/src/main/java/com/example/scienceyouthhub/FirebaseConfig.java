package com.example.scienceyouthhub;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

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

    /**
     * Saves a user to Firestore (as a static method)
     */
    public static void saveUserToFirestore(UserModel user) {
        FirebaseFirestore db = getInstance().getFirestore();
        db.collection("users")
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseConfig", "User saved to Firestore: " + user.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseConfig", "Failed to save user: " + e.getMessage());
                });
    }

    /**
     * (Optional!) Get a user by ID (if needed)
     */
    public static void getUserFromFirestore(String userId, OnSuccessListener<DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = getInstance().getFirestore();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
