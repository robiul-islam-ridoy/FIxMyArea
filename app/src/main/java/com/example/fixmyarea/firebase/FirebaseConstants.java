package com.example.fixmyarea.firebase;

/**
 * Constants for Firebase operations
 */
public class FirebaseConstants {

    // Firestore Collection Names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_ISSUES = "issues";
    public static final String COLLECTION_COMMENTS = "comments";
    public static final String COLLECTION_AREAS = "areas";

    // Firestore Field Names - Users
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_USER_NAME = "userName";
    public static final String FIELD_USER_EMAIL = "email";
    public static final String FIELD_USER_PHONE = "phone";
    public static final String FIELD_USER_NID = "nid"; // National ID / Student ID
    public static final String FIELD_USER_PROFILE_IMAGE = "profileImageUrl"; // Profile image URL
    public static final String FIELD_USER_ROLE = "role"; // User role (admin/user)
    public static final String FIELD_USER_CREATED_AT = "createdAt";

    // User Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    // Firestore Field Names - Issues
    public static final String FIELD_ISSUE_ID = "issueId";
    public static final String FIELD_ISSUE_TITLE = "title";
    public static final String FIELD_ISSUE_DESCRIPTION = "description";
    public static final String FIELD_ISSUE_CATEGORY = "category";
    public static final String FIELD_ISSUE_STATUS = "status";
    public static final String FIELD_ISSUE_LOCATION = "location";
    public static final String FIELD_ISSUE_LATITUDE = "latitude";
    public static final String FIELD_ISSUE_LONGITUDE = "longitude";
    public static final String FIELD_ISSUE_IMAGE_URL = "imageUrl";
    public static final String FIELD_ISSUE_REPORTER_ID = "reporterId";
    public static final String FIELD_ISSUE_TIMESTAMP = "timestamp";
    public static final String FIELD_ISSUE_UPVOTES = "upvotes";

    // Storage Paths
    public static final String STORAGE_ISSUE_IMAGES = "issue_images/";
    public static final String STORAGE_PROFILE_IMAGES = "profile_images/";

    // Issue Status
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_RESOLVED = "resolved";
    public static final String STATUS_REJECTED = "rejected";

    // Issue Categories
    public static final String CATEGORY_ROAD = "road";
    public static final String CATEGORY_WATER = "water";
    public static final String CATEGORY_ELECTRICITY = "electricity";
    public static final String CATEGORY_SANITATION = "sanitation";
    public static final String CATEGORY_OTHER = "other";

    // Error Messages
    public static final String ERROR_AUTHENTICATION_FAILED = "Authentication failed";
    public static final String ERROR_NETWORK = "Network error occurred";
    public static final String ERROR_UPLOAD_FAILED = "Upload failed";
    public static final String ERROR_PERMISSION_DENIED = "Permission denied";

    private FirebaseConstants() {
        // Private constructor to prevent instantiation
    }
}
