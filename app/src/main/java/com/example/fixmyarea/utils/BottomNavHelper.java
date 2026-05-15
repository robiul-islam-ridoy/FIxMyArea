package com.example.fixmyarea.utils;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.fixmyarea.R;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.ui.CreatePostActivity;
import com.example.fixmyarea.ui.DashboardActivity;
import com.example.fixmyarea.ui.DiscoverActivity;
import com.example.fixmyarea.ui.NotificationsActivity;
import com.example.fixmyarea.ui.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

/**
 * Shared bottom navigation for main app tabs.
 */
public final class BottomNavHelper {

    // Static reference keeps target alive, preventing Glide from cancelling mid-load
    private static CustomTarget<Bitmap> glideTarget;

    private BottomNavHelper() {
    }

    public static void setup(AppCompatActivity activity, BottomNavigationView bottomNav, int selectedItemId) {
        // Let the nav bar handle active/inactive tinting for all non-photo icons
        // We set it to null only AFTER loading the photo so other icons keep their colour state
        ColorStateList navTint = activity.getResources().getColorStateList(R.color.nav_item_color_state, activity.getTheme());
        bottomNav.setItemIconTintList(navTint);
        bottomNav.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
        bottomNav.setOnItemSelectedListener(item -> handleItemSelected(activity, item.getItemId()));
        syncTabState(activity, bottomNav, selectedItemId);
    }

    public static void syncTabState(AppCompatActivity activity, BottomNavigationView bottomNav, int selectedItemId) {
        bottomNav.setSelectedItemId(selectedItemId);
        loadProfileAvatar(activity, bottomNav);
    }

    private static boolean handleItemSelected(AppCompatActivity activity, int itemId) {
        if (itemId == R.id.nav_home) {
            if (activity instanceof DashboardActivity) return true;
            Intent intent = new Intent(activity, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            return true;
        }
        if (itemId == R.id.nav_discover) {
            if (activity instanceof DiscoverActivity) return true;
            activity.startActivity(new Intent(activity, DiscoverActivity.class));
            return true;
        }
        if (itemId == R.id.nav_create) {
            if (activity instanceof CreatePostActivity) return true;
            activity.startActivity(new Intent(activity, CreatePostActivity.class));
            return true;
        }
        if (itemId == R.id.nav_notifications) {
            if (activity instanceof NotificationsActivity) return true;
            activity.startActivity(new Intent(activity, NotificationsActivity.class));
            return true;
        }
        if (itemId == R.id.nav_profile) {
            if (activity instanceof ProfileActivity) return true;
            activity.startActivity(new Intent(activity, ProfileActivity.class));
            return true;
        }
        return false;
    }

    private static void loadProfileAvatar(AppCompatActivity activity, BottomNavigationView bottomNav) {
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        FirebaseUser currentUser = firebaseManager.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        android.view.MenuItem profileItem = bottomNav.getMenu().findItem(R.id.nav_profile);
        // Always show the vector placeholder first (it is tinted by nav_item_color_state)
        profileItem.setIcon(R.drawable.ic_profile_placeholder);

        firebaseManager.getDocument(FirebaseConstants.COLLECTION_USERS, currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    String profileImageUrl = documentSnapshot.getString(FirebaseConstants.FIELD_USER_PROFILE_IMAGE);
                    if (profileImageUrl == null || profileImageUrl.isEmpty()) return;
                    if (activity.isFinishing() || activity.isDestroyed()) return;

                    // Use explicit 96×96 px target so Glide always has a concrete size
                    glideTarget = new CustomTarget<Bitmap>(96, 96) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            if (activity.isFinishing() || activity.isDestroyed()) return;

                            // RoundedBitmapDrawable renders perfectly as a circular nav icon
                            RoundedBitmapDrawable rd = RoundedBitmapDrawableFactory
                                    .create(activity.getResources(), resource);
                            rd.setCircular(true);
                            rd.setAntiAlias(true);

                            activity.runOnUiThread(() -> {
                                // Disable the global tint so the real photo shows in full colour
                                bottomNav.setItemIconTintList(null);
                                profileItem.setIcon(rd);
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    };

                    // Use activity (not applicationContext) so Glide pauses/resumes with lifecycle
                    Glide.with(activity)
                            .asBitmap()
                            .load(profileImageUrl)
                            .override(96, 96)
                            .circleCrop()
                            .into(glideTarget);
                });
    }
}
