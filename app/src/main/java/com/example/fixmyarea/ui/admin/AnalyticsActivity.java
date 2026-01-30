package com.example.fixmyarea.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fixmyarea.R;
import com.example.fixmyarea.firebase.FirebaseManager;

import java.util.Map;

/**
 * Analytics Activity - Displays system statistics and analytics
 */
public class AnalyticsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView totalUsersText, totalIssuesText;
    private TextView pendingText, approvedText, inProgressText, resolvedText, rejectedText;
    private TextView roadText, waterText, electricityText, sanitationText, otherText;

    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Analytics");
        }

        firebaseManager = FirebaseManager.getInstance();

        initViews();
        loadStatistics();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        totalUsersText = findViewById(R.id.totalUsersText);
        totalIssuesText = findViewById(R.id.totalIssuesText);

        // Status breakdown views
        View pendingRow = findViewById(R.id.pendingRow);
        View approvedRow = findViewById(R.id.approvedRow);
        View inProgressRow = findViewById(R.id.inProgressRow);
        View resolvedRow = findViewById(R.id.resolvedRow);
        View rejectedRow = findViewById(R.id.rejectedRow);

        pendingText = pendingRow.findViewById(R.id.statValue);
        approvedText = approvedRow.findViewById(R.id.statValue);
        inProgressText = inProgressRow.findViewById(R.id.statValue);
        resolvedText = resolvedRow.findViewById(R.id.statValue);
        rejectedText = rejectedRow.findViewById(R.id.statValue);

        ((TextView) pendingRow.findViewById(R.id.statLabel)).setText("Pending");
        ((TextView) approvedRow.findViewById(R.id.statLabel)).setText("Approved");
        ((TextView) inProgressRow.findViewById(R.id.statLabel)).setText("In Progress");
        ((TextView) resolvedRow.findViewById(R.id.statLabel)).setText("Resolved");
        ((TextView) rejectedRow.findViewById(R.id.statLabel)).setText("Rejected");

        // Category breakdown views
        View roadRow = findViewById(R.id.roadRow);
        View waterRow = findViewById(R.id.waterRow);
        View electricityRow = findViewById(R.id.electricityRow);
        View sanitationRow = findViewById(R.id.sanitationRow);
        View otherRow = findViewById(R.id.otherRow);

        roadText = roadRow.findViewById(R.id.statValue);
        waterText = waterRow.findViewById(R.id.statValue);
        electricityText = electricityRow.findViewById(R.id.statValue);
        sanitationText = sanitationRow.findViewById(R.id.statValue);
        otherText = otherRow.findViewById(R.id.statValue);

        ((TextView) roadRow.findViewById(R.id.statLabel)).setText("Road Issues");
        ((TextView) waterRow.findViewById(R.id.statLabel)).setText("Water Issues");
        ((TextView) electricityRow.findViewById(R.id.statLabel)).setText("Electricity Issues");
        ((TextView) sanitationRow.findViewById(R.id.statLabel)).setText("Sanitation Issues");
        ((TextView) otherRow.findViewById(R.id.statLabel)).setText("Other Issues");
    }

    private void loadStatistics() {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.getSystemStatistics().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                Map<String, Object> stats = task.getResult();

                // Overall stats
                totalUsersText.setText(String.valueOf(stats.getOrDefault("totalUsers", 0)));
                totalIssuesText.setText(String.valueOf(stats.getOrDefault("totalIssues", 0)));

                // Status breakdown
                pendingText.setText(String.valueOf(stats.getOrDefault("pendingIssues", 0)));
                approvedText.setText(String.valueOf(stats.getOrDefault("approvedIssues", 0)));
                inProgressText.setText(String.valueOf(stats.getOrDefault("inProgressIssues", 0)));
                resolvedText.setText(String.valueOf(stats.getOrDefault("resolvedIssues", 0)));
                rejectedText.setText(String.valueOf(stats.getOrDefault("rejectedIssues", 0)));

                // Category breakdown
                roadText.setText(String.valueOf(stats.getOrDefault("roadIssues", 0)));
                waterText.setText(String.valueOf(stats.getOrDefault("waterIssues", 0)));
                electricityText.setText(String.valueOf(stats.getOrDefault("electricityIssues", 0)));
                sanitationText.setText(String.valueOf(stats.getOrDefault("sanitationIssues", 0)));
                otherText.setText(String.valueOf(stats.getOrDefault("otherIssues", 0)));
            } else {
                Toast.makeText(this, "Failed to load statistics", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
