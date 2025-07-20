package com.example.scienceyouthhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private Button logoutButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    // Чтобы помнить роль
    private String currentUserRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        logoutButton = findViewById(R.id.logoutButton);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        auth = FirebaseAuth.getInstance();
        db = FirebaseConfig.getInstance().getFirestore();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 1. Загружаем роль пользователя из Firestore
        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String email = user.getEmail();
                            currentUserRole = document.getString("type");
                            welcomeTextView.setText("Hello, " + email + " (" + currentUserRole + ")");
                            // После загрузки роли — инициализация вкладок
                            setupTabsAndFragments(currentUserRole);
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "User data not found", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("MainActivity", "Failed to load user data", task.getException());
                        Snackbar.make(findViewById(android.R.id.content), "Error loading data", Snackbar.LENGTH_SHORT).show();
                    }
                });

        logoutButton.setOnClickListener(view -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    // 2. Настройка вкладок и адаптера по роли пользователя
    private void setupTabsAndFragments(String role) {
        // Динамически собираем список фрагментов и названия для вкладок
        List<String> tabTitles = new ArrayList<>();
        List<Integer> tabIds = new ArrayList<>();

        tabTitles.add("Home");
        tabIds.add(0);

        tabTitles.add("Activities");
        tabIds.add(1);

        if (role != null && (role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Руководитель"))) {
            tabTitles.add("Users");
            tabIds.add(4); // пусть будет 4 для примера, ты сам определишь id/позицию
        }

        tabTitles.add("Photos");
        tabIds.add(2);

        tabTitles.add("Feedback");
        tabIds.add(3);

        // Свой адаптер ViewPager, который умеет по tabIds создавать разные фрагменты
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), tabIds, role);
        viewPager.setAdapter(adapter);

        // Синхронизация TabLayout и ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles.get(position));
        }).attach();
    }
}
