package com.example.fixmyarea.firebase;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class to manage Firebase operations
 * Provides methods for Authentication, Firestore, and Storage operations
 */
public class FirebaseManager {

    private static FirebaseManager instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // ==================== AUTHENTICATION METHODS ====================

    /**
     * Sign up a new user with email and password
     */
    public Task<AuthResult> signUpWithEmail(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Sign in existing user with email and password
     */
    public Task<AuthResult> signInWithEmail(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Sign out current user
     */
    public void signOut() {
        auth.signOut();
    }

    /**
     * Get current logged-in user
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Send password reset email
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // ==================== FIRESTORE METHODS ====================

    /**
     * Add a document to a collection
     */
    public Task<Void> addDocument(String collection, String documentId, Map<String, Object> data) {
        return firestore.collection(collection).document(documentId).set(data);
    }

    /**
     * Add a document with auto-generated ID
     */
    public Task<com.google.firebase.firestore.DocumentReference> addDocumentAutoId(String collection,
            Map<String, Object> data) {
        return firestore.collection(collection).add(data);
    }

    /**
     * Get a single document
     */
    public Task<DocumentSnapshot> getDocument(String collection, String documentId) {
        return firestore.collection(collection).document(documentId).get();
    }

    /**
     * Get all documents from a collection
     */
    public Task<QuerySnapshot> getAllDocuments(String collection) {
        return firestore.collection(collection).get();
    }

    /**
     * Get Firestore instance for complex queries
     */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    /**
     * Update a document
     */
    public Task<Void> updateDocument(String collection, String documentId, Map<String, Object> updates) {
        return firestore.collection(collection).document(documentId).update(updates);
    }

    /**
     * Delete a document
     */
    public Task<Void> deleteDocument(String collection, String documentId) {
        return firestore.collection(collection).document(documentId).delete();
    }

    /**
     * Query documents with a condition
     * Example: queryDocuments("issues", "status", "pending")
     */
    public Task<QuerySnapshot> queryDocuments(String collection, String field, Object value) {
        return firestore.collection(collection).whereEqualTo(field, value).get();
    }

    // ==================== STORAGE METHODS ====================

    /**
     * Upload image to Firebase Storage
     * 
     * @param uri  The local URI of the image
     * @param path The storage path (e.g., "issue_images/image123.jpg")
     * @return UploadTask
     */
    public UploadTask uploadImage(Uri uri, String path) {
        StorageReference storageRef = storage.getReference().child(path);
        return storageRef.putFile(uri);
    }

    /**
     * Get download URL for an uploaded file
     */
    public Task<Uri> getDownloadUrl(String path) {
        StorageReference storageRef = storage.getReference().child(path);
        return storageRef.getDownloadUrl();
    }

    /**
     * Delete a file from storage
     */
    public Task<Void> deleteFile(String path) {
        StorageReference storageRef = storage.getReference().child(path);
        return storageRef.delete();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a user profile in Firestore after registration
     */
    public Task<Void> createUserProfile(String userId, String name, String email, String phone,
            String nid, String profileImageUrl) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put(FirebaseConstants.FIELD_USER_ID, userId);
        userProfile.put(FirebaseConstants.FIELD_USER_NAME, name);
        userProfile.put(FirebaseConstants.FIELD_USER_EMAIL, email);
        userProfile.put(FirebaseConstants.FIELD_USER_PHONE, phone);
        userProfile.put(FirebaseConstants.FIELD_USER_NID, nid);
        userProfile.put(FirebaseConstants.FIELD_USER_PROFILE_IMAGE, profileImageUrl);
        userProfile.put(FirebaseConstants.FIELD_USER_ROLE, FirebaseConstants.ROLE_USER); // Default role
        userProfile.put(FirebaseConstants.FIELD_USER_CREATED_AT, System.currentTimeMillis());

        return addDocument(FirebaseConstants.COLLECTION_USERS, userId, userProfile);
    }

    /**
     * Create an issue report in Firestore
     */
    public Task<com.google.firebase.firestore.DocumentReference> createIssue(
            String title,
            String description,
            String category,
            String location,
            String imageUrl,
            String reporterId) {

        Map<String, Object> issue = new HashMap<>();
        issue.put(FirebaseConstants.FIELD_ISSUE_TITLE, title);
        issue.put(FirebaseConstants.FIELD_ISSUE_DESCRIPTION, description);
        issue.put(FirebaseConstants.FIELD_ISSUE_CATEGORY, category);
        issue.put(FirebaseConstants.FIELD_ISSUE_LOCATION, location);
        issue.put(FirebaseConstants.FIELD_ISSUE_IMAGE_URL, imageUrl);
        issue.put(FirebaseConstants.FIELD_ISSUE_REPORTER_ID, reporterId);
        issue.put(FirebaseConstants.FIELD_ISSUE_STATUS, FirebaseConstants.STATUS_PENDING);
        issue.put(FirebaseConstants.FIELD_ISSUE_TIMESTAMP, System.currentTimeMillis());
        issue.put(FirebaseConstants.FIELD_ISSUE_UPVOTES, 0);

        return addDocumentAutoId(FirebaseConstants.COLLECTION_ISSUES, issue);
    }

    /**
     * Get all issues with a specific status
     */
    public Task<QuerySnapshot> getIssuesByStatus(String status) {
        return queryDocuments(FirebaseConstants.COLLECTION_ISSUES, FirebaseConstants.FIELD_ISSUE_STATUS, status);
    }

    /**
     * Get all issues by category
     */
    public Task<QuerySnapshot> getIssuesByCategory(String category) {
        return queryDocuments(FirebaseConstants.COLLECTION_ISSUES, FirebaseConstants.FIELD_ISSUE_CATEGORY, category);
    }

    // ==================== ADMIN METHODS ====================

    /**
     * Get user role from Firestore
     */
    public Task<String> getUserRole(String userId) {
        return getDocument(FirebaseConstants.COLLECTION_USERS, userId)
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        String role = task.getResult().getString(FirebaseConstants.FIELD_USER_ROLE);
                        return role != null ? role : FirebaseConstants.ROLE_USER;
                    }
                    return FirebaseConstants.ROLE_USER;
                });
    }

    /**
     * Get all users (Admin only)
     */
    public Task<QuerySnapshot> getAllUsers() {
        return getAllDocuments(FirebaseConstants.COLLECTION_USERS);
    }

    /**
     * Create a new user account by admin
     */
    public Task<Void> createUserByAdmin(String userId, String name, String email, String phone,
            String nid, String profileImageUrl, String role) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put(FirebaseConstants.FIELD_USER_ID, userId);
        userProfile.put(FirebaseConstants.FIELD_USER_NAME, name);
        userProfile.put(FirebaseConstants.FIELD_USER_EMAIL, email);
        userProfile.put(FirebaseConstants.FIELD_USER_PHONE, phone);
        userProfile.put(FirebaseConstants.FIELD_USER_NID, nid);
        userProfile.put(FirebaseConstants.FIELD_USER_PROFILE_IMAGE, profileImageUrl);
        userProfile.put(FirebaseConstants.FIELD_USER_ROLE, role);
        userProfile.put(FirebaseConstants.FIELD_USER_CREATED_AT, System.currentTimeMillis());

        return addDocument(FirebaseConstants.COLLECTION_USERS, userId, userProfile);
    }

    /**
     * Update user information (Admin only)
     */
    public Task<Void> updateUser(String userId, Map<String, Object> updates) {
        return updateDocument(FirebaseConstants.COLLECTION_USERS, userId, updates);
    }

    /**
     * Delete user data from Firestore (Admin only)
     * Note: Firebase Auth account must be deleted separately
     */
    public Task<Void> deleteUserData(String userId) {
        return deleteDocument(FirebaseConstants.COLLECTION_USERS, userId);
    }

    /**
     * Update issue status (Admin - for approve/reject)
     */
    public Task<Void> updateIssueStatus(String issueId, String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FirebaseConstants.FIELD_ISSUE_STATUS, newStatus);
        updates.put("lastUpdated", System.currentTimeMillis());
        return updateDocument(FirebaseConstants.COLLECTION_ISSUES, issueId, updates);
    }

    /**
     * Get system statistics (Admin only)
     * Returns a map with counts of users, issues, and status breakdowns
     */
    public Task<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Get all users count
        Task<QuerySnapshot> usersTask = getAllUsers();
        // Get all issues
        Task<QuerySnapshot> issuesTask = getAllDocuments(FirebaseConstants.COLLECTION_ISSUES);

        return com.google.android.gms.tasks.Tasks.whenAllSuccess(usersTask, issuesTask)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot usersSnapshot = (QuerySnapshot) task.getResult().get(0);
                        QuerySnapshot issuesSnapshot = (QuerySnapshot) task.getResult().get(1);

                        stats.put("totalUsers", usersSnapshot.size());
                        stats.put("totalIssues", issuesSnapshot.size());

                        // Count issues by status
                        int pending = 0, approved = 0, inProgress = 0, resolved = 0, rejected = 0;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : issuesSnapshot.getDocuments()) {
                            String status = doc.getString(FirebaseConstants.FIELD_ISSUE_STATUS);
                            if (status != null) {
                                switch (status) {
                                    case FirebaseConstants.STATUS_PENDING:
                                        pending++;
                                        break;
                                    case FirebaseConstants.STATUS_APPROVED:
                                        approved++;
                                        break;
                                    case FirebaseConstants.STATUS_IN_PROGRESS:
                                        inProgress++;
                                        break;
                                    case FirebaseConstants.STATUS_RESOLVED:
                                        resolved++;
                                        break;
                                    case FirebaseConstants.STATUS_REJECTED:
                                        rejected++;
                                        break;
                                }
                            }
                        }

                        stats.put("pendingIssues", pending);
                        stats.put("approvedIssues", approved);
                        stats.put("inProgressIssues", inProgress);
                        stats.put("resolvedIssues", resolved);
                        stats.put("rejectedIssues", rejected);

                        // Count by category
                        int road = 0, water = 0, electricity = 0, sanitation = 0, other = 0;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : issuesSnapshot.getDocuments()) {
                            String category = doc.getString(FirebaseConstants.FIELD_ISSUE_CATEGORY);
                            if (category != null) {
                                switch (category) {
                                    case FirebaseConstants.CATEGORY_ROAD:
                                        road++;
                                        break;
                                    case FirebaseConstants.CATEGORY_WATER:
                                        water++;
                                        break;
                                    case FirebaseConstants.CATEGORY_ELECTRICITY:
                                        electricity++;
                                        break;
                                    case FirebaseConstants.CATEGORY_SANITATION:
                                        sanitation++;
                                        break;
                                    case FirebaseConstants.CATEGORY_OTHER:
                                        other++;
                                        break;
                                }
                            }
                        }

                        stats.put("roadIssues", road);
                        stats.put("waterIssues", water);
                        stats.put("electricityIssues", electricity);
                        stats.put("sanitationIssues", sanitation);
                        stats.put("otherIssues", other);
                    }
                    return stats;
                });
    }
}
