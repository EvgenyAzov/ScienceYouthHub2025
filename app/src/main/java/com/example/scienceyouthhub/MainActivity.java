package com.example.scienceyouthhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_NAME = "user_name";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private String userRole;
    private String userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        userRole = loadUserRole();
        userName = loadUserName();

        viewPagerAdapter = new ViewPagerAdapter(this, userRole);
        viewPager.setAdapter(viewPagerAdapter);

        // Attach tab titles from adapter
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(viewPagerAdapter.getTabTitle(position));
        }).attach();

        Button logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> logout());
        }

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        String welcomeText = buildWelcomeText(userRole, userName);
        welcomeTextView.setText(welcomeText);

        // --- ВАЖНО: уведомление для инструктора ---
        if ("Instructor".equals(userRole)) {
            String instructorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            checkInstructorNotifications(instructorId);
        }
    }

    private String loadUserRole() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_ROLE, "Student");
    }

    private String loadUserName() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_NAME, "");
    }

    private String buildWelcomeText(String role, String name) {
        StringBuilder sb = new StringBuilder("Hello");
        if (role != null && !role.isEmpty()) {
            sb.append(", ").append(role);
        }
        if (name != null && !name.isEmpty()) {
            sb.append(": ").append(name);
        }
        return sb.toString();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(KEY_ROLE).remove(KEY_NAME).apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --------- УВЕДОМЛЕНИЕ ДЛЯ ИНСТРУКТОРА ---------
    private void checkInstructorNotifications(String instructorId) {
        FirebaseFirestore.getInstance().collection("activities")
                .whereEqualTo("instructorId", instructorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> lowEnrollmentEvents = new ArrayList<>();
                    Date now = new Date();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ActivityModel activity = doc.toObject(ActivityModel.class);
                        if (activity != null && activity.getEndDate() != null && activity.getEndDate().before(now)) {
                            // Здесь participants должен быть массивом id участников!
                            List<String> participants = (List<String>) doc.get("participants");
                            int numParticipants = participants != null ? participants.size() : 0;
                            if (numParticipants < 5) {
                                lowEnrollmentEvents.add(activity.getName());
                            }
                        }
                    }
                    if (!lowEnrollmentEvents.isEmpty()) {
                        showLowEnrollmentDialog(lowEnrollmentEvents);
                    }
                });
    }

    private void showLowEnrollmentDialog(List<String> eventNames) {
        StringBuilder msg = new StringBuilder("Внимание! На следующие мероприятия записано менее 5 участников:\n\n");
        for (String s : eventNames) msg.append("• ").append(s).append("\n");
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Уведомление инструктора")
                .setMessage(msg.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}
