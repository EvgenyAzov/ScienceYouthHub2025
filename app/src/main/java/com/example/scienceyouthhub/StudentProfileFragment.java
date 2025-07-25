package com.example.scienceyouthhub;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class StudentProfileFragment extends Fragment {

    private ImageView profileImageView;
    private TextView studentNameTextView;
    private RecyclerView scheduleRecyclerView;
    private TextView monthStatsTextView, categoryDistributionTextView;

    private StudentScheduleAdapter studentScheduleAdapter;
    private List<ActivityModel> mySchedule = new ArrayList<>();

    private UserModel student;
    private String studentId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        studentNameTextView = view.findViewById(R.id.studentNameTextView);
        scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView);
        monthStatsTextView = view.findViewById(R.id.monthStatsTextView);
        categoryDistributionTextView = view.findViewById(R.id.categoryDistributionTextView);

        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        studentScheduleAdapter = new StudentScheduleAdapter(mySchedule);
        scheduleRecyclerView.setAdapter(studentScheduleAdapter);

        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadStudentProfile();

        return view;
    }

    private void loadStudentProfile() {
        FirebaseFirestore.getInstance().collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener(doc -> {
                    student = doc.toObject(UserModel.class);
                    if (student == null) return;

                    studentNameTextView.setText(student.getName());

                    // Показываем фото студента (если есть)
                    loadStudentPhoto();

                    loadStudentSchedule();
                });
    }


    private void loadStudentSchedule() {
        List<String> activitiesIds = student.getMyActivities();
        if (activitiesIds == null || activitiesIds.isEmpty()) {
            monthStatsTextView.setText("Нет занятий");
            categoryDistributionTextView.setText("");
            return;
        }
        // Исключаем дубли
        Set<String> uniqueIds = new HashSet<>(activitiesIds);

        FirebaseFirestore.getInstance().collection("activities")
                .whereIn("id", new ArrayList<>(uniqueIds))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    mySchedule.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ActivityModel activity = doc.toObject(ActivityModel.class);
                        if (activity != null) mySchedule.add(activity);
                    }
                    studentScheduleAdapter.notifyDataSetChanged();

                    loadMonthlyStats();
                });
    }

    private void loadMonthlyStats() {
        // Для текущего месяца
        Calendar calendar = Calendar.getInstance();
        int curMonth = calendar.get(Calendar.MONTH);
        int curYear = calendar.get(Calendar.YEAR);

        int count = 0;
        final double[] totalRating = {0.0};
        final int[] feedbacksCount = {0};
        Map<String, Integer> categoryMap = new HashMap<>(); // категория → количество

        // Проходим по всем занятиям
        for (ActivityModel act : mySchedule) {
            if (act.getStartDate() == null) continue;
            Calendar actCal = Calendar.getInstance();
            actCal.setTime(act.getStartDate());
            int actMonth = actCal.get(Calendar.MONTH);
            int actYear = actCal.get(Calendar.YEAR);

            if (actMonth == curMonth && actYear == curYear) {
                count++;
                // Категория
                String cat = act.getCategory();
                if (cat != null) {
                    int prev = categoryMap.getOrDefault(cat, 0);
                    categoryMap.put(cat, prev + 1);
                }
            }
        }

        // Загружаем отзывы для всех своих занятий за месяц
        int finalCount = count;
        FirebaseFirestore.getInstance().collection("feedbacks")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        // Проверяем, что feedback относится к занятию этого месяца
                        String activityId = doc.getString("activityId");
                        Double rating = doc.getDouble("rating");
                        if (activityId != null && rating != null) {
                            for (ActivityModel act : mySchedule) {
                                if (act.getId().equals(activityId)) {
                                    Calendar actCal = Calendar.getInstance();
                                    actCal.setTime(act.getStartDate());
                                    int actMonth = actCal.get(Calendar.MONTH);
                                    int actYear = actCal.get(Calendar.YEAR);
                                    if (actMonth == curMonth && actYear == curYear) {
                                        totalRating[0] += rating;
                                        feedbacksCount[0]++;
                                    }
                                }
                            }
                        }
                    }

                    // Выводим статистику
                    String stats = "Занятий в этом месяце: " + finalCount +
                            "\nСредний балл: " +
                            (feedbacksCount[0] > 0 ? String.format(Locale.US, "%.2f", totalRating[0] / feedbacksCount[0]) : "Нет отзывов");

                    StringBuilder dist = new StringBuilder("Распределение по направлениям:\n");
                    for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
                        dist.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }

                    monthStatsTextView.setText(stats);
                    categoryDistributionTextView.setText(dist.toString());
                });
    }

    private void loadStudentPhoto() {
        Log.d("PHOTOS", "loadStudentPhoto() called!");

        List<String> activitiesIds = student.getMyActivities();
        Log.d("PHOTOS", "activitiesIds: " + activitiesIds);

        if (activitiesIds == null || activitiesIds.isEmpty()) {
            Log.d("PHOTOS", "No activities for student!");
            return;
        }

        FirebaseFirestore.getInstance().collection("photos")
                .whereIn("activityId", activitiesIds)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("PHOTOS", "onSuccessListener: photos.size = " + snapshot.size());

                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);

                        String base64 = null;
                        if (doc.contains("photoBase64")) base64 = doc.getString("photoBase64");
                        else if (doc.contains("imageBase64")) base64 = doc.getString("imageBase64");
                        else if (doc.contains("base64")) base64 = doc.getString("base64");

                        Log.d("PHOTOS", "base64: " + (base64 != null ? base64.substring(0, Math.min(30, base64.length())) + "..." : "null"));

                        if (base64 != null && !base64.isEmpty()) {
                            byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            profileImageView.setImageBitmap(bitmap);
                            Log.d("PHOTOS", "Photo decoded and set to ImageView");
                        } else {
                            Log.d("PHOTOS", "Base64 is null or empty");
                        }
                    } else {
                        Log.d("PHOTOS", "No photos found for student activities");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PHOTOS", "Firestore error: ", e);
                });
    }





}
