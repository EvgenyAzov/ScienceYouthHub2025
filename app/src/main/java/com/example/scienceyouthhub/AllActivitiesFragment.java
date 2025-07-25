package com.example.scienceyouthhub;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.firestore.*;
import java.util.*;

public class AllActivitiesFragment extends Fragment {
    private RecyclerView recyclerView;
    private AllActivitiesAdapter adapter;
    private List<ActivityModel> allActivities = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_activities, container, false);

        recyclerView = view.findViewById(R.id.allActivitiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AllActivitiesAdapter(allActivities);
        recyclerView.setAdapter(adapter);

        loadApprovedActivities();

        return view;
    }

    private void loadApprovedActivities() {
        FirebaseFirestore.getInstance().collection("activities")
                .whereEqualTo("approvedByAdmin", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    allActivities.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        ActivityModel act = doc.toObject(ActivityModel.class);
                        allActivities.add(act);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
