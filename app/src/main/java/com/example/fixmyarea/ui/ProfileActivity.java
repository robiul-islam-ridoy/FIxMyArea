package com.example.fixmyarea.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fixmyarea.R;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Profile Activity for viewing and editing user profile
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int IMAGE_PICK_CODE = 1002;

    private ImageView backButton;
    private ImageView profileImageView;
    private ImageView editImageButton;
    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private EditText nidInput;
    private Button saveButton;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;
    private String userId;
    private Uri selectedImageUri = null;
    private String currentProfileImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Manager
        firebaseManager = FirebaseManager.getInstance();

        // Initialize views
        backButton = findViewById(R.id.backButton);
        profileImageView = findViewById(R.id.profileImageView);
        editImageButton = findViewById(R.id.editImageButton);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        nidInput = findViewById(R.id.nidInput);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        backButton.setOnClickListener(v -> finish());
        editImageButton.setOnClickListener(v -> pickImage());
        saveButton.setOnClickListener(v -> saveProfile());

        // Load user profile data
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            showProgress(true);

            // Fetch user profile from Firestore
            firebaseManager.getDocument(FirebaseConstants.COLLECTION_USERS, userId)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get user data
                            String name = documentSnapshot.getString(FirebaseConstants.FIELD_USER_NAME);
                            String email = documentSnapshot.getString(FirebaseConstants.FIELD_USER_EMAIL);
                            String phone = documentSnapshot.getString(FirebaseConstants.FIELD_USER_PHONE);
                            String nid = documentSnapshot.getString(FirebaseConstants.FIELD_USER_NID);
                            currentProfileImageUrl = documentSnapshot.getString(
                                    FirebaseConstants.FIELD_USER_PROFILE_IMAGE);

                            // Populate UI fields
                            nameInput.setText(name);
                            emailInput.setText(email);
                            phoneInput.setText(phone);
                            nidInput.setText(nid);

                            // Load profile image
                            loadProfileImage(currentProfileImageUrl);

                            showProgress(false);
                            Log.d(TAG, "User profile loaded successfully");
                        } else {
                            showProgress(false);
                            Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        showProgress(false);
                        Log.e(TAG, "Error loading user profile", e);
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void pickImage() {
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null) {
            selectedImageUri = data.getData();

            // Display selected image
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(profileImageView);
        }
    }

    private void saveProfile() {
        // Get input values
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String nid = nidInput.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(name, phone, nid)) {
            return;
        }

        showProgress(true);

        // Check if image was changed
        if (selectedImageUri != null) {
            // Upload new image first
            uploadImageAndSave(name, phone, nid);
        } else {
            // Save without changing image
            updateUserProfile(name, phone, nid, currentProfileImageUrl);
        }
    }

    private void uploadImageAndSave(String name, String phone, String nid) {
        // Upload to Cloudinary
        com.example.fixmyarea.utils.CloudinaryUploader.uploadImage(this, selectedImageUri, "profile_images",
                new com.example.fixmyarea.utils.CloudinaryUploader.UploadCallback() {

                    @Override
                    public void onSuccess(String imageUrl) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Image uploaded to Cloudinary: " + imageUrl);
                            updateUserProfile(name, phone, nid, imageUrl);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            showProgress(false);
                            Toast.makeText(ProfileActivity.this,
                                    "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                            // Continue with old image
                            updateUserProfile(name, phone, nid, currentProfileImageUrl);
                        });
                    }

                    @Override
                    public void onProgress(int progress) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Upload progress: " + progress + "%");
                        });
                    }
                });
    }

    private void updateUserProfile(String name, String phone, String nid, String profileImageUrl) {
        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put(FirebaseConstants.FIELD_USER_NAME, name);
        updates.put(FirebaseConstants.FIELD_USER_PHONE, phone);
        updates.put(FirebaseConstants.FIELD_USER_NID, nid);
        updates.put(FirebaseConstants.FIELD_USER_PROFILE_IMAGE, profileImageUrl);

        // Update Firestore document
        firebaseManager.updateDocument(FirebaseConstants.COLLECTION_USERS, userId, updates)
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    // Update current profile image URL
                    currentProfileImageUrl = profileImageUrl;
                    selectedImageUri = null;

                    // Return to previous screen
                    finish();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Log.e(TAG, "Error updating profile", e);
                    Toast.makeText(this, "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs(String name, String phone, String nid) {
        // Validate name
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return false;
        }

        if (name.length() < 3) {
            nameInput.setError("Name must be at least 3 characters");
            nameInput.requestFocus();
            return false;
        }

        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return false;
        }

        if (phone.length() < 10) {
            phoneInput.setError("Please enter a valid phone number");
            phoneInput.requestFocus();
            return false;
        }

        // Validate NID
        if (TextUtils.isEmpty(nid)) {
            nidInput.setError("NID/Student ID is required");
            nidInput.requestFocus();
            return false;
        }

        if (nid.length() < 5) {
            nidInput.setError("Please enter a valid NID/Student ID");
            nidInput.requestFocus();
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!show);
        editImageButton.setEnabled(!show);
        nameInput.setEnabled(!show);
        phoneInput.setEnabled(!show);
        nidInput.setEnabled(!show);
    }
}
