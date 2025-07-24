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
import java.util.*;

public class InstructorsFragment extends Fragment {

    private RecyclerView instructorsRecyclerView;
    private InstructorAdapter instructorAdapter;
    private List<UserModel> allInstructors = new ArrayList<>();
    private List<UserModel> filteredInstructors = new ArrayList<>();
    private Spinner categorySpinner, subcategorySpinner;

    // Категории и подкатегории как в registration
    private final Map<String, List<String>> categoryMap = new HashMap<String, List<String>>() {{
        put("Science", Arrays.asList("Biology", "Robotics", "Physics", "Math"));
        put("Social", Arrays.asList("Leadership", "Public Speaking", "Collaboration"));
        put("Art", Arrays.asList("Art", "Writing", "Music"));
    }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instructors, container, false);

        instructorsRecyclerView = view.findViewById(R.id.instructorsRecyclerView);
        instructorsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        instructorAdapter = new InstructorAdapter(filteredInstructors);
        instructorsRecyclerView.setAdapter(instructorAdapter);

        categorySpinner = view.findViewById(R.id.instructorCategorySpinner);
        subcategorySpinner = view.findViewById(R.id.instructorSubcategorySpinner);

        setupSpinners();
        loadInstructors();

        return view;
    }

    private void setupSpinners() {
        List<String> categories = new ArrayList<>(categoryMap.keySet());
        categories.add(0, "All categories");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(categoryAdapter);

        // При выборе категории обновляем подкатегории
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                List<String> subcategories;
                if ("All categories".equals(selectedCategory)) {
                    Set<String> allSubs = new HashSet<>();
                    for (List<String> subs : categoryMap.values()) allSubs.addAll(subs);
                    subcategories = new ArrayList<>(allSubs);
                } else {
                    subcategories = new ArrayList<>(categoryMap.getOrDefault(selectedCategory, new ArrayList<>()));
                }
                Collections.sort(subcategories);
                subcategories.add(0, "All subcategories");
                ArrayAdapter<String> subcategoryAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_dropdown_item, subcategories);
                subcategorySpinner.setAdapter(subcategoryAdapter);
                filterAndUpdate();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        subcategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndUpdate();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterAndUpdate() {
        String selectedCategory = categorySpinner.getSelectedItem() != null
                ? categorySpinner.getSelectedItem().toString()
                : "All categories";
        String selectedSubcategory = subcategorySpinner.getSelectedItem() != null
                ? subcategorySpinner.getSelectedItem().toString()
                : "All subcategories";

        filteredInstructors.clear();
        for (UserModel user : allInstructors) {
            boolean match = true;

            // Фильтр по категории
            if (!"All categories".equals(selectedCategory)) {
                String userCat = user.getCategory() != null ? user.getCategory() : "";
                if (!userCat.equals(selectedCategory)) match = false;
            }

            // Фильтр по подкатегории
            if (!"All subcategories".equals(selectedSubcategory)) {
                String userSub = user.getSubcategory() != null ? user.getSubcategory() : "";
                if (!userSub.equals(selectedSubcategory)) match = false;
            }

            if (match) filteredInstructors.add(user);
        }
        instructorAdapter.notifyDataSetChanged();
    }


    private void loadInstructors() {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("type", "Instructor")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allInstructors.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null) allInstructors.add(user);
                    }
                    filterAndUpdate();
                });
    }
}
