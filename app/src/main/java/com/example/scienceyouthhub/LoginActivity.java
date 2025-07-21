package com.example.scienceyouthhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(uid).get().addOnSuccessListener(document -> {
                            if (document.exists()) {
                                // ВАЖНО: используем type!
                                String role = document.getString("type");
                                String name = document.getString("name");
                                if (role == null) role = "Student";
                                if (name == null || name.trim().isEmpty()) name = email;

                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                prefs.edit()
                                        .putString("user_role", role)
                                        .putString("user_name", name)
                                        .apply();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Профиль пользователя не найден", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "Ошибка получения роли: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
