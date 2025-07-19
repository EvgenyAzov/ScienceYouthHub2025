package com.example.scienceyouthhub;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, nameEditText, ageEditText;
    private Spinner roleSpinner;
    private FirebaseAuth mAuth;

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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

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

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            UserModel newUser = new UserModel(userId, userName, userAge, userRole);

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
