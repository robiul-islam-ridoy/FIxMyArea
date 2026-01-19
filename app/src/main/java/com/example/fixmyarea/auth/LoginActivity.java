package com.example.fixmyarea.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmyarea.R;
import com.example.fixmyarea.ui.MainActivity;
import com.example.fixmyarea.firebase.FirebaseManager;

/**
 * Login Activity for existing users
 */
public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private TextView forgotPasswordLink;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Manager
        firebaseManager = FirebaseManager.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        progressBar = findViewById(R.id.progressBar);

        // Set button click listeners
        loginButton.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v -> navigateToRegister());
        forgotPasswordLink.setOnClickListener(v -> resetPassword());
    }

    private void loginUser() {
        // Get input values
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Show progress
        showProgress(true);

        // Sign in user
        firebaseManager.signInWithEmail(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Please enter your email");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        showProgress(true);

        firebaseManager.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage()
                                : "Failed to send reset email";
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
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

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        emailInput.setEnabled(!show);
        passwordInput.setEnabled(!show);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
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
