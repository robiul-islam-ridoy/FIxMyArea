package com.example.fixmyarea.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fixmyarea.R;
import com.example.fixmyarea.auth.LoginActivity;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.utils.RoleManager;
import com.example.fixmyarea.utils.SessionManager;

import java.util.Map;

/**
 * Admin Dashboard Activity - Main control panel for administrators
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView statsText;
    private CardView manageUsersCard;
    private CardView manageIssuesCard;
    private CardView analyticsCard;
    private CardView logoutCard;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;
    private SessionManager sessionManager;
    private RoleManager roleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize managers
        firebaseManager = FirebaseManager.getInstance();
        sessionManager = SessionManager.getInstance(this);
        roleManager = RoleManager.getInstance();

        // Verify admin access
        verifyAdminAccess();

        // Initialize views
        initViews();

        // Load quick stats
        loadQuickStats();

        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        statsText = findViewById(R.id.statsText);
        manageUsersCard = findViewById(R.id.manageUsersCard);
        manageIssuesCard = findViewById(R.id.manageIssuesCard);
        analyticsCard = findViewById(R.id.analyticsCard);
        logoutCard = findViewById(R.id.logoutCard);
        progressBar = findViewById(R.id.progressBar);

        // Set welcome message
        String email = sessionManager.getUserEmail();
        if (email != null) {
            welcomeText.setText("Welcome, Admin");
        }
    }

    private void verifyAdminAccess() {
        roleManager.isAdmin(isAdmin -> {
            if (!isAdmin) {
                Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
                logout();
            }
        });
    }

    private void loadQuickStats() {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.getSystemStatistics().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                Map<String, Object> stats = task.getResult();
                int totalUsers = ((Number) stats.getOrDefault("totalUsers", 0)).intValue();
                int totalIssues = ((Number) stats.getOrDefault("totalIssues", 0)).intValue();
                int pendingIssues = ((Number) stats.getOrDefault("pendingIssues", 0)).intValue();

                String statsMessage = totalUsers + " Users  •  " + totalIssues + " Reports  •  " +
                        pendingIssues + " Pending";
                statsText.setText(statsMessage);
            } else {
                statsText.setText("Unable to load statistics");
            }
        });
    }

    private void setupClickListeners() {
        manageUsersCard.setOnClickListener(v -> openManageUsers());
        manageIssuesCard.setOnClickListener(v -> openManageIssues());
        analyticsCard.setOnClickListener(v -> openAnalytics());
        logoutCard.setOnClickListener(v -> confirmLogout());
    }

    private void openManageUsers() {
        Intent intent = new Intent(this, ManageUsersActivity.class);
        startActivity(intent);
    }

    private void openManageIssues() {
        Intent intent = new Intent(this, ManageIssuesActivity.class);
        startActivity(intent);
    }

    private void openAnalytics() {
        Intent intent = new Intent(this, AnalyticsActivity.class);
        startActivity(intent);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        firebaseManager.signOut();
        sessionManager.clearSession();
        roleManager.clearCache();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload stats when returning to dashboard
        loadQuickStats();
    }
}
