package com.example.fixmyarea.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fixmyarea.R;
import com.example.fixmyarea.ui.MainActivity;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.utils.SessionManager;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseUser;

/**
 * Registration Activity for new users
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int IMAGE_PICK_CODE = 1001;

    private ImageView profileImageView;
    private Button chooseImageButton;
    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private EditText nidInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;
    private SessionManager sessionManager;

    private Uri selectedImageUri = null;
    private String uploadedImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Manager
        firebaseManager = FirebaseManager.getInstance();

        // Initialize Session Manager
        sessionManager = SessionManager.getInstance(this);

        // Initialize views
        profileImageView = findViewById(R.id.profileImageView);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        nidInput = findViewById(R.id.nidInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        // Set button click listeners
        chooseImageButton.setOnClickListener(v -> pickImage());
        registerButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> navigateToLogin());
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

    private void registerUser() {
        // Get input values
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String nid = nidInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        // Validate inputs
        if (!validateInputs(name, email, phone, nid, password, confirmPassword)) {
            return;
        }

        // Show progress
        showProgress(true);

        // Upload image first (if selected)
        if (selectedImageUri != null) {
            uploadImageAndRegister(name, email, phone, nid, password);
        } else {
            // Register without image
            createUserAccount(name, email, phone, nid, "", password);
        }
    }

    private void uploadImageAndRegister(String name, String email, String phone,
            String nid, String password) {
        // Upload to Cloudinary
        com.example.fixmyarea.utils.CloudinaryUploader.uploadImage(this, selectedImageUri, "profile_images",
                new com.example.fixmyarea.utils.CloudinaryUploader.UploadCallback() {

                    @Override
                    public void onSuccess(String imageUrl) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Image uploaded to Cloudinary: " + imageUrl);
                            createUserAccount(name, email, phone, nid, imageUrl, password);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            showProgress(false);
                            Toast.makeText(RegisterActivity.this,
                                    "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                            // Continue without image
                            createUserAccount(name, email, phone, nid, "", password);
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

    private void createUserAccount(String name, String email, String phone,
            String nid, String profileImageUrl, String password) {
        // Create user account
        firebaseManager.signUpWithEmail(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseManager.getCurrentUser();

                        // Create user profile in Firestore
                        firebaseManager.createUserProfile(
                                user.getUid(),
                                name,
                                email,
                                phone,
                                nid,
                                profileImageUrl).addOnSuccessListener(aVoid -> {
                                    // Save session to DataStore
                                    sessionManager.saveSession(user.getUid(), email);

                                    showProgress(false);
                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                    // Navigate to main activity
                                    navigateToMain();
                                }).addOnFailureListener(e -> {
                                    showProgress(false);
                                    Toast.makeText(this, "Failed to create profile: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        showProgress(false);
                        String error = task.getException() != null ? task.getException().getMessage()
                                : "Registration failed";
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String name, String email, String phone, String nid,
            String password, String confirmPassword) {
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

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
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

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Please confirm your password");
            confirmPasswordInput.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
        chooseImageButton.setEnabled(!show);
        nameInput.setEnabled(!show);
        emailInput.setEnabled(!show);
        phoneInput.setEnabled(!show);
        nidInput.setEnabled(!show);
        passwordInput.setEnabled(!show);
        confirmPasswordInput.setEnabled(!show);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
