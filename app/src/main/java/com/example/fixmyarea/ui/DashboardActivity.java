package com.example.fixmyarea.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

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

    private ImageView profileImage;
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
        profileImage = findViewById(R.id.profileImage);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        postsRecyclerView = findViewById(R.id.postsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);

        // Setup RecyclerView
        setupRecyclerView();

        // Load user data
        loadUserData();

        // Load posts
        loadPosts();

        // Set click listener for profile image to show menu
        profileImage.setOnClickListener(v -> showProfileMenu(v));

        // Set up bottom navigation
        setupBottomNavigation();
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Fetch user profile from Firestore
            firebaseManager.getDocument(FirebaseConstants.COLLECTION_USERS, userId)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get profile image URL
                            String profileImageUrl = documentSnapshot.getString(
                                    FirebaseConstants.FIELD_USER_PROFILE_IMAGE);

                            // Load profile image
                            loadProfileImage(profileImageUrl);

                            Log.d(TAG, "User profile loaded successfully");
                        } else {
                            Log.w(TAG, "User profile document does not exist");
                            showDefaultProfileImage();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user profile", e);
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                        showDefaultProfileImage();
                    });
        } else {
            Log.w(TAG, "No current user found");
            showDefaultProfileImage();
        }
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Load image from URL using Glide
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground) // Placeholder while loading
                    .error(R.drawable.ic_launcher_foreground) // Error fallback
                    .into(profileImage);

            Log.d(TAG, "Loading profile image from: " + imageUrl);
        } else {
            // No profile image URL available
            showDefaultProfileImage();
        }
    }

    private void showDefaultProfileImage() {
        // Show default profile icon
        profileImage.setImageResource(R.drawable.ic_launcher_foreground);
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_view_profile) {
                // Navigate to Profile Activity
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });

        popupMenu.show();
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
        postAdapter = new PostAdapter(post -> {
            // Open post detail activity
            Intent intent = new Intent(this, PostDetailActivity.class);
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
                        }

                        post.setReporterId(document.getString(FirebaseConstants.FIELD_ISSUE_REPORTER_ID));

                        // Get timestamp
                        Long timestamp = document.getLong(FirebaseConstants.FIELD_ISSUE_TIMESTAMP);
                        if (timestamp != null) {
                            post.setTimestamp(timestamp);
                        }

                        // Get upvotes
                        Long upvotes = document.getLong(FirebaseConstants.FIELD_ISSUE_UPVOTES);
                        if (upvotes != null) {
                            post.setUpvotes(upvotes.intValue());
                        }

                        posts.add(post);
                    }

                    // Hide loading
                    progressBar.setVisibility(View.GONE);

                    // Update UI based on results
                    if (posts.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        postsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        postsRecyclerView.setVisibility(View.VISIBLE);
                        postAdapter.setPosts(posts);
                    }

                    Log.d(TAG, "Loaded " + posts.size() + " posts");
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
        // Reload posts when returning to dashboard
        loadPosts();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_discover) {
                Toast.makeText(this, "Discover clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_create) {
                // Navigate to Create Post Activity
                Intent intent = new Intent(this, CreatePostActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_notifications) {
                Toast.makeText(this, "Alerts clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_menu) {
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Set Home as the default selected item
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}
