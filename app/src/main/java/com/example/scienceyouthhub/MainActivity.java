package com.example.scienceyouthhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private String userRole;
    private String userName;

    private final List<String> tabTitles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        userRole = loadUserRole();
        userName = loadUserName();

        setupTabsByRole(userRole);

        viewPagerAdapter = new ViewPagerAdapter(this, tabTitles, userRole);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles.get(position));
        }).attach();

        Button logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> logout());
        }

        // Приветствие: Hello, Admin: Ivan Ivanov
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        String welcomeText = "Hello";
        if (userRole != null && !userRole.isEmpty()) {
            welcomeText += ", " + userRole;
        }
        if (userName != null && !userName.isEmpty()) {
            welcomeText += ": " + userName;
        }
        welcomeTextView.setText(welcomeText);
    }

    private String loadUserRole() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "Student");
        // Валидируем роль
        if (!role.equals("Student") && !role.equals("Instructor") &&
                !role.equals("Parent") && !role.equals("Admin")) {
            role = "Student";
        }
        return role;
    }

    private String loadUserName() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("user_name", "");
    }

    private void setupTabsByRole(String role) {
        tabTitles.clear();

        if ("Admin".equals(role)) {
            tabTitles.add("Кружки");
            tabTitles.add("Пользователи");
            tabTitles.add("Отзывы");
            tabTitles.add("Фотографии");
        } else if ("Instructor".equals(role)) {
            tabTitles.add("Кружки");
            tabTitles.add("Мои студенты");
            tabTitles.add("Отзывы");
        } else if ("Student".equals(role)) {
            tabTitles.add("Кружки");
            tabTitles.add("Мои записи");
            tabTitles.add("Отзывы");
        } else if ("Parent".equals(role)) {
            tabTitles.add("Кружки");
            tabTitles.add("Мои дети");    // <<<<<< Добавлено!
            tabTitles.add("Отзывы");
        } else {
            tabTitles.add("Кружки");
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().remove("user_role").remove("user_name").apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
