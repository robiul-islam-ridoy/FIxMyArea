package com.example.fixmyarea.utils;

import android.content.Context;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.google.android.gms.tasks.Task;

/**
 * Utility class for role-based access control
 */
public class RoleManager {

    private static RoleManager instance;
    private final FirebaseManager firebaseManager;
    private String currentUserRole = null;
    private String currentUserId = null;

    private RoleManager() {
        firebaseManager = FirebaseManager.getInstance();
    }

    public static synchronized RoleManager getInstance() {
        if (instance == null) {
            instance = new RoleManager();
        }
        return instance;
    }

    /**
     * Check if current user is an admin
     * 
     * @param callback Callback with result
     */
    public void isAdmin(RoleCheckCallback callback) {
        if (firebaseManager.getCurrentUser() == null) {
            callback.onResult(false);
            return;
        }

        String userId = firebaseManager.getCurrentUser().getUid();

        // Use cached role if available and same user
        if (currentUserRole != null && userId.equals(currentUserId)) {
            callback.onResult(FirebaseConstants.ROLE_ADMIN.equals(currentUserRole));
            return;
        }

        // Fetch role from Firestore
        firebaseManager.getUserRole(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUserId = userId;
                currentUserRole = task.getResult();
                callback.onResult(FirebaseConstants.ROLE_ADMIN.equals(currentUserRole));
            } else {
                callback.onResult(false);
            }
        });
    }

    /**
     * Get user role with callback
     */
    public void getUserRole(String userId, RoleCallback callback) {
        firebaseManager.getUserRole(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onResult(task.getResult());
            } else {
                callback.onResult(FirebaseConstants.ROLE_USER);
            }
        });
    }

    /**
     * Clear cached role (call on logout)
     */
    public void clearCache() {
        currentUserRole = null;
        currentUserId = null;
    }

    /**
     * Callback interface for role checks
     */
    public interface RoleCheckCallback {
        void onResult(boolean isAdmin);
    }

    /**
     * Callback interface for role retrieval
     */
    public interface RoleCallback {
        void onResult(String role);
    }
}
