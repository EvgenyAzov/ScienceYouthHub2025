package com.example.scienceyouthhub;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class InstructorFeedbacksFragment extends Fragment {

    private Spinner activitySpinner, studentSpinner;
    private EditText feedbackCommentInput;
    private RatingBar feedbackScoreInput;
    private Button submitFeedbackBtn;
    private RecyclerView feedbacksRecyclerView;
    private InstructorFeedbackAdapter feedbackAdapter;

    private List<ActivityModel> myActivities = new ArrayList<>();
    private List<UserModel> myStudents = new ArrayList<>();
    private List<InstructorFeedbackModel> feedbacks = new ArrayList<>();

    private String currentInstructorId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instructor_feedbacks, container, false);

        activitySpinner = view.findViewById(R.id.feedbackActivitySpinner);
        studentSpinner = view.findViewById(R.id.feedbackStudentSpinner);
        feedbackCommentInput = view.findViewById(R.id.feedbackCommentInput);
        feedbackScoreInput = view.findViewById(R.id.feedbackScoreInput);
        submitFeedbackBtn = view.findViewById(R.id.submitFeedbackBtn);
        feedbacksRecyclerView = view.findViewById(R.id.feedbacksRecyclerView);

        feedbacksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedbackAdapter = new InstructorFeedbackAdapter(feedbacks);
        feedbacksRecyclerView.setAdapter(feedbackAdapter);

        currentInstructorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadMyActivities();

        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadStudents();
                loadFeedbacks();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        submitFeedbackBtn.setOnClickListener(v -> submitFeedback());

        return view;
    }

    private void loadMyActivities() {
        FirebaseFirestore.getInstance().collection("activities")
                .whereEqualTo("instructorId", currentInstructorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myActivities.clear();
                    List<String> activityNames = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ActivityModel act = doc.toObject(ActivityModel.class);
                        if (act != null) {
                            myActivities.add(act);
                            activityNames.add(act.getName());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_dropdown_item, activityNames);
                    activitySpinner.setAdapter(adapter);

                    // авто-выбор первой активности
                    if (!myActivities.isEmpty()) {
                        loadStudents();
                        loadFeedbacks();
                    }
                });
    }

    private void loadStudents() {
        int pos = activitySpinner.getSelectedItemPosition();
        if (pos < 0 || myActivities.isEmpty()) {
            myStudents.clear();
            studentSpinner.setAdapter(null);
            return;
        }
        String activityId = myActivities.get(pos).getId();
        FirebaseFirestore.getInstance().collection("users")
                .whereArrayContains("myActivities", activityId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myStudents.clear();
                    List<String> studentNames = new ArrayList<>();
                    studentNames.add("No students :("); // <-- Добавляем первым "None"
                    for (DocumentSnapshot doc : querySnapshot) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null) {
                            myStudents.add(user);
                            studentNames.add(user.getName());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_dropdown_item, studentNames);
                    studentSpinner.setAdapter(adapter);
                });
    }


    private void loadFeedbacks() {
        int pos = activitySpinner.getSelectedItemPosition();
        if (pos < 0 || myActivities.isEmpty()) {
            feedbacks.clear();
            feedbackAdapter.notifyDataSetChanged();
            return;
        }
        String activityId = myActivities.get(pos).getId();
        FirebaseFirestore.getInstance().collection("instructor_feedbacks")
                .whereEqualTo("activityId", activityId)
                .whereEqualTo("instructorId", currentInstructorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    feedbacks.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        InstructorFeedbackModel fb = doc.toObject(InstructorFeedbackModel.class);
                        if (fb != null) feedbacks.add(fb);
                    }
                    feedbackAdapter.setFeedbacks(feedbacks);
                });
    }

    private void submitFeedback() {
        int activityPos = activitySpinner.getSelectedItemPosition();
        int studentPos = studentSpinner.getSelectedItemPosition();
        if (activityPos < 0 || myActivities.isEmpty() || studentPos <= 0) { // studentPos==0 — это None
            Toast.makeText(getContext(), "Выберите мероприятие и участника", Toast.LENGTH_SHORT).show();
            return;
        }
        String activityId = myActivities.get(activityPos).getId();
        String studentId = myStudents.get(studentPos - 1).getId(); // -1 из-за None
        String comment = feedbackCommentInput.getText().toString().trim();
        int score = Math.round(feedbackScoreInput.getRating());

        if (TextUtils.isEmpty(comment) || score < 1) {
            Toast.makeText(getContext(), "Введите комментарий и оценку!", Toast.LENGTH_SHORT).show();
            return;
        }

        InstructorFeedbackModel fb = new InstructorFeedbackModel(
                UUID.randomUUID().toString(),
                activityId,
                currentInstructorId,
                studentId,
                new Date(),
                comment,
                score
        );

        FirebaseFirestore.getInstance().collection("instructor_feedbacks")
                .document(fb.getId())
                .set(fb)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Отзыв сохранён", Toast.LENGTH_SHORT).show();
                    feedbackCommentInput.setText("");
                    feedbackScoreInput.setRating(1);
                    loadFeedbacks();
                });
    }

}
