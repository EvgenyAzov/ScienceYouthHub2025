package com.example.scienceyouthhub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class ParentChildrenFragment extends Fragment {

    private RecyclerView childrenRecyclerView;
    private ParentChildAdapter childAdapter;
    private List<UserModel> myChildren = new ArrayList<>();
    private String parentId;
    private Button addChildBtn;

    private List<ActivityModel> availableActivities = new ArrayList<>(); // Для выбора занятия

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_children, container, false);

        childrenRecyclerView = view.findViewById(R.id.childrenRecyclerView);
        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        childAdapter = new ParentChildAdapter(myChildren, this::showAddToActivityDialog);
        childrenRecyclerView.setAdapter(childAdapter);

        addChildBtn = view.findViewById(R.id.addChildBtn);

        parentId = getParentIdFromPrefs();
        Log.d("PARENT_ID", "parentId from prefs: " + parentId);

        if (parentId == null || parentId.isEmpty()) {
            Toast.makeText(getContext(), "Ошибка: не удалось определить id родителя", Toast.LENGTH_LONG).show();
            addChildBtn.setEnabled(false);
            return view;
        }

        loadMyChildren();

        addChildBtn.setOnClickListener(v -> showSelectStudentDialog());

        return view;
    }

    private String getParentIdFromPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        // Добавлено user_id (он должен устанавливаться при логине!)
        return prefs.contains("user_id") ? prefs.getString("user_id", null) : null;
    }

    private void loadMyChildren() {
        if (parentId == null || parentId.isEmpty()) return;
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("type", "Student")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myChildren.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        UserModel child = doc.toObject(UserModel.class);
                        if (child != null) myChildren.add(child);
                    }
                    childAdapter.notifyDataSetChanged();
                });
    }

    // Показываем диалог выбора студента
    private void showSelectStudentDialog() {
        if (parentId == null || parentId.isEmpty()) {
            Toast.makeText(getContext(), "Ошибка: id родителя не найден!", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("type", "Student")
                .whereEqualTo("parentId", null) // Только те, у кого нет родителя
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<UserModel> availableStudents = new ArrayList<>();
                    List<String> studentNames = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        UserModel student = doc.toObject(UserModel.class);
                        if (student != null) {
                            availableStudents.add(student);
                            studentNames.add(student.getName() + " (" + student.getId() + ")");
                        }
                    }
                    if (availableStudents.isEmpty()) {
                        Toast.makeText(getContext(), "Нет доступных студентов для добавления", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Выберите ребёнка")
                            .setItems(studentNames.toArray(new String[0]), (dialog, which) -> {
                                UserModel selectedStudent = availableStudents.get(which);

                                // --- ДОБАВЛЯЕМ parentId студенту
                                FirebaseFirestore.getInstance().collection("users")
                                        .document(selectedStudent.getId())
                                        .update("parentId", parentId)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Ребёнок добавлен!", Toast.LENGTH_SHORT).show();
                                            loadMyChildren();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                });
    }

    // --- Диалог добавления ребёнка в активность ---
    private void showAddToActivityDialog(UserModel child) {
        // 1. Загрузить все доступные активности (занятия)
        FirebaseFirestore.getInstance().collection("activities")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    availableActivities.clear();
                    List<String> activityNames = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ActivityModel act = doc.toObject(ActivityModel.class);
                        if (act != null) {
                            availableActivities.add(act);
                            activityNames.add(act.getName());
                        }
                    }
                    if (availableActivities.isEmpty()) {
                        Toast.makeText(getContext(), "Нет доступных занятий", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2. Показываем диалог выбора активности
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Добавить \"" + child.getName() + "\" в занятие")
                            .setItems(activityNames.toArray(new String[0]), (dialog, which) -> {
                                ActivityModel selectedActivity = availableActivities.get(which);
                                addChildToActivity(child, selectedActivity);
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                });
    }

    // --- Добавить ребёнка в выбранную активность ---
    private void addChildToActivity(UserModel child, ActivityModel activity) {
        // Здесь добавляем id ребёнка в myActivities (или можно в отдельную коллекцию, если логика другая)
        FirebaseFirestore.getInstance().collection("users")
                .document(child.getId())
                .update("myActivities", FieldValue.arrayUnion(activity.getId()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Ребёнок добавлен в занятие!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // --- Если нужно, можно также добавить ребёнка в список участников активности:
        FirebaseFirestore.getInstance().collection("activities")
                .document(activity.getId())
                .update("participants", FieldValue.arrayUnion(child.getId()))
                .addOnSuccessListener(aVoid -> {
                    // Optionally: уведомление или лог
                });
    }
}
