package com.example.scienceyouthhub;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.firestore.*;
import java.util.*;

public class AdminPendingActivitiesFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminPendingActivitiesAdapter adapter;
    private List<ActivityModel> pendingActivities = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_pending_activities, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminPendingActivitiesAdapter(pendingActivities);
        recyclerView.setAdapter(adapter);

        loadPendingActivities();

        return view;
    }

    private void loadPendingActivities() {
        FirebaseFirestore.getInstance().collection("activities")
                .whereEqualTo("approvedByAdmin", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    pendingActivities.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        ActivityModel act = doc.toObject(ActivityModel.class);
                        pendingActivities.add(act);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
