package com.example.scienceyouthhub;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import android.widget.TextView;
import com.google.firebase.firestore.*;
import java.util.*;

public class StudentScheduleFragment extends Fragment {
    private RecyclerView scheduleRecyclerView;
    private StudentScheduleAdapter adapter;
    private List<ActivityModel> activities = new ArrayList<>();
    private UserModel student;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_schedule, container, false);

        scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StudentScheduleAdapter(activities);
        scheduleRecyclerView.setAdapter(adapter);

        // Подгружаем профиль студента и его расписание
        loadStudentAndSchedule();

        return view;
    }

    private void loadStudentAndSchedule() {
        String studentId = requireContext().getSharedPreferences("user_prefs", 0).getString("user_id", "");
        FirebaseFirestore.getInstance().collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener(doc -> {
                    student = doc.toObject(UserModel.class);
                    if (student == null) return;
                    loadActivities(student.getMyActivities());
                });
    }

    private void loadActivities(List<String> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            activities.clear();
            adapter.notifyDataSetChanged();
            return;
        }
        FirebaseFirestore.getInstance().collection("activities")
                .whereIn("id", activityIds)
                .get()
                .addOnSuccessListener(snapshot -> {
                    activities.clear();
                    Set<String> added = new HashSet<>();
                    for (DocumentSnapshot doc : snapshot) {
                        ActivityModel act = doc.toObject(ActivityModel.class);
                        // Гарантия уникальности
                        if (act != null && !added.contains(act.getId())) {
                            activities.add(act);
                            added.add(act.getId());
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
