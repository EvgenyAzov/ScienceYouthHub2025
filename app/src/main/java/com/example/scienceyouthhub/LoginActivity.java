package com.example.scienceyouthhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> loginUser());

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(uid).get().addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String role = document.getString("type");
                                String name = document.getString("name");
                                if (role == null) role = "Student";
                                if (name == null || name.trim().isEmpty()) name = email;

                                Log.d("LOGIN", "role=" + role + " name=" + name);

                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                prefs.edit()
                                        .putString("user_role", role)
                                        .putString("user_name", name)
                                        .apply();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "Error getting role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
