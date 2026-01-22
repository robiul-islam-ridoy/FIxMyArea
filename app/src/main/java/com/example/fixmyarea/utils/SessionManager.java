package com.example.fixmyarea.utils;

import android.content.Context;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Single;

/**
 * SessionManager handles user session persistence using DataStore
 * Provides methods to save, retrieve, and clear user session data
 */
public class SessionManager {

    private static final String DATASTORE_NAME = "user_session";

    // Preference keys
    private static final Preferences.Key<String> KEY_USER_ID = PreferencesKeys.stringKey("user_id");
    private static final Preferences.Key<String> KEY_USER_EMAIL = PreferencesKeys.stringKey("user_email");
    private static final Preferences.Key<Long> KEY_LOGIN_TIMESTAMP = PreferencesKeys.longKey("login_timestamp");
    private static final Preferences.Key<Boolean> KEY_IS_LOGGED_IN = PreferencesKeys.booleanKey("is_logged_in");

    private static SessionManager instance;
    private final RxDataStore<Preferences> dataStore;

    private SessionManager(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context.getApplicationContext(), DATASTORE_NAME).build();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    /**
     * Save user session data
     * 
     * @param userId Firebase user ID
     * @param email  User email
     */
    public void saveSession(String userId, String email) {
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(KEY_USER_ID, userId);
            mutablePreferences.set(KEY_USER_EMAIL, email);
            mutablePreferences.set(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());
            mutablePreferences.set(KEY_IS_LOGGED_IN, true);
            return Single.just(mutablePreferences);
        }).blockingSubscribe();
    }

    /**
     * Get user ID from session
     * 
     * @return User ID or null if not logged in
     */
    public String getUserId() {
        return dataStore.data().map(prefs -> prefs.get(KEY_USER_ID)).blockingFirst();
    }

    /**
     * Get user email from session
     * 
     * @return User email or null if not logged in
     */
    public String getUserEmail() {
        return dataStore.data().map(prefs -> prefs.get(KEY_USER_EMAIL)).blockingFirst();
    }

    /**
     * Get login timestamp
     * 
     * @return Timestamp in milliseconds or 0 if not logged in
     */
    public long getLoginTimestamp() {
        Long timestamp = dataStore.data().map(prefs -> prefs.get(KEY_LOGIN_TIMESTAMP)).blockingFirst();
        return timestamp != null ? timestamp : 0L;
    }

    /**
     * Check if user is logged in
     * 
     * @return true if user has an active session
     */
    public boolean isLoggedIn() {
        Boolean isLoggedIn = dataStore.data().map(prefs -> prefs.get(KEY_IS_LOGGED_IN)).blockingFirst();
        return isLoggedIn != null && isLoggedIn;
    }

    /**
     * Clear all session data (logout)
     */
    public void clearSession() {
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.clear();
            return Single.just(mutablePreferences);
        }).blockingSubscribe();
    }

    /**
     * Session data model for easier retrieval
     */
    public static class SessionData {
        private final String userId;
        private final String email;
        private final long loginTimestamp;
        private final boolean isLoggedIn;

        public SessionData(String userId, String email, long loginTimestamp, boolean isLoggedIn) {
            this.userId = userId;
            this.email = email;
            this.loginTimestamp = loginTimestamp;
            this.isLoggedIn = isLoggedIn;
        }

        public String getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public long getLoginTimestamp() {
            return loginTimestamp;
        }

        public boolean isLoggedIn() {
            return isLoggedIn;
        }
    }

    /**
     * Get all session data at once
     * 
     * @return SessionData object
     */
    public SessionData getSessionData() {
        return dataStore.data().map(prefs -> {
            String userId = prefs.get(KEY_USER_ID);
            String email = prefs.get(KEY_USER_EMAIL);
            Long timestamp = prefs.get(KEY_LOGIN_TIMESTAMP);
            Boolean isLoggedIn = prefs.get(KEY_IS_LOGGED_IN);

            return new SessionData(
                    userId,
                    email,
                    timestamp != null ? timestamp : 0L,
                    isLoggedIn != null && isLoggedIn);
        }).blockingFirst();
    }
}
