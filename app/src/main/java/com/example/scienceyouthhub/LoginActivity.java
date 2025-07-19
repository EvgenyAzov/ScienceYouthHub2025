package com.example.scienceyouthhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText emailEditText, passwordEditText, nameEditText, ageEditText;
    private Spinner roleSpinner;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseConfig.getInstance().getFirestore();

        // Initialize UI components
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        nameEditText = findViewById(R.id.name_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        roleSpinner = findViewById(R.id.role_spinner);
        Button loginButton = findViewById(R.id.login_button);
        Button registerButton = findViewById(R.id.register_button);
        TextView forgotPasswordText = findViewById(R.id.forgot_password_text);

        // Setup Spinner for roles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // CHECK: If user is already logged in — go to MainActivity
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Login button click
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "Please fill email and password", Snackbar.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Login successful");
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Log.e(TAG, "Login failed", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Login failed: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
        });

        // Register button click
        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String ageStr = ageEditText.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || ageStr.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "Please fill all fields", Snackbar.LENGTH_SHORT).show();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                Snackbar.make(findViewById(android.R.id.content), "Invalid age", Snackbar.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Registration successful");
                            FirebaseUser newUser = mAuth.getCurrentUser();  // Переименовали здесь
                            if (newUser != null) {
                                // Save additional user info to Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email);
                                userData.put("name", name);
                                userData.put("age", age);
                                userData.put("type", role);  // Role as "type"

                                db.collection("users").document(newUser.getUid())
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "User data saved");
                                            Snackbar.make(findViewById(android.R.id.content), "Registration complete", Snackbar.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to save user data", e);
                                            Snackbar.make(findViewById(android.R.id.content), "Failed to save data: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Log.e(TAG, "Registration failed", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Registration failed: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
        });

        // Forgot password click
        forgotPasswordText.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "Please enter email", Snackbar.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Snackbar.make(findViewById(android.R.id.content), "Password reset email sent", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Password reset failed", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Failed to send reset email", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", emailEditText.getText().toString());
        outState.putString("password", passwordEditText.getText().toString());
        outState.putString("name", nameEditText.getText().toString());
        outState.putString("age", ageEditText.getText().toString());
        outState.putInt("role_position", roleSpinner.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        emailEditText.setText(savedInstanceState.getString("email"));
        passwordEditText.setText(savedInstanceState.getString("password"));
        nameEditText.setText(savedInstanceState.getString("name"));
        ageEditText.setText(savedInstanceState.getString("age"));
        roleSpinner.setSelection(savedInstanceState.getInt("role_position"));
    }
}