package com.example.scienceyouthhub;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class FeedbackFragment extends Fragment {
    private RecyclerView feedbackRecyclerView;
    private FeedbackAdapter feedbackAdapter;
    private List<FeedbackModel> feedbackList = new ArrayList<>();

    private FloatingActionButton addFeedbackFab;
    private String selectedActivityId;
    private String selectedActivityName;

    private List<String> activityIds = new ArrayList<>();
    private List<String> activityNames = new ArrayList<>();
    private String userRole = "Admin"; // <-- динамически по своей логике

    public FeedbackFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);

        feedbackRecyclerView = v.findViewById(R.id.feedbackRecyclerView);
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedbackAdapter = new FeedbackAdapter(feedbackList, userRole, feedback -> showEditFeedbackDialog(feedback));
        feedbackRecyclerView.setAdapter(feedbackAdapter);

        addFeedbackFab = v.findViewById(R.id.addFeedbackFab);
        addFeedbackFab.setOnClickListener(view -> showAddFeedbackDialog());

        loadAllFeedbacks();

        return v;
    }

    private void loadAllFeedbacks() {
        feedbackList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("activities")
                .get()
                .addOnSuccessListener(activitySnapshots -> {
                    for (QueryDocumentSnapshot activityDoc : activitySnapshots) {
                        String activityId = activityDoc.getId();
                        String activityName = activityDoc.getString("name");
                        db.collection("activities")
                                .document(activityId)
                                .collection("feedbacks")
                                .get()
                                .addOnSuccessListener(feedbackSnapshots -> {
                                    for (QueryDocumentSnapshot feedbackDoc : feedbackSnapshots) {
                                        String feedbackId = feedbackDoc.getId();
                                        String userId = feedbackDoc.getString("userId");
                                        String userName = feedbackDoc.getString("userName");
                                        String comment = feedbackDoc.getString("comment");
                                        int rating = feedbackDoc.getLong("rating") == null ? 0 : feedbackDoc.getLong("rating").intValue();
                                        if (!TextUtils.isEmpty(comment)) {
                                            feedbackList.add(new FeedbackModel(feedbackId, activityId, activityName, userId, userName, comment, rating));
                                        }
                                    }
                                    feedbackAdapter.setFeedbacks(feedbackList);
                                });
                    }
                });
    }

    private void showAddFeedbackDialog() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("activities")
                .get()
                .addOnSuccessListener(activitySnapshots -> {
                    activityIds.clear();
                    activityNames.clear();
                    for (QueryDocumentSnapshot doc : activitySnapshots) {
                        activityIds.add(doc.getId());
                        activityNames.add(doc.getString("name"));
                    }
                    if (activityIds.isEmpty()) {
                        Toast.makeText(getContext(), "Нет доступных активностей!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_feedback, null, false);
                    Spinner activitySpinner = dialogView.findViewById(R.id.activitySpinner);
                    RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
                    EditText commentEditText = dialogView.findViewById(R.id.commentEditText);

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, activityNames);
                    activitySpinner.setAdapter(spinnerAdapter);

                    activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedActivityId = activityIds.get(position);
                            selectedActivityName = activityNames.get(position);
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Добавить отзыв")
                            .setView(dialogView)
                            .setPositiveButton("Сохранить", (d, w) -> {
                                int rating = (int) ratingBar.getRating();
                                String comment = commentEditText.getText().toString().trim();

                                if (TextUtils.isEmpty(comment)) {
                                    Toast.makeText(getContext(), "Введите текст отзыва!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String userName = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                                Map<String, Object> data = new HashMap<>();
                                data.put("comment", comment);
                                data.put("rating", rating);
                                data.put("userId", userId);
                                data.put("userName", userName);
                                data.put("activityName", selectedActivityName);
                                data.put("timestamp", System.currentTimeMillis());

                                db.collection("activities")
                                        .document(selectedActivityId)
                                        .collection("feedbacks")
                                        .add(data)
                                        .addOnSuccessListener(ref -> {
                                            Toast.makeText(getContext(), "Отзыв добавлен!", Toast.LENGTH_SHORT).show();
                                            loadAllFeedbacks();
                                        });
                            })
                            .setNegativeButton("Отмена", null)
                            .create();
                    dialog.show();
                });
    }

    private void showEditFeedbackDialog(FeedbackModel feedback) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_feedback, null, false);
        Spinner activitySpinner = dialogView.findViewById(R.id.activitySpinner);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText commentEditText = dialogView.findViewById(R.id.commentEditText);

        // Подготовим список активностей
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("activities")
                .get()
                .addOnSuccessListener(activitySnapshots -> {
                    activityNames.clear();
                    activityIds.clear();
                    for (QueryDocumentSnapshot doc : activitySnapshots) {
                        activityIds.add(doc.getId());
                        activityNames.add(doc.getString("name"));
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, activityNames);
                    activitySpinner.setAdapter(spinnerAdapter);
                    int activityPos = activityNames.indexOf(feedback.getActivityName());
                    if (activityPos >= 0) activitySpinner.setSelection(activityPos);

                    activitySpinner.setEnabled(false); // нельзя менять кружок

                    ratingBar.setRating(feedback.getRating());
                    commentEditText.setText(feedback.getComment());

                    new AlertDialog.Builder(getContext())
                            .setTitle("Редактировать отзыв")
                            .setView(dialogView)
                            .setPositiveButton("Сохранить", (d, w) -> {
                                int rating = (int) ratingBar.getRating();
                                String comment = commentEditText.getText().toString().trim();

                                if (TextUtils.isEmpty(comment)) {
                                    Toast.makeText(getContext(), "Введите текст отзыва!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Map<String, Object> update = new HashMap<>();
                                update.put("comment", comment);
                                update.put("rating", rating);

                                FirebaseFirestore.getInstance()
                                        .collection("activities")
                                        .document(feedback.getActivityId())
                                        .collection("feedbacks")
                                        .document(feedback.getFeedbackId())
                                        .update(update)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(getContext(), "Отзыв обновлён!", Toast.LENGTH_SHORT).show();
                                            loadAllFeedbacks();
                                        });
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                });
    }
}
