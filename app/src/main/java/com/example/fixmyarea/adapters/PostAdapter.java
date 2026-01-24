package com.example.fixmyarea.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fixmyarea.R;
import com.example.fixmyarea.models.Post;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying posts in RecyclerView
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts = new ArrayList<>();
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public PostAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView postImage;
        private TextView postTitle;
        private Chip categoryChip;
        private TextView postDescription;
        private TextView postLocation;
        private TextView postTime;
        private TextView postStatus;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.postImage);
            postTitle = itemView.findViewById(R.id.postTitle);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            postDescription = itemView.findViewById(R.id.postDescription);
            postLocation = itemView.findViewById(R.id.postLocation);
            postTime = itemView.findViewById(R.id.postTime);
            postStatus = itemView.findViewById(R.id.postStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostClick(posts.get(position));
                }
            });
        }

        public void bind(Post post) {
            // Set title
            postTitle.setText(post.getTitle());

            // Set category
            String category = post.getCategory();
            if (category != null) {
                categoryChip.setText(capitalizeFirst(category));
                categoryChip.setChipBackgroundColorResource(getCategoryColor(category));
            }

            // Set description
            postDescription.setText(post.getDescription());

            // Set location
            postLocation.setText(post.getLocation());

            // Set time
            postTime.setText(post.getTimeAgo());

            // Set status
            String status = post.getStatus();
            if (status != null) {
                postStatus.setText(capitalizeFirst(status.replace("_", " ")));
                postStatus.setTextColor(itemView.getContext().getColor(getStatusColor(status)));
            }

            // Load first image
            String imageUrl = post.getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(postImage);
            } else {
                postImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }

        private int getCategoryColor(String category) {
            switch (category.toLowerCase()) {
                case "road":
                    return android.R.color.holo_orange_dark;
                case "water":
                    return android.R.color.holo_blue_dark;
                case "electricity":
                    return android.R.color.holo_orange_light;
                case "sanitation":
                    return android.R.color.holo_green_dark;
                default:
                    return android.R.color.darker_gray;
            }
        }

        private int getStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return android.R.color.holo_orange_dark;
                case "in_progress":
                    return android.R.color.holo_blue_dark;
                case "resolved":
                    return android.R.color.holo_green_dark;
                case "rejected":
                    return android.R.color.holo_red_dark;
                default:
                    return android.R.color.darker_gray;
            }
        }
    }
}
