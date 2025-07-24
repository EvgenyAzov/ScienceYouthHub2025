package com.example.scienceyouthhub;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.*;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, nameEditText, ageEditText;
    private Spinner roleSpinner;
    private Spinner categorySpinner, subcategorySpinner;
    private TextView categoryLabel, subcategoryLabel;
    private FirebaseAuth mAuth;

    // Категории и подкатегории для Instructor
    private final Map<String, List<String>> categoryMap = new HashMap<String, List<String>>() {{
        put("Science", Arrays.asList("Biology", "Robotics", "Physics", "Math"));
        put("Social", Arrays.asList("Leadership", "Public Speaking", "Collaboration"));
        put("Art", Arrays.asList("Art", "Writing", "Music"));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        nameEditText = findViewById(R.id.name_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        roleSpinner = findViewById(R.id.role_spinner);

        categorySpinner = findViewById(R.id.category_spinner);
        subcategorySpinner = findViewById(R.id.subcategory_spinner);
        categoryLabel = findViewById(R.id.category_label);
        subcategoryLabel = findViewById(R.id.subcategory_label);

        // По умолчанию скрыто
        categorySpinner.setVisibility(View.GONE);
        subcategorySpinner.setVisibility(View.GONE);
        categoryLabel.setVisibility(View.GONE);
        subcategoryLabel.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.registration_user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = roleSpinner.getSelectedItem().toString();
                if ("Instructor".equals(selectedRole)) {
                    // Показать category/subcategory
                    categorySpinner.setVisibility(View.VISIBLE);
                    subcategorySpinner.setVisibility(View.VISIBLE);
                    categoryLabel.setVisibility(View.VISIBLE);
                    subcategoryLabel.setVisibility(View.VISIBLE);

                    List<String> categories = new ArrayList<>(categoryMap.keySet());
                    ArrayAdapter<String> catAdapter = new ArrayAdapter<>(RegistrationActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, categories);
                    categorySpinner.setAdapter(catAdapter);

                    // Слушатель смены категории
                    categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedCat = categorySpinner.getSelectedItem().toString();
                            List<String> subs = categoryMap.getOrDefault(selectedCat, Collections.singletonList("None"));
                            ArrayAdapter<String> subAdapter = new ArrayAdapter<>(RegistrationActivity.this,
                                    android.R.layout.simple_spinner_dropdown_item, subs);
                            subcategorySpinner.setAdapter(subAdapter);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    // Первый раз заполнить подкатегории
                    if (!categories.isEmpty()) {
                        List<String> firstSubs = categoryMap.get(categories.get(0));
                        if (firstSubs != null) {
                            ArrayAdapter<String> subAdapter = new ArrayAdapter<>(RegistrationActivity.this,
                                    android.R.layout.simple_spinner_dropdown_item, firstSubs);
                            subcategorySpinner.setAdapter(subAdapter);
                        }
                    }
                } else {
                    // Скрыть
                    categorySpinner.setVisibility(View.GONE);
                    subcategorySpinner.setVisibility(View.GONE);
                    categoryLabel.setVisibility(View.GONE);
                    subcategoryLabel.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button registerButton = findViewById(R.id.register_button);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String userName = nameEditText.getText().toString();
        String userAgeStr = ageEditText.getText().toString();

        Object selectedRoleObj = roleSpinner.getSelectedItem();
        if (selectedRoleObj == null) {
            Toast.makeText(this, "Please select a role!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userRole = selectedRoleObj.toString();

        if (email.isEmpty() || password.isEmpty() || userName.isEmpty() || userAgeStr.isEmpty() || userRole.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int userAge = Integer.parseInt(userAgeStr);

        // Категории для Instructor
        String userCategory;
        String userSubcategory;
        if ("Instructor".equals(userRole)) {
            userCategory = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : null;
            userSubcategory = subcategorySpinner.getSelectedItem() != null ? subcategorySpinner.getSelectedItem().toString() : null;
            if (userCategory == null || userSubcategory == null) {
                Toast.makeText(this, "Please select category and subcategory", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            userSubcategory = null;
            userCategory = null;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            UserModel newUser = new UserModel(
                                    userId, userName, userAge, userRole, null, null, userCategory, userSubcategory
                            );

                            // 1. Save to Firestore
                            FirebaseConfig.saveUserToFirestore(newUser);

                            // 2. Save to SQLite
                            DatabaseHelper dbHelper = new DatabaseHelper(this);
                            dbHelper.insertOrUpdateUser(newUser);

                            dbHelper.logAllUsers();

                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                            finish(); // Go back to login screen
                        }
                    } else {
                        Toast.makeText(this, "Registration error: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
