package com.example.fixmyarea.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private String currentUserId;
    private PostActionCallback listener;

    public interface PostActionCallback {
        void onPostClick(Post post);
        void onLikeClick(Post post);
        void onDislikeClick(Post post);
        void onDeleteClick(Post post);
    }

    public PostAdapter(String currentUserId, PostActionCallback listener) {
        this.currentUserId = currentUserId;
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
        private ImageButton btnDelete;
        private LinearLayout btnLike;
        private LinearLayout btnDislike;
        private TextView tvLikeCount;
        private TextView tvDislikeCount;
        private ImageView icLike;
        private ImageView icDislike;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.postImage);
            postTitle = itemView.findViewById(R.id.postTitle);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            postDescription = itemView.findViewById(R.id.postDescription);
            postLocation = itemView.findViewById(R.id.postLocation);
            postTime = itemView.findViewById(R.id.postTime);
            postStatus = itemView.findViewById(R.id.postStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvDislikeCount = itemView.findViewById(R.id.tvDislikeCount);
            icLike = itemView.findViewById(R.id.icLike);
            icDislike = itemView.findViewById(R.id.icDislike);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostClick(posts.get(position));
                }
            });

            btnLike.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLikeClick(posts.get(position));
                }
            });

            btnDislike.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDislikeClick(posts.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(posts.get(position));
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

            // Show/hide delete button
            if (currentUserId != null && currentUserId.equals(post.getReporterId())) {
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                btnDelete.setVisibility(View.GONE);
            }

            // Setup Like/Dislike counts and states
            int likeCount = post.getLikedBy() != null ? post.getLikedBy().size() : 0;
            int dislikeCount = post.getDislikedBy() != null ? post.getDislikedBy().size() : 0;
            
            tvLikeCount.setText(String.valueOf(likeCount));
            tvDislikeCount.setText(String.valueOf(dislikeCount));

            boolean isLiked = post.getLikedBy() != null && post.getLikedBy().contains(currentUserId);
            boolean isDisliked = post.getDislikedBy() != null && post.getDislikedBy().contains(currentUserId);

            if (isLiked) {
                icLike.setColorFilter(itemView.getContext().getColor(R.color.black)); // Or blue
                tvLikeCount.setTextColor(itemView.getContext().getColor(R.color.black));
            } else {
                icLike.setColorFilter(android.graphics.Color.parseColor("#666666"));
                tvLikeCount.setTextColor(android.graphics.Color.parseColor("#666666"));
            }

            if (isDisliked) {
                icDislike.setColorFilter(itemView.getContext().getColor(R.color.black));
                tvDislikeCount.setTextColor(itemView.getContext().getColor(R.color.black));
            } else {
                icDislike.setColorFilter(android.graphics.Color.parseColor("#666666"));
                tvDislikeCount.setTextColor(android.graphics.Color.parseColor("#666666"));
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
