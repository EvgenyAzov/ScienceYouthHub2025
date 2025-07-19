package com.example.scienceyouthhub;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        List<UserModel> dirtyUsers = dbHelper.getDirtyUsers();
        FirebaseFirestore firestore = FirebaseConfig.getInstance().getFirestore();

        // Use an array to bypass "effectively final" restriction
        boolean[] hasErrorsArr = new boolean[1];  // Initialize the array with false
        CountDownLatch latch = new CountDownLatch(dirtyUsers.size());

        for (UserModel user : dirtyUsers) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", user.getName());
            userData.put("age", user.getAge());
            userData.put("type", user.getType());

            firestore.collection("users").document(user.getId())
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        dbHelper.markUserSynced(user.getId());
                        Log.d("SyncWorker", "User synced: " + user.getId());
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SyncWorker", "Sync failed for " + user.getId(), e);
                        hasErrorsArr[0] = true;  // Change the first array element
                        latch.countDown();
                    });
        }

        // Wait for all operations to finish
        try {
            latch.await();  // Block until all async tasks are done
        } catch (InterruptedException e) {
            Log.e("SyncWorker", "Interrupted while waiting", e);
            return Result.retry();
        }

        // Return result
        if (hasErrorsArr[0]) {
            return Result.retry();  // Retry if there were errors
        } else {
            return Result.success();  // Success if all users are synced
        }
    }
}
