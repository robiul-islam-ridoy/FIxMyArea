package com.example.fixmyarea;

import android.app.Application;
import com.google.firebase.FirebaseApp;

/**
 * Application class for initializing Firebase and other app-wide configurations
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}
