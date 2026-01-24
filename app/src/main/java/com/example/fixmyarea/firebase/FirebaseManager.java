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
}
