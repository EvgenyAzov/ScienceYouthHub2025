package com.example.scienceyouthhub;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.*;

import java.util.*;

public class ActivitiesFragment extends Fragment {

    private RecyclerView activitiesRecyclerView;
    private ActivityAdapter activityAdapter;
    private List<ActivityModel> allActivities = new ArrayList<>();
    private List<ActivityModel> filteredActivities = new ArrayList<>();
    private Spinner spinnerArea, spinnerMonth, spinnerInstructor;
    private FloatingActionButton addActivityFab;

    private String userRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false);

        activitiesRecyclerView = view.findViewById(R.id.activitiesRecyclerView);
        spinnerArea = view.findViewById(R.id.spinnerArea);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerInstructor = view.findViewById(R.id.spinnerInstructor);
        addActivityFab = view.findViewById(R.id.addActivityFab);

        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityAdapter = new ActivityAdapter(filteredActivities, getContext(), new ActivityAdapter.OnActivityActionListener() {
            @Override
            public void onEdit(ActivityModel activity) {
                // your edit logic here
            }
            @Override
            public void onDelete(ActivityModel activity) {
                // your delete logic here
            }
        });
        activitiesRecyclerView.setAdapter(activityAdapter);

        setupSpinners();
        loadActivities();

        return view;
    }

    private void setupSpinners() {
        // For example — put needed values for area and months
        final List<String> areas = Arrays.asList("All areas", "SCIENCE", "ART", "SOCIAL");
        final List<String> months = Arrays.asList("All months", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, areas);
        spinnerArea.setAdapter(areaAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, months);
        spinnerMonth.setAdapter(monthAdapter);

        // Load instructors from Firestore or statically (example below)
        List<String> instructors = new ArrayList<>();
        instructors.add("All instructors");

        // (Additionally — load instructors list from Firestore)
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("type", "Instructor")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        if (!TextUtils.isEmpty(name)) {
                            instructors.add(name);
                        }
                    }
                    ArrayAdapter<String> instrAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, instructors);
                    spinnerInstructor.setAdapter(instrAdapter);
                });

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndUpdate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterAndUpdate();
            }
        };
        spinnerArea.setOnItemSelectedListener(filterListener);
        spinnerMonth.setOnItemSelectedListener(filterListener);
        spinnerInstructor.setOnItemSelectedListener(filterListener);
    }

    private String safeGetSpinnerString(Spinner spinner) {
        Object obj = spinner.getSelectedItem();
        return obj != null ? obj.toString() : "";
    }

    private void filterAndUpdate() {
        String selectedArea = safeGetSpinnerString(spinnerArea);
        String selectedMonth = safeGetSpinnerString(spinnerMonth);
        String selectedInstructor = safeGetSpinnerString(spinnerInstructor);

        filteredActivities.clear();

        for (ActivityModel activity : allActivities) {
            boolean match = true;

            // Filter by area (category)
            if (!TextUtils.isEmpty(selectedArea)
                    && !selectedArea.equals("All areas")
                    && (activity.getCategory() == null || !activity.getCategory().equals(selectedArea))) {
                match = false;
            }

            // Filter by month — depends on your model! (e.g., activity.getDays())
            if (!TextUtils.isEmpty(selectedMonth)
                    && !selectedMonth.equals("All months")
                    && (activity.getDays() == null || !activity.getDays().toLowerCase().contains(selectedMonth.toLowerCase()))) {
                match = false;
            }

            // Filter by instructor
            if (!TextUtils.isEmpty(selectedInstructor)
                    && !selectedInstructor.equals("All instructors")
                    && (activity.getInstructorName() == null || !activity.getInstructorName().equals(selectedInstructor))) {
                match = false;
            }

            if (match) filteredActivities.add(activity);
        }
        activityAdapter.notifyDataSetChanged();
    }

    private void loadActivities() {
        FirebaseFirestore.getInstance().collection("activities")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allActivities.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ActivityModel activity = doc.toObject(ActivityModel.class);
                        allActivities.add(activity);
                    }
                    filterAndUpdate();
                });
    }
}
