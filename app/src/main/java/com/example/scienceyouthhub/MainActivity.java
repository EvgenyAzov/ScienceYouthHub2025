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

public class MainActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private Button logoutButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        logoutButton = findViewById(R.id.logoutButton);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        auth = FirebaseAuth.getInstance();
        db = FirebaseConfig.getInstance().getFirestore();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Load user data from Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String email = user.getEmail();
                                String role = document.getString("type");
                                welcomeTextView.setText("Hello, " + email + " (" + role + ")");
                            } else {
                                Snackbar.make(findViewById(android.R.id.content), "User data not found", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("MainActivity", "Failed to load user data", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Error loading data", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Setup ViewPager with fragments
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

        // Connect TabLayout to ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Home"); break;
                case 1: tab.setText("Activities"); break;
                case 2: tab.setText("Photos"); break;
                case 3: tab.setText("Feedback"); break;
            }
        }).attach();

        // Logout
        logoutButton.setOnClickListener(view -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}