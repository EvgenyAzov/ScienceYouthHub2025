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

        // Используем массив для обхода ограничения "effectively final"
        boolean[] hasErrorsArr = new boolean[1];  // Инициализируем массив с false
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
                        hasErrorsArr[0] = true;  // Изменяем первый элемент массива
                        latch.countDown();
                    });
        }

        // Ждём завершения всех операций
        try {
            latch.await();  // Блокируем до завершения всех асинхронных задач
        } catch (InterruptedException e) {
            Log.e("SyncWorker", "Interrupted while waiting", e);
            return Result.retry();
        }

        // Возвращаем результат
        if (hasErrorsArr[0]) {
            return Result.retry();  // Повторить, если были ошибки
        } else {
            return Result.success();  // Успешно, если все синхронизированы
        }
    }
}