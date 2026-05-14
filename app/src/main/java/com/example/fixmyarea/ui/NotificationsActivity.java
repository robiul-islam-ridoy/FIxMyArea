package com.example.fixmyarea.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmyarea.R;
import com.example.fixmyarea.adapters.NotificationAdapter;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.models.Notification;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyState;
    private NotificationAdapter adapter;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifications");
        }

        firebaseManager = FirebaseManager.getInstance();
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);

        adapter = new NotificationAdapter(notification -> {
            // Mark as read
            if (!notification.isRead()) {
                firebaseManager.getFirestore().collection("notifications")
                        .document(notification.getId())
                        .update("isRead", true);
            }

            // Open the post details if postId exists
            if (notification.getPostId() != null && !notification.getPostId().isEmpty()) {
                Intent intent = new Intent(this, PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, notification.getPostId());
                // In a real app we might fetch the full post data here or let PostDetailActivity do it
                startActivity(intent);
            }
        });

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user == null) {
            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.getFirestore().collection("notifications")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) {
                            n.setId(doc.getId());
                            notifications.add(n);
                        }
                    }

                    if (notifications.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                    } else {
                        // Sort locally (descending by timestamp)
                        java.util.Collections.sort(notifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                        
                        emptyState.setVisibility(View.GONE);
                        adapter.setNotifications(notifications);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
