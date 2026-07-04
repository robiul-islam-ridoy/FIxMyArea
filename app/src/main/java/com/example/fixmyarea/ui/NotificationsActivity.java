package com.example.fixmyarea.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmyarea.R;
import com.example.fixmyarea.adapters.NotificationAdapter;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.models.Notification;
import com.example.fixmyarea.utils.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private ProgressBar progressBar;
    private View emptyState;
    private NotificationAdapter adapter;
    private FirebaseManager firebaseManager;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Alerts");
        }

        firebaseManager = FirebaseManager.getInstance();
        bottomNavigation = findViewById(R.id.bottomNavigation);
        BottomNavHelper.setup(this, bottomNavigation, R.id.nav_notifications);

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);

        adapter = new NotificationAdapter(notification -> {
            // Mark as read
            if (!notification.isRead()) {
                firebaseManager.getFirestore().collection(FirebaseConstants.COLLECTION_NOTIFICATIONS)
                        .document(notification.getId())
                        .update(FirebaseConstants.FIELD_NOTIFICATION_IS_READ, true)
                        .addOnSuccessListener(aVoid -> {
                            notification.setRead(true);
                            adapter.notifyDataSetChanged();
                        });
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

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavHelper.syncTabState(this, bottomNavigation, R.id.nav_notifications);
        loadNotifications();
    }

    private void loadNotifications() {
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user == null) {
            emptyState.setVisibility(View.VISIBLE);
            notificationsRecyclerView.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.getFirestore().collection(FirebaseConstants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo(FirebaseConstants.FIELD_NOTIFICATION_USER_ID, user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        notifications.add(mapNotification(doc));
                    }

                    if (notifications.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        notificationsRecyclerView.setVisibility(View.GONE);
                    } else {
                        // Sort locally (descending by timestamp)
                        java.util.Collections.sort(notifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                        
                        emptyState.setVisibility(View.GONE);
                        notificationsRecyclerView.setVisibility(View.VISIBLE);
                        adapter.setNotifications(notifications);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                    notificationsRecyclerView.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                });
    }

    private Notification mapNotification(DocumentSnapshot doc) {
        Notification notification = new Notification();
        notification.setId(doc.getId());
        notification.setUserId(doc.getString(FirebaseConstants.FIELD_NOTIFICATION_USER_ID));
        notification.setMessage(doc.getString(FirebaseConstants.FIELD_NOTIFICATION_MESSAGE));
        notification.setPostId(doc.getString(FirebaseConstants.FIELD_NOTIFICATION_POST_ID));

        Long timestamp = doc.getLong(FirebaseConstants.FIELD_NOTIFICATION_TIMESTAMP);
        notification.setTimestamp(timestamp != null ? timestamp : 0L);

        Boolean isRead = doc.getBoolean(FirebaseConstants.FIELD_NOTIFICATION_IS_READ);
        notification.setRead(Boolean.TRUE.equals(isRead));

        return notification;
    }

}