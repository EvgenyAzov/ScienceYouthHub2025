package com.example.scienceyouthhub;

import android.app.AlertDialog;
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
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<ActivityModel> activityList = new ArrayList<>();
    private FirebaseFirestore db;
    private Map<String, String> instructorMap = new HashMap<>(); // instructorId -> name

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        recyclerView = view.findViewById(R.id.activitiesRecyclerView);
        FloatingActionButton addFab = view.findViewById(R.id.addActivityFab);

        db = FirebaseFirestore.getInstance();
        adapter = new ActivityAdapter(activityList, new ActivityAdapter.OnActivityActionListener() {
            @Override
            public void onEdit(ActivityModel activity) { showActivityDialog(activity, true); }
            @Override
            public void onDelete(ActivityModel activity) { deleteActivity(activity); }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Исправленный обработчик: диалог открывается только после загрузки инструкторов
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
                        showActivityDialog(null, false); // Открываем после загрузки!
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Ошибка загрузки руководителей", Toast.LENGTH_SHORT).show()
                    );
        });

        loadActivities();
        return view;
    }

    private void loadActivities() {
        // Сначала загрузим всех руководителей (для отображения имен в списке)
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
                    // Теперь загрузим кружки
                    db.collection("activities")
                            .get()
                            .addOnSuccessListener(activitySnapshot -> {
                                activityList.clear();
                                for (DocumentSnapshot doc : activitySnapshot) {
                                    ActivityModel activity = doc.toObject(ActivityModel.class);
                                    if (activity != null) {
                                        // Присваиваем имя руководителя для показа
                                        String instrName = instructorMap.get(activity.getInstructorId());
                                        activity.setInstructorName(instrName != null ? instrName : "—");
                                        activityList.add(activity);
                                    }
                                }
                                adapter.setActivities(activityList);
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
        List<String> instructorNames = new ArrayList<>(instructorMap.values());
        List<String> instructorIds = new ArrayList<>(instructorMap.keySet());
        ArrayAdapter<String> instrAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, instructorNames);
        instrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instructorSpinner.setAdapter(instrAdapter);

        if (activity != null) {
            nameInput.setText(activity.getName());
            categoryInput.setText(activity.getCategory());
            ageRangeInput.setText(activity.getAgeRange());
            descriptionInput.setText(activity.getDescription());
            daysInput.setText(activity.getDays());
            maxParticipantsInput.setText(String.valueOf(activity.getMaxParticipants()));
            // Выбираем руководителя по id
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
