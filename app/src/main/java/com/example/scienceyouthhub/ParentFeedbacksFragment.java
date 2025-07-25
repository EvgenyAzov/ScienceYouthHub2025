package com.example.scienceyouthhub;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import java.util.*;

public class ParentFeedbacksFragment extends Fragment {
    private Spinner activitySpinner;
    private RecyclerView feedbacksRecyclerView;
    private ParentFeedbacksAdapter feedbacksAdapter;
    private EditText feedbackEditText, ratingEditText;
    private Button sendFeedbackBtn;

    private List<ActivityModel> childActivities = new ArrayList<>();
    private List<FeedbackModel> feedbacks = new ArrayList<>();

    private String parentId;
    private String parentName;
    private String childId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_feedbacks, container, false);

        activitySpinner = view.findViewById(R.id.parentActivitySpinner);
        feedbacksRecyclerView = view.findViewById(R.id.parentFeedbacksRecyclerView);
        feedbackEditText = view.findViewById(R.id.parentFeedbackEditText);
        ratingEditText = view.findViewById(R.id.parentRatingEditText);
        sendFeedbackBtn = view.findViewById(R.id.parentSendFeedbackBtn);

        feedbacksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedbacksAdapter = new ParentFeedbacksAdapter(feedbacks);
        feedbacksRecyclerView.setAdapter(feedbacksAdapter);

        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        parentName = getActivity().getSharedPreferences("user_prefs", 0).getString("user_name", "Родитель");
        childId = getActivity().getSharedPreferences("user_prefs", 0).getString("child_id", null);
        // Если несколько детей — реализуй выбор через ещё один Spinner

        loadChildActivities();

        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                loadFeedbacks();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        sendFeedbackBtn.setOnClickListener(v -> sendFeedback());

        return view;
    }

    private void loadChildActivities() {
        FirebaseFirestore.getInstance().collection("activities")
                .whereArrayContains("participants", childId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    childActivities.clear();
                    List<String> names = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        ActivityModel act = doc.toObject(ActivityModel.class);
                        childActivities.add(act);
                        names.add(act.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_dropdown_item, names);
                    activitySpinner.setAdapter(adapter);
                    if (!childActivities.isEmpty())
                        loadFeedbacks();
                });
    }

    private void loadFeedbacks() {
        int pos = activitySpinner.getSelectedItemPosition();
        if (pos < 0 || childActivities.isEmpty()) {
            feedbacks.clear();
            feedbacksAdapter.notifyDataSetChanged();
            return;
        }
        String activityId = childActivities.get(pos).getId();
        FirebaseFirestore.getInstance().collection("feedbacks")
                .whereEqualTo("activityId", activityId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    feedbacks.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        FeedbackModel f = doc.toObject(FeedbackModel.class);
                        feedbacks.add(f);
                    }
                    feedbacksAdapter.notifyDataSetChanged();
                });
    }

    private void sendFeedback() {
        int pos = activitySpinner.getSelectedItemPosition();
        if (pos < 0 || childActivities.isEmpty()) return;
        String activityId = childActivities.get(pos).getId();
        String activityName = childActivities.get(pos).getName();

        String comment = feedbackEditText.getText().toString().trim();
        String ratingStr = ratingEditText.getText().toString().trim();
        if (TextUtils.isEmpty(comment) || TextUtils.isEmpty(ratingStr)) {
            Toast.makeText(getContext(), "Введите комментарий и оценку!", Toast.LENGTH_SHORT).show();
            return;
        }
        int rating = Integer.parseInt(ratingStr);
        if (rating < 1 || rating > 10) {
            Toast.makeText(getContext(), "Оценка от 1 до 10", Toast.LENGTH_SHORT).show();
            return;
        }

        FeedbackModel feedback = new FeedbackModel();
        feedback.setId(UUID.randomUUID().toString());
        feedback.setActivityId(activityId);
        feedback.setActivityName(activityName);
        feedback.setAuthorId(parentId);
        feedback.setAuthorName(parentName);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setDate(new Date());

        FirebaseFirestore.getInstance().collection("feedbacks")
                .document(feedback.getId())
                .set(feedback)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Отзыв отправлен!", Toast.LENGTH_SHORT).show();
                    feedbackEditText.setText("");
                    ratingEditText.setText("");
                    loadFeedbacks();
                });
    }
}
