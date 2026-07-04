package com.example.fixmyarea.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fixmyarea.R;
import com.example.fixmyarea.adapters.PostAdapter;
import com.example.fixmyarea.auth.LoginActivity;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.models.Post;
import com.example.fixmyarea.utils.BottomNavHelper;
import com.example.fixmyarea.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Activity displayed after successful login
 */
public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private BottomNavigationView bottomNavigation;
    private RecyclerView postsRecyclerView;
    private ProgressBar progressBar;
    private View emptyState;

    private FirebaseManager firebaseManager;
    private SessionManager sessionManager;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Manager
        firebaseManager = FirebaseManager.getInstance();

        // Initialize Session Manager
        sessionManager = SessionManager.getInstance(this);

        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation);
        postsRecyclerView = findViewById(R.id.postsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);

        // Setup RecyclerView
        setupRecyclerView();

        // Load posts
        loadPosts();

        // Set up bottom navigation
        BottomNavHelper.setup(this, bottomNavigation, R.id.nav_home);
    }

    private void logout() {
        // Clear session from DataStore
        sessionManager.clearSession();

        // Sign out from Firebase
        firebaseManager.signOut();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void setupRecyclerView() {
        String currentUserId = firebaseManager.getCurrentUser() != null ? firebaseManager.getCurrentUser().getUid() : "";
        postAdapter = new PostAdapter(currentUserId, new PostAdapter.PostActionCallback() {
            @Override
            public void onPostClick(Post post) {
                // Open post detail activity
                Intent intent = new Intent(DashboardActivity.this, PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getPostId());
                intent.putExtra(PostDetailActivity.EXTRA_POST_TITLE, post.getTitle());
                intent.putExtra(PostDetailActivity.EXTRA_POST_DESCRIPTION, post.getDescription());
                intent.putExtra(PostDetailActivity.EXTRA_POST_CATEGORY, post.getCategory());
                intent.putExtra(PostDetailActivity.EXTRA_POST_STATUS, post.getStatus());
                intent.putExtra(PostDetailActivity.EXTRA_POST_LOCATION, post.getLocation());
                intent.putExtra(PostDetailActivity.EXTRA_POST_TIMESTAMP, post.getTimestamp());
                intent.putExtra(PostDetailActivity.EXTRA_POST_UPVOTES, post.getUpvotes());

                if (post.getImageUrls() != null) {
                    intent.putStringArrayListExtra(PostDetailActivity.EXTRA_POST_IMAGE_URLS,
                            new ArrayList<>(post.getImageUrls()));
                }

                startActivity(intent);
            }

            @Override
            public void onLikeClick(Post post) {
                handleLikeDislike(post, true);
            }

            @Override
            public void onDislikeClick(Post post) {
                handleLikeDislike(post, false);
            }

            @Override
            public void onDeleteClick(Post post) {
                confirmDeletePost(post);
            }
        });

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(postAdapter);
    }

    private void loadPosts() {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        postsRecyclerView.setVisibility(View.GONE);

        // Fetch posts from Firestore, ordered by timestamp (newest first)
        firebaseManager.getFirestore().collection(FirebaseConstants.COLLECTION_ISSUES)
                .orderBy(FirebaseConstants.FIELD_ISSUE_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Post post = new Post();
                        post.setPostId(document.getId());
                        post.setTitle(document.getString(FirebaseConstants.FIELD_ISSUE_TITLE));
                        post.setDescription(document.getString(FirebaseConstants.FIELD_ISSUE_DESCRIPTION));
                        post.setCategory(document.getString(FirebaseConstants.FIELD_ISSUE_CATEGORY));
                        post.setStatus(document.getString(FirebaseConstants.FIELD_ISSUE_STATUS));
                        post.setLocation(document.getString(FirebaseConstants.FIELD_ISSUE_LOCATION));

                        // Get coordinates if available
                        if (document.contains(FirebaseConstants.FIELD_ISSUE_LATITUDE)) {
                            post.setLatitude(document.getDouble(FirebaseConstants.FIELD_ISSUE_LATITUDE));
                        }
                        if (document.contains(FirebaseConstants.FIELD_ISSUE_LONGITUDE)) {
                            post.setLongitude(document.getDouble(FirebaseConstants.FIELD_ISSUE_LONGITUDE));
                        }

                        // Get images
                        Object imageUrlObj = document.get(FirebaseConstants.FIELD_ISSUE_IMAGE_URL);
                        if (imageUrlObj instanceof List) {
                            post.setImageUrls((List<String>) imageUrlObj);
                        } else if (imageUrlObj instanceof String) {
                            List<String> images = new ArrayList<>();
                            images.add((String) imageUrlObj);
                            post.setImageUrls(images);
                        }

                        post.setReporterId(document.getString(FirebaseConstants.FIELD_ISSUE_REPORTER_ID));

                        // Get timestamp
                        Long timestamp = document.getLong(FirebaseConstants.FIELD_ISSUE_TIMESTAMP);
                        if (timestamp != null) {
                            post.setTimestamp(timestamp);
                        }

                        // Get upvotes and likes/dislikes
                        Long upvotes = document.getLong(FirebaseConstants.FIELD_ISSUE_UPVOTES);
                        if (upvotes != null) {
                            post.setUpvotes(upvotes.intValue());
                        }

                        Object likedByObj = document.get("likedBy");
                        if (likedByObj instanceof List) {
                            post.setLikedBy((List<String>) likedByObj);
                        }
                        
                        Object dislikedByObj = document.get("dislikedBy");
                        if (dislikedByObj instanceof List) {
                            post.setDislikedBy((List<String>) dislikedByObj);
                        }

                        posts.add(post);
                    }

                    // Hide loading
                    progressBar.setVisibility(View.GONE);

                    // Filter for "My Area" (Mocked for now, assumes "Dhaka")
                    List<Post> areaPosts = new ArrayList<>();
                    for (Post p : posts) {
                        if (p.getLocation() != null && p.getLocation().toLowerCase().contains("dhaka")) {
                            areaPosts.add(p);
                        } else if (p.getLocation() == null) {
                            areaPosts.add(p); // Include if no location
                        }
                    }
                    
                    // If no posts in "dhaka", just show all posts as a fallback for the prototype
                    if (areaPosts.isEmpty()) {
                        areaPosts.addAll(posts);
                    }

                    // Update UI based on results
                    if (areaPosts.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        postsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        postsRecyclerView.setVisibility(View.VISIBLE);
                        postAdapter.setPosts(areaPosts);
                    }

                    Log.d(TAG, "Loaded " + areaPosts.size() + " posts");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                    postsRecyclerView.setVisibility(View.GONE);

                    Log.e(TAG, "Error loading posts", e);
                    Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavHelper.syncTabState(this, bottomNavigation, R.id.nav_home);
        loadPosts();
    }

    private void handleLikeDislike(Post post, boolean isLike) {
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        List<String> likedBy = post.getLikedBy() != null ? new ArrayList<>(post.getLikedBy()) : new ArrayList<>();
        List<String> dislikedBy = post.getDislikedBy() != null ? new ArrayList<>(post.getDislikedBy()) : new ArrayList<>();

        boolean wasLiked = likedBy.contains(userId);
        boolean wasDisliked = dislikedBy.contains(userId);

        boolean stateChanged = false;

        if (isLike) {
            if (wasLiked) {
                likedBy.remove(userId);
            } else {
                likedBy.add(userId);
                dislikedBy.remove(userId);
                if (post.getReporterId() != null && !post.getReporterId().equals(userId)) {
                    sendNotification(post.getReporterId(), post.getPostId(), "Someone liked your post: " + post.getTitle());
                }
            }
            stateChanged = true;
        } else {
            if (wasDisliked) {
                dislikedBy.remove(userId);
            } else {
                dislikedBy.add(userId);
                likedBy.remove(userId);
                if (post.getReporterId() != null && !post.getReporterId().equals(userId)) {
                    sendNotification(post.getReporterId(), post.getPostId(), "Someone disliked your post: " + post.getTitle());
                }
            }
            stateChanged = true;
        }

        if (stateChanged) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("likedBy", likedBy);
            updates.put("dislikedBy", dislikedBy);

            firebaseManager.getFirestore().collection(FirebaseConstants.COLLECTION_ISSUES)
                    .document(post.getPostId()).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        post.setLikedBy(likedBy);
                        post.setDislikedBy(dislikedBy);
                        postAdapter.notifyDataSetChanged();
                    });
        }
    }

    private void confirmDeletePost(Post post) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseManager.getFirestore().collection(FirebaseConstants.COLLECTION_ISSUES)
                            .document(post.getPostId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
                                loadPosts();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendNotification(String toUserId, String postId, String message) {
        firebaseManager.createNotification(toUserId, postId, message);
    }
}
