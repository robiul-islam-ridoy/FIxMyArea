package com.example.fixmyarea.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fixmyarea.R;
import com.example.fixmyarea.adapters.PostImageAdapter;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.models.Post;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display full post details
 */
public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";
    public static final String EXTRA_POST_ID = "post_id";
    public static final String EXTRA_POST_TITLE = "post_title";
    public static final String EXTRA_POST_DESCRIPTION = "post_description";
    public static final String EXTRA_POST_CATEGORY = "post_category";
    public static final String EXTRA_POST_STATUS = "post_status";
    public static final String EXTRA_POST_LOCATION = "post_location";
    public static final String EXTRA_POST_TIMESTAMP = "post_timestamp";
    public static final String EXTRA_POST_UPVOTES = "post_upvotes";
    public static final String EXTRA_POST_IMAGE_URLS = "post_image_urls";

    // UI Components
    private ImageButton backButton;
    private ViewPager2 imagesViewPager;
    private TextView imageIndicator;
    private TextView postTitle;
    private Chip categoryChip;
    private TextView statusBadge;
    private TextView postDescription;
    private TextView postLocation;
    private TextView postTime;
    private TextView tvLikeCountDetail;
    private TextView tvDislikeCountDetail;

    private PostImageAdapter imageAdapter;
    private Post post;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();

        // Initialize views
        initializeViews();

        // Load post data from intent or Firestore
        loadPostData();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        imagesViewPager = findViewById(R.id.imagesViewPager);
        imageIndicator = findViewById(R.id.imageIndicator);
        postTitle = findViewById(R.id.postTitle);
        categoryChip = findViewById(R.id.categoryChip);
        statusBadge = findViewById(R.id.statusBadge);
        postDescription = findViewById(R.id.postDescription);
        postLocation = findViewById(R.id.postLocation);
        postTime = findViewById(R.id.postTime);
        tvLikeCountDetail = findViewById(R.id.tvLikeCountDetail);
        tvDislikeCountDetail = findViewById(R.id.tvDislikeCountDetail);

        // Setup image adapter
        imageAdapter = new PostImageAdapter();
        imagesViewPager.setAdapter(imageAdapter);
    }

    private void loadPostData() {
        // Check if we only have postId (from admin dashboard)
        String postId = getIntent().getStringExtra("postId");
        if (postId != null && !getIntent().hasExtra(EXTRA_POST_TITLE)) {
            // Load from Firestore
            loadPostFromFirestore(postId);
            return;
        }

        // Create post object from intent extras (legacy method)
        post = new Post();
        post.setPostId(getIntent().getStringExtra(EXTRA_POST_ID));
        post.setTitle(getIntent().getStringExtra(EXTRA_POST_TITLE));
        post.setDescription(getIntent().getStringExtra(EXTRA_POST_DESCRIPTION));
        post.setCategory(getIntent().getStringExtra(EXTRA_POST_CATEGORY));
        post.setStatus(getIntent().getStringExtra(EXTRA_POST_STATUS));
        post.setLocation(getIntent().getStringExtra(EXTRA_POST_LOCATION));
        post.setTimestamp(getIntent().getLongExtra(EXTRA_POST_TIMESTAMP, 0));
        post.setUpvotes(getIntent().getIntExtra(EXTRA_POST_UPVOTES, 0));

        // Get image URLs
        if (getIntent().hasExtra(EXTRA_POST_IMAGE_URLS)) {
            post.setImageUrls(getIntent().getStringArrayListExtra(EXTRA_POST_IMAGE_URLS));
        }

        // Display post data
        displayPost();
    }

    private void loadPostFromFirestore(String postId) {
        firebaseManager.getDocument("issues", postId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        post = new Post();
                        post.setPostId(documentSnapshot.getId());
                        post.setTitle(documentSnapshot.getString(FirebaseConstants.FIELD_ISSUE_TITLE));
                        post.setDescription(documentSnapshot.getString(FirebaseConstants.FIELD_ISSUE_DESCRIPTION));
                        post.setCategory(documentSnapshot.getString(FirebaseConstants.FIELD_ISSUE_CATEGORY));
                        post.setStatus(documentSnapshot.getString(FirebaseConstants.FIELD_ISSUE_STATUS));
                        post.setLocation(documentSnapshot.getString(FirebaseConstants.FIELD_ISSUE_LOCATION));

                        // Get coordinates if available
                        if (documentSnapshot.contains(FirebaseConstants.FIELD_ISSUE_LATITUDE)) {
                            post.setLatitude(documentSnapshot.getDouble(FirebaseConstants.FIELD_ISSUE_LATITUDE));
                        }
                        if (documentSnapshot.contains(FirebaseConstants.FIELD_ISSUE_LONGITUDE)) {
                            post.setLongitude(documentSnapshot.getDouble(FirebaseConstants.FIELD_ISSUE_LONGITUDE));
                        }

                        // Get images - this is the key fix
                        Object imageUrlObj = documentSnapshot.get(FirebaseConstants.FIELD_ISSUE_IMAGE_URL);
                        if (imageUrlObj instanceof List) {
                            post.setImageUrls((List<String>) imageUrlObj);
                        } else if (imageUrlObj instanceof String) {
                            List<String> images = new ArrayList<>();
                            images.add((String) imageUrlObj);
                            post.setImageUrls(images);
                        }

                        post.setReporterId(documentSnapshot.getString(FirebaseConstants.FIELD_ISSUE_REPORTER_ID));

                        // Get timestamp
                        Long timestamp = documentSnapshot.getLong(FirebaseConstants.FIELD_ISSUE_TIMESTAMP);
                        if (timestamp != null) {
                            post.setTimestamp(timestamp);
                        }

                        // Get likes/dislikes
                        Object likedByObj = documentSnapshot.get("likedBy");
                        if (likedByObj instanceof List) {
                            post.setLikedBy((List<String>) likedByObj);
                        }
                        Object dislikedByObj = documentSnapshot.get("dislikedBy");
                        if (dislikedByObj instanceof List) {
                            post.setDislikedBy((List<String>) dislikedByObj);
                        }

                        displayPost();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error - maybe show a toast
                    finish();
                });
    }

    private void displayPost() {
        // Set title
        postTitle.setText(post.getTitle());

        // Set category
        String category = post.getCategory();
        if (category != null) {
            categoryChip.setText(capitalizeFirst(category));
            categoryChip.setChipBackgroundColorResource(getCategoryColor(category));
        }

        // Set status
        String status = post.getStatus();
        if (status != null) {
            statusBadge.setText(capitalizeFirst(status.replace("_", " ")));
            statusBadge.setTextColor(getColor(getStatusColor(status)));
        }

        // Set description (full text, no limit)
        postDescription.setText(post.getDescription());

        // Set location
        postLocation.setText(post.getLocation());

        // Set time
        postTime.setText(post.getTimeAgo());

        // Set likes and dislikes
        int likes = post.getLikedBy() != null ? post.getLikedBy().size() : 0;
        int dislikes = post.getDislikedBy() != null ? post.getDislikedBy().size() : 0;
        
        if (tvLikeCountDetail != null) tvLikeCountDetail.setText(String.valueOf(likes));
        if (tvDislikeCountDetail != null) tvDislikeCountDetail.setText(String.valueOf(dislikes));

        // Setup images
        if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
            imageAdapter.setImageUrls(post.getImageUrls());

            // Show indicator if multiple images
            if (post.getImageUrls().size() > 1) {
                imageIndicator.setVisibility(View.VISIBLE);
                updateImageIndicator(0);

                // Update indicator on page change
                imagesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        updateImageIndicator(position);
                    }
                });
            }
        }
    }

    private void updateImageIndicator(int position) {
        int total = post.getImageUrls() != null ? post.getImageUrls().size() : 0;
        imageIndicator.setText((position + 1) + " / " + total);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private int getCategoryColor(String category) {
        switch (category.toLowerCase()) {
            case "road":
                return android.R.color.holo_orange_dark;
            case "water":
                return android.R.color.holo_blue_dark;
            case "electricity":
                return android.R.color.holo_orange_light;
            case "sanitation":
                return android.R.color.holo_green_dark;
            default:
                return android.R.color.darker_gray;
        }
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return android.R.color.holo_orange_dark;
            case "in_progress":
                return android.R.color.holo_blue_dark;
            case "resolved":
                return android.R.color.holo_green_dark;
            case "rejected":
                return android.R.color.holo_red_dark;
            default:
                return android.R.color.darker_gray;
        }
    }
}
