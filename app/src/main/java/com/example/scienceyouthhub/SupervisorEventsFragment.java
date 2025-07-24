package com.example.scienceyouthhub;

import android.app.DatePickerDialog;
import android.graphics.Color;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class SupervisorEventsFragment extends Fragment {

    private Spinner spinnerArea, spinnerMonth, spinnerInstructor;
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private FloatingActionButton fabAdd;

    private List<ActivityModel> allEvents = new ArrayList<>();
    private List<ActivityModel> filteredEvents = new ArrayList<>();

    // Категории и подкатегории
    private final Map<String, List<String>> categoryMap = new HashMap<String, List<String>>() {{
        put("Science", Arrays.asList("Biology", "Robotics", "Physics", "Math"));
        put("Social", Arrays.asList("Leadership", "Public Speaking", "Collaboration"));
        put("Art", Arrays.asList("Art", "Writing", "Music"));
    }};
    private List<String> areas = new ArrayList<>(Arrays.asList("All areas", "Science", "Social", "Art"));
    private List<String> months = Arrays.asList("All months", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December");
    private List<UserModel> instructors = new ArrayList<>();
    private List<String> instructorNames = new ArrayList<>();

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

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, areas);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArea.setAdapter(areaAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        adapter = new EventsAdapter(filteredEvents, this::showEditEventDialog, this::deleteEvent);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(vv -> showAddEventDialog());

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
                    instructorNames.clear();
                    instructorNames.add("All instructors");
                    for (DocumentSnapshot doc : querySnapshot) {
                        UserModel instructor = doc.toObject(UserModel.class);
                        if (instructor != null) {
                            instructors.add(instructor);
                            instructorNames.add(instructor.getName());
                        }
                    }
                    ArrayAdapter<String> instructorAdapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, instructorNames);
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
            // Фильтр по категории
            if (!"All areas".equals(selectedArea) && (event.getCategory() == null || !event.getCategory().equalsIgnoreCase(selectedArea)))
                continue;

            // Фильтр по месяцу (startDate)
            if (!"All months".equals(selectedMonth)) {
                if (event.getStartDate() == null) continue;
                Calendar cal = Calendar.getInstance();
                cal.setTime(event.getStartDate());
                String eventMonth = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(cal.getTime());
                if (!eventMonth.equalsIgnoreCase(selectedMonth))
                    continue;
            }

            // Фильтр по инструктору
            if (!"All instructors".equals(selectedInstructor) && !isEventHasInstructorName(event, selectedInstructor))
                continue;

            filteredEvents.add(event);
        }

        adapter.notifyDataSetChanged();
    }

    private boolean isEventHasInstructorName(ActivityModel event, String instructorName) {
        if (event.getInstructorId() == null || event.getInstructorId().isEmpty()) return false;
        for (UserModel instructor : instructors) {
            if (instructor.getName().equalsIgnoreCase(instructorName) && instructor.getId().equals(event.getInstructorId())) {
                return true;
            }
        }
        return false;
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
        Spinner categorySpinner = dialogView.findViewById(R.id.dialogActivityCategory);
        Spinner subcategorySpinner = dialogView.findViewById(R.id.dialogActivitySubcategory);
        EditText ageRangeInput = dialogView.findViewById(R.id.dialogActivityAgeRange);
        EditText descriptionInput = dialogView.findViewById(R.id.dialogActivityDescription);
        EditText maxParticipantsInput = dialogView.findViewById(R.id.dialogActivityMaxParticipants);
        Spinner instructorSpinner = dialogView.findViewById(R.id.dialogActivityInstructor);
        CheckBox specialEventCheckbox = dialogView.findViewById(R.id.specialEventCheck);

        // Кнопки для выбора дат
        Button startDateBtn = dialogView.findViewById(R.id.startDateBtn);
        Button endDateBtn = dialogView.findViewById(R.id.endDateBtn);
        final Date[] startDate = {null};
        final Date[] endDate = {null};

        // Чекбоксы дней недели
        String[] weekDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] weekCheckIds = {
                R.id.checkboxMon, R.id.checkboxTue, R.id.checkboxWed, R.id.checkboxThu,
                R.id.checkboxFri, R.id.checkboxSat, R.id.checkboxSun
        };
        List<CheckBox> dayCheckBoxes = new ArrayList<>();
        for (int id : weekCheckIds) {
            dayCheckBoxes.add(dialogView.findViewById(id));
        }

        // Категории
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(categoryMap.keySet()));
        categorySpinner.setAdapter(categoryAdapter);

        // Подкатегории
        ArrayAdapter<String> subcategoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        subcategorySpinner.setAdapter(subcategoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String cat = (String) categorySpinner.getSelectedItem();
                List<String> subs = categoryMap.getOrDefault(cat, new ArrayList<>());
                subcategoryAdapter.clear();
                subcategoryAdapter.addAll(subs);
                subcategoryAdapter.notifyDataSetChanged();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Инструкторы
        List<String> instructorNamesForDialog = new ArrayList<>();
        instructorNamesForDialog.add("None");
        for (UserModel instructor : instructors) {
            instructorNamesForDialog.add(instructor.getName());
        }
        ArrayAdapter<String> instructorAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, instructorNamesForDialog);
        instructorSpinner.setAdapter(instructorAdapter);

        // --- Даты через DatePicker ---
        startDateBtn.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (startDate[0] != null) cal.setTime(startDate[0]);
            new DatePickerDialog(getContext(), (view, y, m, d) -> {
                cal.set(y, m, d);
                startDate[0] = cal.getTime();
                startDateBtn.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(startDate[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        endDateBtn.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (endDate[0] != null) cal.setTime(endDate[0]);
            new DatePickerDialog(getContext(), (view, y, m, d) -> {
                cal.set(y, m, d);
                endDate[0] = cal.getTime();
                endDateBtn.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(endDate[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // --- Если редактируем, подставить значения ---
        if (event != null) {
            nameInput.setText(event.getName());
            categorySpinner.setSelection(new ArrayList<>(categoryMap.keySet()).indexOf(event.getCategory()));
            List<String> subs = categoryMap.get(event.getCategory());
            if (subs != null) {
                subcategoryAdapter.clear();
                subcategoryAdapter.addAll(subs);
                subcategoryAdapter.notifyDataSetChanged();
                subcategorySpinner.setSelection(subs.indexOf(event.getSubcategory()));
            }
            ageRangeInput.setText(event.getAgeRange());
            descriptionInput.setText(event.getDescription());
            maxParticipantsInput.setText(String.valueOf(event.getMaxParticipants()));

            // Дни недели
            if (event.getDaysOfWeek() != null) {
                for (int i = 0; i < weekDays.length; i++) {
                    dayCheckBoxes.get(i).setChecked(event.getDaysOfWeek().contains(weekDays[i]));
                }
            }
            // Даты
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            if (event.getStartDate() != null) {
                startDate[0] = event.getStartDate();
                startDateBtn.setText(sdf.format(event.getStartDate()));
            }
            if (event.getEndDate() != null) {
                endDate[0] = event.getEndDate();
                endDateBtn.setText(sdf.format(event.getEndDate()));
            }
            // Инструктор
            if (event.getInstructorId() != null && !event.getInstructorId().isEmpty()) {
                for (int i = 1; i < instructorNamesForDialog.size(); i++) {
                    UserModel instructor = instructors.get(i - 1);
                    if (instructor.getId().equals(event.getInstructorId())) {
                        instructorSpinner.setSelection(i);
                        break;
                    }
                }
            } else {
                instructorSpinner.setSelection(0);
            }
            // Спец. событие
            specialEventCheckbox.setChecked(!event.isApprovedByAdmin());
        }

        // --- AlertDialog ---
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(event == null ? "Add Event" : "Edit Event")
                .setView(dialogView)
                .setPositiveButton(event == null ? "Add" : "Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String category = (String) categorySpinner.getSelectedItem();
                    String subcategory = (String) subcategorySpinner.getSelectedItem();
                    String ageRange = ageRangeInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    List<String> daysOfWeek = new ArrayList<>();
                    for (int i = 0; i < weekDays.length; i++) {
                        if (dayCheckBoxes.get(i).isChecked()) daysOfWeek.add(weekDays[i]);
                    }
                    int maxParticipants = 0;
                    try {
                        maxParticipants = Integer.parseInt(maxParticipantsInput.getText().toString().trim());
                    } catch (Exception e) {}
                    String selectedInstructorName = (String) instructorSpinner.getSelectedItem();
                    String selectedInstructorId = null;
                    if (!"None".equals(selectedInstructorName)) {
                        for (UserModel instructor : instructors) {
                            if (instructor.getName().equals(selectedInstructorName)) {
                                selectedInstructorId = instructor.getId();
                                break;
                            }
                        }
                    }
                    boolean approvedByAdmin = !specialEventCheckbox.isChecked();
                    // Проверка
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category) || TextUtils.isEmpty(ageRange) ||
                            TextUtils.isEmpty(description) || daysOfWeek.isEmpty() || maxParticipants <= 0 ||
                            startDate[0] == null || endDate[0] == null) {
                        Toast.makeText(getContext(), "Fill in all fields and dates", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (event == null) {
                        ActivityModel newEvent = new ActivityModel(
                                UUID.randomUUID().toString(), name, category, subcategory, ageRange,
                                description, daysOfWeek, startDate[0], endDate[0], maxParticipants,
                                selectedInstructorId, selectedInstructorName, approvedByAdmin
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
                        event.setSubcategory(subcategory);
                        event.setAgeRange(ageRange);
                        event.setDescription(description);
                        event.setDaysOfWeek(daysOfWeek);
                        event.setStartDate(startDate[0]);
                        event.setEndDate(endDate[0]);
                        event.setMaxParticipants(maxParticipants);
                        event.setInstructorId(selectedInstructorId);
                        event.setInstructorName(selectedInstructorName);
                        event.setApprovedByAdmin(approvedByAdmin);
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
            holder.subcategory.setText("Subcategory: " + event.getSubcategory());
            holder.ageRange.setText("Age range: " + event.getAgeRange());
            holder.instructor.setText("Instructor: " + (event.getInstructorName() == null ? "None" : event.getInstructorName()));

            StringBuilder daysText = new StringBuilder("Days: ");
            if (event.getDaysOfWeek() != null)
                for (String d : event.getDaysOfWeek()) daysText.append(d).append(" ");
            holder.days.setText(daysText.toString());

            holder.dates.setText("From: " +
                    (event.getStartDate() == null ? "-" : new SimpleDateFormat("yyyy-MM-dd").format(event.getStartDate()))
                    + " To: " +
                    (event.getEndDate() == null ? "-" : new SimpleDateFormat("yyyy-MM-dd").format(event.getEndDate()))
            );

            // Выделение special event
            if (!event.isApprovedByAdmin()) {
                holder.itemView.setBackgroundColor(Color.parseColor("#FFF8E1"));
                holder.editBtn.setEnabled(false);
                holder.deleteBtn.setEnabled(false);
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE);
                holder.editBtn.setEnabled(true);
                holder.deleteBtn.setEnabled(true);
            }

            holder.editBtn.setOnClickListener(v -> editListener.onEdit(event));
            holder.deleteBtn.setOnClickListener(v -> deleteListener.onDelete(event));
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        static class EventViewHolder extends RecyclerView.ViewHolder {
            TextView name, category, subcategory, ageRange, instructor, days, dates;
            ImageButton editBtn, deleteBtn;

            EventViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.activityNameText);
                category = itemView.findViewById(R.id.activityCategoryText);
                subcategory = itemView.findViewById(R.id.activitySubcategoryText); // добавь в xml!
                ageRange = itemView.findViewById(R.id.activityAgeRangeText);
                instructor = itemView.findViewById(R.id.activityInstructorText);
                days = itemView.findViewById(R.id.activityDaysText); // добавь в xml!
                dates = itemView.findViewById(R.id.activityDatesText); // добавь в xml!
                editBtn = itemView.findViewById(R.id.editActivityBtn);
                deleteBtn = itemView.findViewById(R.id.deleteActivityBtn);
            }
        }
    }
}
