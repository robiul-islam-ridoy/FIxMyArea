package com.example.fixmyarea.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fixmyarea.R;
import com.example.fixmyarea.models.User;

import java.util.List;

/**
 * Adapter for displaying users in RecyclerView (Admin)
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;
    private final UserActionListener listener;

    public interface UserActionListener {
        void onEditUser(User user);

        void onDeleteUser(User user);
    }

    public UserAdapter(List<User> userList, UserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImage;
        private final TextView userName;
        private final TextView userEmail;
        private final TextView userRole;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userRole = itemView.findViewById(R.id.userRole);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(User user, UserActionListener listener) {
            userName.setText(user.getUserName());
            userEmail.setText(user.getEmail());
            userRole.setText(user.getRoleDisplayName());

            // Set role color
            if (user.isAdmin()) {
                userRole.setBackgroundResource(R.drawable.bg_role_admin);
            } else {
                userRole.setBackgroundResource(R.drawable.bg_role_user);
            }

            // Load profile image
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getProfileImageUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // Set click listeners
            btnEdit.setOnClickListener(v -> listener.onEditUser(user));
            btnDelete.setOnClickListener(v -> listener.onDeleteUser(user));
        }
    }
}
