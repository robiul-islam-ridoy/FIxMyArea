package com.example.fixmyarea.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmyarea.R;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.utils.CloudinaryUploader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity for creating a new issue post
 */
public class CreatePostActivity extends AppCompatActivity {

    private static final String TAG = "CreatePostActivity";
    private static final int MAX_IMAGES = 5;
    private static final int LOCATION_PICKER_REQUEST = 100;

    // UI Components
    private ImageButton backButton;
    private RecyclerView imagesRecyclerView;
    private MaterialButton addImageButton;
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private AutoCompleteTextView categoryInput;

    // Location UI components
    private RadioGroup locationModeGroup;
    private TextInputLayout locationInputLayout;
    private TextInputEditText locationInput;
    private MaterialButton pickLocationButton;
    private TextView selectedLocationText;

    private MaterialButton submitButton;
    private ProgressBar progressBar;

    // Data
    private List<Uri> selectedImages = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private FirebaseManager firebaseManager;

    // Image Picker
    private ActivityResultLauncher<String> imagePickerLauncher;

    // Location data
    private boolean isManualLocationMode = true;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();

        // Initialize views
        initializeViews();

        // Setup image picker
        setupImagePicker();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup category dropdown
        setupCategoryDropdown();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        addImageButton = findViewById(R.id.addImageButton);
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        categoryInput = findViewById(R.id.categoryInput);

        // Location components
        locationModeGroup = findViewById(R.id.locationModeGroup);
        locationInputLayout = findViewById(R.id.locationInputLayout);
        locationInput = findViewById(R.id.locationInput);
        pickLocationButton = findViewById(R.id.pickLocationButton);
        selectedLocationText = findViewById(R.id.selectedLocationText);

        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        // Add selected images up to max limit
                        int availableSlots = MAX_IMAGES - selectedImages.size();
                        int imagesToAdd = Math.min(uris.size(), availableSlots);

                        for (int i = 0; i < imagesToAdd; i++) {
                            selectedImages.add(uris.get(i));
                        }

                        imageAdapter.notifyDataSetChanged();

                        if (uris.size() > availableSlots) {
                            Toast.makeText(this,
                                    "Only " + imagesToAdd + " images added. Maximum " + MAX_IMAGES + " images allowed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        updateAddImageButton();
                    }
                });
    }

    private void setupRecyclerView() {
        imageAdapter = new ImageAdapter(selectedImages, position -> {
            // Remove image at position
            selectedImages.remove(position);
            imageAdapter.notifyItemRemoved(position);
            imageAdapter.notifyItemRangeChanged(position, selectedImages.size());
            updateAddImageButton();
        });

        imagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imagesRecyclerView.setAdapter(imageAdapter);
    }

    private void setupCategoryDropdown() {
        String[] categories = {
                "Road",
                "Water",
                "Electricity",
                "Sanitation",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories);
        categoryInput.setAdapter(adapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        addImageButton.setOnClickListener(v -> {
            if (selectedImages.size() < MAX_IMAGES) {
                imagePickerLauncher.launch("image/*");
            } else {
                Toast.makeText(this, "Maximum " + MAX_IMAGES + " images allowed",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Location mode toggle
        locationModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioManual) {
                isManualLocationMode = true;
                updateLocationUI();
            } else if (checkedId == R.id.radioAutomated) {
                isManualLocationMode = false;
                updateLocationUI();
            }
        });

        // Pick location on map
        pickLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationPickerActivity.class);
            startActivityForResult(intent, LOCATION_PICKER_REQUEST);
        });

        submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void updateAddImageButton() {
        if (selectedImages.size() >= MAX_IMAGES) {
            addImageButton.setEnabled(false);
            addImageButton.setText("Maximum images reached");
        } else {
            addImageButton.setEnabled(true);
            addImageButton.setText("Add Images (" + selectedImages.size() + "/" + MAX_IMAGES + ")");
        }
    }

    private void updateLocationUI() {
        if (isManualLocationMode) {
            // Show manual input, hide map button and preview
            locationInputLayout.setVisibility(View.VISIBLE);
            pickLocationButton.setVisibility(View.GONE);
            selectedLocationText.setVisibility(View.GONE);

            // Clear automated location data
            selectedLatitude = 0.0;
            selectedLongitude = 0.0;
            selectedAddress = "";
        } else {
            // Show map button, hide manual input
            locationInputLayout.setVisibility(View.GONE);
            pickLocationButton.setVisibility(View.VISIBLE);

            // Clear manual input
            if (locationInput != null && locationInput.getText() != null) {
                locationInput.getText().clear();
            }

            // Show preview if location was selected
            if (!selectedAddress.isEmpty()) {
                selectedLocationText.setText(selectedAddress);
                selectedLocationText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void validateAndSubmit() {
        // Get input values
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";
        String category = categoryInput.getText() != null ? categoryInput.getText().toString().trim() : "";
        String location = locationInput.getText() != null ? locationInput.getText().toString().trim() : "";

        // Validate inputs
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Please add at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(title)) {
            titleInput.setError("Title is required");
            titleInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionInput.setError("Description is required");
            descriptionInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(category)) {
            categoryInput.setError("Category is required");
            categoryInput.requestFocus();
            return;
        }

        // Validate location based on mode
        if (isManualLocationMode) {
            if (TextUtils.isEmpty(location)) {
                locationInput.setError("Location is required");
                locationInput.requestFocus();
                return;
            }
        } else {
            // Automated mode - check if location was picked
            if (selectedAddress.isEmpty() || selectedLatitude == 0.0 || selectedLongitude == 0.0) {
                Toast.makeText(this, "Please pick a location on the map", Toast.LENGTH_SHORT).show();
                return;
            }
            // Use selected address from map
            location = selectedAddress;
        }

        // All validation passed, submit the post
        submitPost(title, description, category, location);
    }

    private void submitPost(String title, String description, String category, String location) {
        // Show loading
        setLoading(true);

        // Get current user
        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        String userId = currentUser.getUid();

        // Upload images first
        uploadImages(imageUrls -> {
            if (imageUrls == null || imageUrls.isEmpty()) {
                setLoading(false);
                Toast.makeText(this, "Failed to upload images", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create post data
            Map<String, Object> postData = new HashMap<>();
            postData.put(FirebaseConstants.FIELD_ISSUE_TITLE, title);
            postData.put(FirebaseConstants.FIELD_ISSUE_DESCRIPTION, description);
            postData.put(FirebaseConstants.FIELD_ISSUE_CATEGORY, category.toLowerCase());
            postData.put(FirebaseConstants.FIELD_ISSUE_LOCATION, location);

            // Add coordinates if using automated mode
            if (!isManualLocationMode && selectedLatitude != 0.0 && selectedLongitude != 0.0) {
                postData.put(FirebaseConstants.FIELD_ISSUE_LATITUDE, selectedLatitude);
                postData.put(FirebaseConstants.FIELD_ISSUE_LONGITUDE, selectedLongitude);
            }

            postData.put(FirebaseConstants.FIELD_ISSUE_IMAGE_URL, imageUrls); // Store as array
            postData.put(FirebaseConstants.FIELD_ISSUE_REPORTER_ID, userId);
            postData.put(FirebaseConstants.FIELD_ISSUE_STATUS, FirebaseConstants.STATUS_PENDING);
            postData.put(FirebaseConstants.FIELD_ISSUE_TIMESTAMP, System.currentTimeMillis());
            postData.put(FirebaseConstants.FIELD_ISSUE_UPVOTES, 0);

            // Save to Firestore
            firebaseManager.addDocumentAutoId(FirebaseConstants.COLLECTION_ISSUES, postData)
                    .addOnSuccessListener(documentReference -> {
                        setLoading(false);
                        Toast.makeText(this, "Issue reported successfully!", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Post created with ID: " + documentReference.getId());
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Failed to submit report: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error creating post", e);
                    });
        });
    }

    private void uploadImages(ImageUploadCallback callback) {
        List<String> imageUrls = new ArrayList<>();
        AtomicInteger uploadCounter = new AtomicInteger(0);
        int totalImages = selectedImages.size();

        for (Uri imageUri : selectedImages) {
            CloudinaryUploader.uploadImage(this, imageUri, "issue_images", new CloudinaryUploader.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    imageUrls.add(imageUrl);
                    int completed = uploadCounter.incrementAndGet();

                    runOnUiThread(() -> {
                        int progress = (completed * 100) / totalImages;
                        progressBar.setProgress(progress);
                    });

                    if (completed == totalImages) {
                        runOnUiThread(() -> callback.onComplete(imageUrls));
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Image upload failed: " + error);
                    int completed = uploadCounter.incrementAndGet();

                    if (completed == totalImages) {
                        runOnUiThread(() -> {
                            if (imageUrls.isEmpty()) {
                                callback.onComplete(null);
                            } else {
                                callback.onComplete(imageUrls);
                            }
                        });
                    }
                }

                @Override
                public void onProgress(int progress) {
                    // Individual image progress (optional to show)
                }
            });
        }
    }

    private void setLoading(boolean loading) {
        submitButton.setEnabled(!loading);
        addImageButton.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        if (loading) {
            progressBar.setProgress(0);
            submitButton.setText("Uploading...");
        } else {
            submitButton.setText("Submit Report");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedLatitude = data.getDoubleExtra("latitude", 0.0);
            selectedLongitude = data.getDoubleExtra("longitude", 0.0);
            selectedAddress = data.getStringExtra("address");

            if (selectedAddress != null && !selectedAddress.isEmpty()) {
                selectedLocationText.setText(selectedAddress);
                selectedLocationText.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Location selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Callback interface for image uploads
    private interface ImageUploadCallback {
        void onComplete(List<String> imageUrls);
    }
}
