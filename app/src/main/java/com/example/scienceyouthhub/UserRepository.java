package com.example.scienceyouthhub;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private DatabaseHelper localDbHelper;
    private FirebaseFirestore remoteDb = FirebaseConfig.getInstance().getFirestore();

    public UserRepository(Context context) {
        localDbHelper = new DatabaseHelper(context);
    }

    // Сохранение пользователя
    public void saveUser(Context context, UserModel user) {
        // Всегда сохраняем локально
        localDbHelper.insertOrUpdateUser(user);

        if (isOnline(context)) {
            // Сохраняем в Firestore
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", user.getName());
            userData.put("age", user.getAge());
            userData.put("type", user.getType());

            remoteDb.collection("users").document(user.getId())
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d("UserRepository", "User synced to Firestore");
                        localDbHelper.markUserSynced(user.getId());  // Сброс флага
                    })
                    .addOnFailureListener(e -> Log.e("UserRepository", "Sync failed", e));
        } else {
            // Планируем синхронизацию
            scheduleSync(context);
        }
    }

    // Загрузка пользователя
    public UserModel getUser(Context context, String userId) {
        UserModel localUser = localDbHelper.getUserById(userId);
        if (localUser != null) {
            return localUser;
        }

        if (isOnline(context)) {
            // Загружаем из Firestore и сохраняем локально
            remoteDb.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            UserModel user = new UserModel(
                                    userId,
                                    document.getString("name"),
                                    document.getLong("age").intValue(),
                                    document.getString("type")
                            );
                            saveUser(context, user);  // Сохраняем локально
                        }
                    })
                    .addOnFailureListener(e -> Log.e("UserRepository", "Load failed", e));
        }

        return null;
    }

    // Проверка интернета
    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    // Планирование синхронизации
    private void scheduleSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueue(syncWork);
    }
}