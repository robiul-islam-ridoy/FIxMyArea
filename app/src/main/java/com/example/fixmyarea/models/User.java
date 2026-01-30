package com.example.fixmyarea.models;

/**
 * Model class representing a user in the system
 */
public class User {
    private String userId;
    private String userName;
    private String email;
    private String phone;
    private String nid;
    private String profileImageUrl;
    private String role; // "admin" or "user"
    private long createdAt;

    // Required empty constructor for Firestore
    public User() {
    }

    public User(String userId, String userName, String email, String phone,
            String nid, String profileImageUrl, String role, long createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.nid = nid;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Check if user is an admin
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    /**
     * Get formatted role display text
     */
    public String getRoleDisplayName() {
        if (isAdmin()) {
            return "Administrator";
        }
        return "User";
    }
}
