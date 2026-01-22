package com.example.fixmyarea.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmyarea.auth.LoginActivity;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Manager
        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        // Initialize Session Manager
        SessionManager sessionManager = SessionManager.getInstance(this);

        // Check if user is logged in (check both Firebase and DataStore session)
        if (!firebaseManager.isUserLoggedIn() || !sessionManager.isLoggedIn()) {
            // User not logged in, navigate to login page
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // User is logged in, redirect to dashboard
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
