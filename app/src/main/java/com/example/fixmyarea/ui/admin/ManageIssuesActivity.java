package com.example.fixmyarea.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmyarea.R;
import com.example.fixmyarea.adapters.AdminIssueAdapter;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.models.Post;
import com.example.fixmyarea.ui.PostDetailActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage Issues Activity - Admin can view and manage (approve/reject) issue
 * reports
 */
public class ManageIssuesActivity extends AppCompatActivity implements AdminIssueAdapter.IssueActionListener {

    private RecyclerView issuesRecyclerView;
    private AdminIssueAdapter issueAdapter;
    private List<Post> issueList;
    private ProgressBar progressBar;
    private TextView emptyState;
    private ChipGroup filterChipGroup;

    private FirebaseManager firebaseManager;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_issues);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Issues");
        }

        firebaseManager = FirebaseManager.getInstance();

        initViews();
        loadIssues();
    }

    private void initViews() {
        issuesRecyclerView = findViewById(R.id.issuesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        filterChipGroup = findViewById(R.id.filterChipGroup);

        // Setup RecyclerView
        issueList = new ArrayList<>();
        issueAdapter = new AdminIssueAdapter(issueList, this);
        issuesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        issuesRecyclerView.setAdapter(issueAdapter);

        // Setup filter chips
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = findViewById(checkedId);
                currentFilter = chip.getTag().toString();
                loadIssues();
            }
        });
    }

    private void loadIssues() {
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        if ("all".equals(currentFilter)) {
            firebaseManager.getAllDocuments(FirebaseConstants.COLLECTION_ISSUES)
                    .addOnCompleteListener(task -> handleIssuesLoaded(task.getResult()));
        } else {
            firebaseManager.getIssuesByStatus(currentFilter)
                    .addOnCompleteListener(task -> handleIssuesLoaded(task.getResult()));
        }
    }

    private void handleIssuesLoaded(com.google.firebase.firestore.QuerySnapshot result) {
        progressBar.setVisibility(View.GONE);

        if (result != null) {
            issueList.clear();

            for (DocumentSnapshot doc : result.getDocuments()) {
                Post post = doc.toObject(Post.class);
                if (post != null) {
                    post.setPostId(doc.getId());
                    issueList.add(post);
                }
            }

            issueAdapter.notifyDataSetChanged();

            if (issueList.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
            } else {
                emptyState.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "Failed to load issues", Toast.LENGTH_SHORT).show();
            emptyState.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onApproveIssue(Post issue) {
        showStatusChangeDialog(issue, "Approve Issue", FirebaseConstants.STATUS_APPROVED);
    }

    @Override
    public void onRejectIssue(Post issue) {
        showStatusChangeDialog(issue, "Reject Issue", FirebaseConstants.STATUS_REJECTED);
    }

    @Override
    public void onMarkAsDone(Post issue) {
        showStatusChangeDialog(issue, "Mark as Done", FirebaseConstants.STATUS_RESOLVED);
    }

    @Override
    public void onIssueClick(Post issue) {
        // Open post detail activity
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId", issue.getPostId());
        startActivity(intent);
    }

    private void showStatusChangeDialog(Post issue, String title, String newStatus) {
        String action = newStatus.equals(FirebaseConstants.STATUS_APPROVED) ? "approve" : "reject";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Are you sure you want to " + action + " this issue report?")
                .setPositiveButton("Confirm", (dialog, which) -> updateIssueStatus(issue.getPostId(), newStatus))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateIssueStatus(String issueId, String newStatus) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.updateIssueStatus(issueId, newStatus).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                String message = newStatus.equals(FirebaseConstants.STATUS_APPROVED)
                        ? "Issue approved successfully"
                        : "Issue rejected successfully";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                loadIssues();
            } else {
                Toast.makeText(this, "Failed to update issue status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
