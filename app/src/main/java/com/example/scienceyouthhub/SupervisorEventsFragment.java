package com.example.scienceyouthhub;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.*;

import java.util.*;

public class SupervisorEventsFragment extends Fragment {

    private Spinner spinnerArea, spinnerMonth, spinnerInstructor;
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private FloatingActionButton fabAdd;

    private List<ActivityModel> allEvents = new ArrayList<>();
    private List<ActivityModel> filteredEvents = new ArrayList<>();

    private List<String> areas = Arrays.asList("All areas", "Science", "Arts", "Sports");
    private List<String> months = Arrays.asList("All months", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December");
    private List<String> instructors = new ArrayList<>();

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_supervisor_events, container, false);

        spinnerArea = v.findViewById(R.id.spinnerArea);
        spinnerMonth = v.findViewById(R.id.spinnerMonth);
        spinnerInstructor = v.findViewById(R.id.spinnerInstructor);
        recyclerView = v.findViewById(R.id.recyclerViewEvents);
        fabAdd = v.findViewById(R.id.fabAddEvent);

        db = FirebaseFirestore.getInstance();

        // Setup spinners
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, areas);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArea.setAdapter(areaAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Instructor spinner will be loaded from DB
        adapter = new EventsAdapter(filteredEvents, this::showEditEventDialog, this::deleteEvent);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(vv -> showAddEventDialog());

        // Filter listeners
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndUpdate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerArea.setOnItemSelectedListener(filterListener);
        spinnerMonth.setOnItemSelectedListener(filterListener);
        spinnerInstructor.setOnItemSelectedListener(filterListener);

        loadInstructors();
        loadEvents();

        return v;
    }

    private void loadInstructors() {
        db.collection("users")
                .whereEqualTo("type", "Instructor")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    instructors.clear();
                    instructors.add("All instructors");
                    for (DocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        if (name != null) {
                            instructors.add(name);
                        }
                    }
                    ArrayAdapter<String> instructorAdapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, instructors);
                    instructorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerInstructor.setAdapter(instructorAdapter);
                });
    }

    private void loadEvents() {
        db.collection("activities")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allEvents.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ActivityModel event = doc.toObject(ActivityModel.class);
                        if (event != null) {
                            allEvents.add(event);
                        }
                    }
                    filterAndUpdate();
                });
    }

    private void filterAndUpdate() {
        String selectedArea = (String) spinnerArea.getSelectedItem();
        String selectedMonth = (String) spinnerMonth.getSelectedItem();
        String selectedInstructor = (String) spinnerInstructor.getSelectedItem();

        filteredEvents.clear();

        for (ActivityModel event : allEvents) {
            if (!"All areas".equals(selectedArea) && (event.getCategory() == null || !event.getCategory().equalsIgnoreCase(selectedArea)))
                continue;

            if (!"All months".equals(selectedMonth) && (event.getDays() == null || !event.getDays().toLowerCase().contains(selectedMonth.toLowerCase())))
                continue;

            if (!"All instructors".equals(selectedInstructor) && (event.getInstructorName() == null || !event.getInstructorName().equalsIgnoreCase(selectedInstructor)))
                continue;

            filteredEvents.add(event);
        }

        adapter.notifyDataSetChanged();
    }

    private void showAddEventDialog() {
        showEventDialog(null);
    }

    private void showEditEventDialog(ActivityModel event) {
        showEventDialog(event);
    }

    private void showEventDialog(ActivityModel event) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_activity, null);

        EditText nameInput = dialogView.findViewById(R.id.dialogActivityName);
        EditText categoryInput = dialogView.findViewById(R.id.dialogActivityCategory);
        EditText ageRangeInput = dialogView.findViewById(R.id.dialogActivityAgeRange);
        EditText descriptionInput = dialogView.findViewById(R.id.dialogActivityDescription);
        EditText daysInput = dialogView.findViewById(R.id.dialogActivityDays);
        EditText maxParticipantsInput = dialogView.findViewById(R.id.dialogActivityMaxParticipants);
        Spinner instructorSpinner = dialogView.findViewById(R.id.dialogActivityInstructor);

        // Fill instructor spinner
        ArrayAdapter<String> instructorAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, instructors);
        instructorSpinner.setAdapter(instructorAdapter);

        if (event != null) {
            nameInput.setText(event.getName());
            categoryInput.setText(event.getCategory());
            ageRangeInput.setText(event.getAgeRange());
            descriptionInput.setText(event.getDescription());
            daysInput.setText(event.getDays());
            maxParticipantsInput.setText(String.valueOf(event.getMaxParticipants()));

            int index = instructors.indexOf(event.getInstructorName());
            if (index >= 0) {
                instructorSpinner.setSelection(index);
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(event == null ? "Add Event" : "Edit Event")
                .setView(dialogView)
                .setPositiveButton(event == null ? "Add" : "Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String category = categoryInput.getText().toString().trim();
                    String ageRange = ageRangeInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    String days = daysInput.getText().toString().trim();
                    String maxPartStr = maxParticipantsInput.getText().toString().trim();
                    String instructorName = (String) instructorSpinner.getSelectedItem();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category) || TextUtils.isEmpty(ageRange) ||
                            TextUtils.isEmpty(description) || TextUtils.isEmpty(days) || TextUtils.isEmpty(maxPartStr) ||
                            TextUtils.isEmpty(instructorName)) {
                        Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int maxParticipants;
                    try {
                        maxParticipants = Integer.parseInt(maxPartStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Max participants must be a number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (event == null) {
                        ActivityModel newEvent = new ActivityModel(
                                UUID.randomUUID().toString(), name, category, ageRange,
                                description, days, maxParticipants, null, instructorName
                        );
                        db.collection("activities").document(newEvent.getId())
                                .set(newEvent)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Event added", Toast.LENGTH_SHORT).show();
                                    loadEvents();
                                });
                    } else {
                        event.setName(name);
                        event.setCategory(category);
                        event.setAgeRange(ageRange);
                        event.setDescription(description);
                        event.setDays(days);
                        event.setMaxParticipants(maxParticipants);
                        event.setInstructorName(instructorName);
                        db.collection("activities").document(event.getId())
                                .set(event)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Event updated", Toast.LENGTH_SHORT).show();
                                    loadEvents();
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(ActivityModel event) {
        db.collection("activities").document(event.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    loadEvents();
                });
    }

    // --- Adapter ---

    private static class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

        interface OnEditListener { void onEdit(ActivityModel event); }
        interface OnDeleteListener { void onDelete(ActivityModel event); }

        private final List<ActivityModel> events;
        private final OnEditListener editListener;
        private final OnDeleteListener deleteListener;

        EventsAdapter(List<ActivityModel> events, OnEditListener editListener, OnDeleteListener deleteListener) {
            this.events = events;
            this.editListener = editListener;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity, parent, false);
            return new EventViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            ActivityModel event = events.get(position);
            holder.name.setText(event.getName());
            holder.category.setText("Area: " + event.getCategory());
            holder.ageRange.setText("Age range: " + event.getAgeRange());
            holder.instructor.setText("Instructor: " + event.getInstructorName());

            holder.editBtn.setOnClickListener(v -> editListener.onEdit(event));
            holder.deleteBtn.setOnClickListener(v -> deleteListener.onDelete(event));
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        static class EventViewHolder extends RecyclerView.ViewHolder {
            TextView name, category, ageRange, instructor;
            ImageButton editBtn, deleteBtn;

            EventViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.activityNameText);
                category = itemView.findViewById(R.id.activityCategoryText);
                ageRange = itemView.findViewById(R.id.activityAgeRangeText);
                instructor = itemView.findViewById(R.id.activityInstructorText);
                editBtn = itemView.findViewById(R.id.editActivityBtn);
                deleteBtn = itemView.findViewById(R.id.deleteActivityBtn);
            }
        }
    }
}
