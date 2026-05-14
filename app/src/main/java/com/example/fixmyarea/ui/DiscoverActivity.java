package com.example.fixmyarea.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmyarea.R;
import com.example.fixmyarea.adapters.PostAdapter;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscoverActivity extends AppCompatActivity {

    private static final String TAG = "DiscoverActivity";

    private BottomNavigationView bottomNavigation;
    private RecyclerView postsRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    private SearchView searchView;
    private Spinner sortSpinner;

    private FirebaseManager firebaseManager;
    private PostAdapter postAdapter;
    private List<Post> allPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        firebaseManager = FirebaseManager.getInstance();

        initializeViews();
        setupSearchAndSort();
        setupRecyclerView();
        setupBottomNavigation();

        loadPosts();

        loadProfileImageIntoBottomNav();
    }

    private void loadProfileImageIntoBottomNav() {
        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser != null) {
            firebaseManager.getDocument(FirebaseConstants.COLLECTION_USERS, currentUser.getUid())
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String profileImageUrl = documentSnapshot.getString(FirebaseConstants.FIELD_USER_PROFILE_IMAGE);
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                android.view.MenuItem profileItem = bottomNavigation.getMenu().findItem(R.id.nav_profile);
                                com.bumptech.glide.Glide.with(this)
                                        .asBitmap()
                                        .load(profileImageUrl)
                                        .circleCrop()
                                        .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                                            @Override
                                            public void onResourceReady(@androidx.annotation.NonNull android.graphics.Bitmap resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                                                profileItem.setIcon(new android.graphics.drawable.BitmapDrawable(getResources(), resource));
                                            }
                                            @Override
                                            public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {}
                                        });
                            }
                        }
                    });
        }
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        postsRecyclerView = findViewById(R.id.postsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        searchView = findViewById(R.id.searchView);
        sortSpinner = findViewById(R.id.sortSpinner);
    }

    private void setupSearchAndSort() {
        String[] sortOptions = {"Newest", "Oldest", "Most Liked", "Category"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortOptions);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilterAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilterAndSort();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilterAndSort();
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        String currentUserId = firebaseManager.getCurrentUser() != null ? firebaseManager.getCurrentUser().getUid() : "";
        postAdapter = new PostAdapter(currentUserId, new PostAdapter.PostActionCallback() {
            @Override
            public void onPostClick(Post post) {
                Intent intent = new Intent(DiscoverActivity.this, PostDetailActivity.class);
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
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        postsRecyclerView.setVisibility(View.GONE);

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

                        if (document.contains(FirebaseConstants.FIELD_ISSUE_LATITUDE)) {
                            post.setLatitude(document.getDouble(FirebaseConstants.FIELD_ISSUE_LATITUDE));
                        }
                        if (document.contains(FirebaseConstants.FIELD_ISSUE_LONGITUDE)) {
                            post.setLongitude(document.getDouble(FirebaseConstants.FIELD_ISSUE_LONGITUDE));
                        }

                        Object imageUrlObj = document.get(FirebaseConstants.FIELD_ISSUE_IMAGE_URL);
                        if (imageUrlObj instanceof List) {
                            post.setImageUrls((List<String>) imageUrlObj);
                        } else if (imageUrlObj instanceof String) {
                            List<String> images = new ArrayList<>();
                            images.add((String) imageUrlObj);
                            post.setImageUrls(images);
                        }

                        post.setReporterId(document.getString(FirebaseConstants.FIELD_ISSUE_REPORTER_ID));

                        Long timestamp = document.getLong(FirebaseConstants.FIELD_ISSUE_TIMESTAMP);
                        if (timestamp != null) {
                            post.setTimestamp(timestamp);
                        }

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

                    progressBar.setVisibility(View.GONE);
                    allPosts = posts;

                    if (posts.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        postsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        postsRecyclerView.setVisibility(View.VISIBLE);
                        applyFilterAndSort();
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

    private void applyFilterAndSort() {
        if (allPosts == null) return;

        String query = searchView.getQuery() != null ? searchView.getQuery().toString().toLowerCase() : "";
        String sortOption = sortSpinner.getSelectedItem() != null ? sortSpinner.getSelectedItem().toString() : "Newest";

        List<Post> filtered = new ArrayList<>();
        for (Post post : allPosts) {
            boolean matchesSearch = false;
            if (post.getTitle() != null && post.getTitle().toLowerCase().contains(query)) matchesSearch = true;
            if (post.getDescription() != null && post.getDescription().toLowerCase().contains(query)) matchesSearch = true;
            if (post.getCategory() != null && post.getCategory().toLowerCase().contains(query)) matchesSearch = true;
            
            if (matchesSearch) {
                filtered.add(post);
            }
        }

        Collections.sort(filtered, (p1, p2) -> {
            if ("Oldest".equals(sortOption)) {
                return Long.compare(p1.getTimestamp(), p2.getTimestamp());
            } else if ("Most Liked".equals(sortOption)) {
                int likes1 = p1.getLikedBy() != null ? p1.getLikedBy().size() : 0;
                int likes2 = p2.getLikedBy() != null ? p2.getLikedBy().size() : 0;
                return Integer.compare(likes2, likes1);
            } else if ("Category".equals(sortOption)) {
                String cat1 = p1.getCategory() != null ? p1.getCategory() : "";
                String cat2 = p2.getCategory() != null ? p2.getCategory() : "";
                return cat1.compareToIgnoreCase(cat2);
            } else { // "Newest"
                return Long.compare(p2.getTimestamp(), p1.getTimestamp());
            }
        });

        postAdapter.setPosts(filtered);
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
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", toUserId);
        notification.put("message", message);
        notification.put("postId", postId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("isRead", false);

        firebaseManager.getFirestore().collection("notifications").add(notification);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setItemIconTintList(null);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_discover) {
                return true; // Already on discover
            } else if (itemId == R.id.nav_create) {
                Intent intent = new Intent(this, CreatePostActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_notifications) {
                Intent intent = new Intent(this, NotificationsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Set Discover as the default selected item
        bottomNavigation.setSelectedItemId(R.id.nav_discover);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_discover);
    }
}
