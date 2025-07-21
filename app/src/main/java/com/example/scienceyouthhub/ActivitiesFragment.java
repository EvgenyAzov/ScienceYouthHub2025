package com.example.scienceyouthhub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<ActivityModel> activityList = new ArrayList<>();
    private FirebaseFirestore db;
    private Map<String, String> instructorMap = new HashMap<>();
    private List<String> myActivities = new ArrayList<>(); // кружки студента

    // Данные залогиненного пользователя
    private String userRole;
    private String userId;
    private String userName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        recyclerView = view.findViewById(R.id.activitiesRecyclerView);
        FloatingActionButton addFab = view.findViewById(R.id.addActivityFab);

        db = FirebaseFirestore.getInstance();

        // Получаем роль, имя и id пользователя
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userRole = prefs.getString("user_role", "");
        userName = prefs.getString("user_name", "");
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        adapter = new ActivityAdapter(activityList, requireContext(), new ActivityAdapter.OnActivityActionListener() {
            @Override
            public void onEdit(ActivityModel activity) { showActivityDialog(activity, true); }
            @Override
            public void onDelete(ActivityModel activity) { deleteActivity(activity); }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // --- FAB виден только для Admin и Instructor
        if ("Admin".equals(userRole) || "Instructor".equals(userRole)) {
            addFab.setVisibility(View.VISIBLE);
            addFab.setOnClickListener(v -> {
                db.collection("users")
                        .whereEqualTo("type", "Instructor")
                        .get()
                        .addOnSuccessListener(userSnapshot -> {
                            instructorMap.clear();
                            for (DocumentSnapshot doc : userSnapshot) {
                                String id = doc.getId();
                                String name = doc.getString("name");
                                instructorMap.put(id, name);
                            }
                            showActivityDialog(null, false);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Ошибка загрузки руководителей", Toast.LENGTH_SHORT).show()
                        );
            });
        } else {
            addFab.setVisibility(View.GONE);
        }

        // Загружаем кружки студента, если он Student
        if ("Student".equals(userRole)) {
            loadMyActivitiesAndActivities();
        } else {
            loadActivities(); // для других ролей
        }

        return view;
    }

    private void loadMyActivitiesAndActivities() {
        if (TextUtils.isEmpty(userId)) return;
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    myActivities.clear();
                    if (doc.contains("myActivities")) {
                        myActivities = (List<String>) doc.get("myActivities");
                    }
                    adapter.setMyActivities(myActivities == null ? new ArrayList<>() : myActivities);
                    loadActivities();
                });
    }

    private void loadActivities() {
        // Загружаем всех руководителей для отображения имён
        db.collection("users")
                .whereEqualTo("type", "Instructor")
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    instructorMap.clear();
                    for (DocumentSnapshot doc : userSnapshot) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        instructorMap.put(id, name);
                    }
                    // Теперь загружаем кружки
                    db.collection("activities")
                            .get()
                            .addOnSuccessListener(activitySnapshot -> {
                                activityList.clear();
                                for (DocumentSnapshot doc : activitySnapshot) {
                                    ActivityModel activity = doc.toObject(ActivityModel.class);
                                    if (activity != null) {
                                        String instrName = instructorMap.get(activity.getInstructorId());
                                        activity.setInstructorName(instrName != null ? instrName : "—");
                                        activityList.add(activity);
                                    }
                                }
                                adapter.setActivities(activityList);
                                // обновляем список кружков в адаптере (для Student)
                                if ("Student".equals(userRole)) {
                                    adapter.setMyActivities(myActivities == null ? new ArrayList<>() : myActivities);
                                }
                            });
                });
    }

    private void showActivityDialog(@Nullable ActivityModel activity, boolean isEdit) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_activity, null);

        EditText nameInput = dialogView.findViewById(R.id.dialogActivityName);
        EditText categoryInput = dialogView.findViewById(R.id.dialogActivityCategory);
        EditText ageRangeInput = dialogView.findViewById(R.id.dialogActivityAgeRange);
        EditText descriptionInput = dialogView.findViewById(R.id.dialogActivityDescription);
        EditText daysInput = dialogView.findViewById(R.id.dialogActivityDays);
        EditText maxParticipantsInput = dialogView.findViewById(R.id.dialogActivityMaxParticipants);
        Spinner instructorSpinner = dialogView.findViewById(R.id.dialogActivityInstructor);

        // Формируем список руководителей для спиннера
        List<String> instructorIds = new ArrayList<>(instructorMap.keySet());
        List<String> instructorNames = new ArrayList<>(instructorMap.values());
        ArrayAdapter<String> instrAdapter;

        // Если Instructor — только он сам, выбор невозможен
        if ("Instructor".equals(userRole)) {
            instructorIds.clear();
            instructorNames.clear();
            instructorIds.add(userId);
            instructorNames.add(userName);

            instrAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, instructorNames);
            instrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            instructorSpinner.setAdapter(instrAdapter);
            instructorSpinner.setSelection(0);
            instructorSpinner.setEnabled(false);
        } else {
            instrAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, instructorNames);
            instrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            instructorSpinner.setAdapter(instrAdapter);
            instructorSpinner.setEnabled(true);
        }

        if (activity != null) {
            nameInput.setText(activity.getName());
            categoryInput.setText(activity.getCategory());
            ageRangeInput.setText(activity.getAgeRange());
            descriptionInput.setText(activity.getDescription());
            daysInput.setText(activity.getDays());
            maxParticipantsInput.setText(String.valueOf(activity.getMaxParticipants()));
            int idx = instructorIds.indexOf(activity.getInstructorId());
            if (idx >= 0) instructorSpinner.setSelection(idx);
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(isEdit ? "Изменить кружок" : "Добавить кружок")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Сохранить" : "Добавить", (d, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String category = categoryInput.getText().toString().trim();
                    String ageRange = ageRangeInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    String days = daysInput.getText().toString().trim();
                    String maxPartStr = maxParticipantsInput.getText().toString().trim();
                    int maxParticipants = TextUtils.isEmpty(maxPartStr) ? 0 : Integer.parseInt(maxPartStr);
                    int instrIndex = instructorSpinner.getSelectedItemPosition();

                    if (name.isEmpty() || category.isEmpty() || ageRange.isEmpty() ||
                            instrIndex < 0 || instructorIds.size() == 0) {
                        Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String instructorId = instructorIds.get(instrIndex);
                    String instructorName = instructorNames.get(instrIndex);

                    String id = isEdit && activity != null ? activity.getId()
                            : db.collection("activities").document().getId();

                    ActivityModel newActivity = new ActivityModel(id, name, category, ageRange, description, days,
                            maxParticipants, instructorId, instructorName);

                    db.collection("activities").document(id)
                            .set(newActivity)
                            .addOnSuccessListener(aVoid -> {
                                loadActivities();
                                Toast.makeText(getContext(), isEdit ? "Сохранено" : "Добавлено", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Отмена", null)
                .create();
        dialog.show();
    }

    private void deleteActivity(ActivityModel activity) {
        db.collection("activities").document(activity.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    loadActivities();
                    Toast.makeText(getContext(), "Кружок удалён", Toast.LENGTH_SHORT).show();
                });
    }
}
