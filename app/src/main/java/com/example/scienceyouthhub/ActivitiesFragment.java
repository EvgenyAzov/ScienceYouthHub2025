package com.example.scienceyouthhub;

import android.app.DatePickerDialog;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class ActivitiesFragment extends Fragment {

    private RecyclerView activitiesRecyclerView;
    private ActivityAdapter activityAdapter;
    private List<ActivityModel> allActivities = new ArrayList<>();
    private List<ActivityModel> filteredActivities = new ArrayList<>();
    private Spinner spinnerArea, spinnerMonth, spinnerInstructor;
    private FloatingActionButton addActivityFab;
    private String userRole;

    // Категории и подкатегории для спиннеров
    private final Map<String, List<String>> subcategoriesMap = new HashMap<String, List<String>>() {{
        put("Science", Arrays.asList("Biology", "Robotics", "Physics", "Mathematics"));
        put("Social", Arrays.asList("Leadership", "Public Speaking", "Collaboration"));
        put("Art", Arrays.asList("Art", "Writing", "Music"));
    }};

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
        activityAdapter = new ActivityAdapter(filteredActivities, new ActivityAdapter.OnActionListener() {
            @Override
            public void onEdit(ActivityModel activity) {
                showActivityDialog(activity);
            }
            @Override
            public void onDelete(ActivityModel activity) {
                deleteActivity(activity);
            }
        });
        activitiesRecyclerView.setAdapter(activityAdapter);

        setupSpinners();
        loadActivities();

        // Добавление
        addActivityFab.setOnClickListener(v -> showActivityDialog(null));

        return view;
    }

    private void setupSpinners() {
        final List<String> areas = Arrays.asList("All areas", "Science", "Art", "Social");
        final List<String> months = Arrays.asList("All months", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, areas);
        spinnerArea.setAdapter(areaAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, months);
        spinnerMonth.setAdapter(monthAdapter);

        List<String> instructors = new ArrayList<>();
        instructors.add("All instructors");

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
            // Фильтр по категории
            if (!TextUtils.isEmpty(selectedArea)
                    && !selectedArea.equals("All areas")
                    && (activity.getCategory() == null || !activity.getCategory().equals(selectedArea))) {
                match = false;
            }
            // Фильтр по месяцу (по дате старта)
            if (!TextUtils.isEmpty(selectedMonth)
                    && !selectedMonth.equals("All months")
                    && (activity.getStartDate() == null || !getMonthName(activity.getStartDate()).equals(selectedMonth))) {
                match = false;
            }
            // Фильтр по инструктору
            if (!TextUtils.isEmpty(selectedInstructor)
                    && !selectedInstructor.equals("All instructors")
                    && (activity.getInstructorName() == null || !activity.getInstructorName().equals(selectedInstructor))) {
                match = false;
            }
            if (match) filteredActivities.add(activity);
        }
        activityAdapter.notifyDataSetChanged();
    }

    private String getMonthName(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", Locale.ENGLISH);
        return sdf.format(date);
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

    // Диалог добавления/редактирования активности
    private void showActivityDialog(@Nullable ActivityModel activity) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_activity, null);

        EditText nameInput = dialogView.findViewById(R.id.dialogActivityName);
        Spinner categorySpinner = dialogView.findViewById(R.id.dialogActivityCategory);
        Spinner subcategorySpinner = dialogView.findViewById(R.id.dialogActivitySubcategory);
        EditText ageRangeInput = dialogView.findViewById(R.id.dialogActivityAgeRange);
        EditText descriptionInput = dialogView.findViewById(R.id.dialogActivityDescription);
        EditText maxParticipantsInput = dialogView.findViewById(R.id.dialogActivityMaxParticipants);

        // === ВАЖНО: вот тут исправляем под твой layout ===
        Button startDateBtn = dialogView.findViewById(R.id.startDateBtn);
        Button endDateBtn = dialogView.findViewById(R.id.endDateBtn);
        CheckBox specialEventCheckbox = dialogView.findViewById(R.id.specialEventCheck);
        // ================================================

        // Для хранения выбранных дат:
        final Date[] startDate = {null};
        final Date[] endDate = {null};

        // Подгружаем категории
        List<String> categories = new ArrayList<>(subcategoriesMap.keySet());
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(catAdapter);

        // Динамически подгружаем подкатегории
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCat = (String) categorySpinner.getSelectedItem();
                List<String> subs = subcategoriesMap.getOrDefault(selectedCat, Collections.singletonList("None"));
                ArrayAdapter<String> subAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, subs);
                subcategorySpinner.setAdapter(subAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // При редактировании — заполняем поля
        if (activity != null) {
            nameInput.setText(activity.getName());
            categorySpinner.setSelection(categories.indexOf(activity.getCategory()));
            List<String> subs = subcategoriesMap.get(activity.getCategory());
            if (subs != null) {
                subcategorySpinner.setSelection(subs.indexOf(activity.getSubcategory()));
            }
            ageRangeInput.setText(activity.getAgeRange());
            descriptionInput.setText(activity.getDescription());
            maxParticipantsInput.setText(String.valueOf(activity.getMaxParticipants()));
            // Даты
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            if (activity.getStartDate() != null) {
                startDate[0] = activity.getStartDate();
                startDateBtn.setText(sdf.format(activity.getStartDate()));
            }
            if (activity.getEndDate() != null) {
                endDate[0] = activity.getEndDate();
                endDateBtn.setText(sdf.format(activity.getEndDate()));
            }
            specialEventCheckbox.setChecked(!activity.isApprovedByAdmin());
        }

        // Пикер даты для даты старта
        startDateBtn.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (startDate[0] != null) cal.setTime(startDate[0]);
            new DatePickerDialog(getContext(), (view, year, month, day) -> {
                cal.set(year, month, day);
                startDate[0] = cal.getTime();
                startDateBtn.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(startDate[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        endDateBtn.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (endDate[0] != null) cal.setTime(endDate[0]);
            new DatePickerDialog(getContext(), (view, year, month, day) -> {
                cal.set(year, month, day);
                endDate[0] = cal.getTime();
                endDateBtn.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(endDate[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(activity == null ? "Add Activity" : "Edit Activity")
                .setView(dialogView)
                .setPositiveButton(activity == null ? "Add" : "Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String category = categorySpinner.getSelectedItem().toString();
                    String subcategory = subcategorySpinner.getSelectedItem().toString();
                    String ageRange = ageRangeInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    String maxPartStr = maxParticipantsInput.getText().toString().trim();
                    boolean isSpecial = specialEventCheckbox.isChecked();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category) || TextUtils.isEmpty(subcategory)
                            || TextUtils.isEmpty(ageRange) || TextUtils.isEmpty(description)
                            || TextUtils.isEmpty(maxPartStr) || startDate[0] == null || endDate[0] == null) {
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

                    boolean approvedByAdmin = !isSpecial;

                    // Сохраняем
                    if (activity == null) {
                        ActivityModel newActivity = new ActivityModel(
                                UUID.randomUUID().toString(), name, category, subcategory, ageRange,
                                description, null, startDate[0], endDate[0], maxParticipants, null, null, approvedByAdmin
                        );
                        FirebaseFirestore.getInstance().collection("activities")
                                .document(newActivity.getId())
                                .set(newActivity)
                                .addOnSuccessListener(aVoid -> {
                                    loadActivities();
                                    Toast.makeText(getContext(), "Activity added", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        activity.setName(name);
                        activity.setCategory(category);
                        activity.setSubcategory(subcategory);
                        activity.setAgeRange(ageRange);
                        activity.setDescription(description);
                        activity.setMaxParticipants(maxParticipants);
                        activity.setStartDate(startDate[0]);
                        activity.setEndDate(endDate[0]);
                        activity.setApprovedByAdmin(approvedByAdmin);
                        FirebaseFirestore.getInstance().collection("activities")
                                .document(activity.getId())
                                .set(activity)
                                .addOnSuccessListener(aVoid -> {
                                    loadActivities();
                                    Toast.makeText(getContext(), "Activity updated", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteActivity(ActivityModel activity) {
        FirebaseFirestore.getInstance().collection("activities")
                .document(activity.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    loadActivities();
                    Toast.makeText(getContext(), "Activity deleted", Toast.LENGTH_SHORT).show();
                });
    }
}
