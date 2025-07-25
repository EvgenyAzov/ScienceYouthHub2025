package com.example.scienceyouthhub;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.firestore.*;
import java.util.*;

public class AdminActivitiesWithStatusFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminActivitiesWithStatusAdapter adapter;
    private List<ActivityModel> allActivities = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_activities_status, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminActivitiesWithStatusAdapter(allActivities);
        recyclerView.setAdapter(adapter);

        loadAllActivities();

        return view;
    }

    private void loadAllActivities() {
        FirebaseFirestore.getInstance().collection("activities")
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
