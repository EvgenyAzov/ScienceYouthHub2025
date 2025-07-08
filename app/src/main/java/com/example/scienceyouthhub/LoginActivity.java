package com.example.scienceyouthhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Activity for user login and registration.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        Button loginButton = findViewById(R.id.login_button);
        Button registerButton = findViewById(R.id.register_button);
        TextView forgotPasswordText = findViewById(R.id.forgot_password_text);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (prefs.getString("userId", null) != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Login button click
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Login successful");
                            saveLoginState(mAuth.getCurrentUser().getUid());
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Log.e(TAG, "Login failed", task.getException());
                            Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Register button click
        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Registration successful");
                            saveLoginState(mAuth.getCurrentUser().getUid());
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Log.e(TAG, "Registration failed", task.getException());
                            Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Forgot password click
        forgotPasswordText.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Password reset failed", task.getException());
                            Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    /**
     * Saves user login state to SharedPreferences.
     * @param userId The ID of the logged-in user
     */
    private void saveLoginState(String userId) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userId", userId);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", emailEditText.getText().toString());
        outState.putString("password", passwordEditText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        emailEditText.setText(savedInstanceState.getString("email"));
        passwordEditText.setText(savedInstanceState.getString("password"));
    }
}