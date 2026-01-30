package com.example.fixmyarea.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fixmyarea.R;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.models.Post;

import java.util.List;

/**
 * Adapter for displaying issues in admin view
 */
public class AdminIssueAdapter extends RecyclerView.Adapter<AdminIssueAdapter.IssueViewHolder> {

    private final List<Post> issueList;
    private final IssueActionListener listener;

    public interface IssueActionListener {
        void onApproveIssue(Post issue);

        void onRejectIssue(Post issue);

        void onMarkAsDone(Post issue);

        void onIssueClick(Post issue);
    }

    public AdminIssueAdapter(List<Post> issueList, IssueActionListener listener) {
        this.issueList = issueList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_issue, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        Post issue = issueList.get(position);
        holder.bind(issue, listener);
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    static class IssueViewHolder extends RecyclerView.ViewHolder {
        private final ImageView issueImage;
        private final TextView issueTitle;
        private final TextView issueDescription;
        private final TextView issueStatus;
        private final TextView issueCategory;
        private final Button btnApprove;
        private final Button btnReject;
        private final Button btnDone;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);
            issueImage = itemView.findViewById(R.id.issueImage);
            issueTitle = itemView.findViewById(R.id.issueTitle);
            issueDescription = itemView.findViewById(R.id.issueDescription);
            issueStatus = itemView.findViewById(R.id.issueStatus);
            issueCategory = itemView.findViewById(R.id.issueCategory);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnDone = itemView.findViewById(R.id.btnDone);
        }

        public void bind(Post issue, IssueActionListener listener) {
            issueTitle.setText(issue.getTitle());
            issueDescription.setText(issue.getDescription());
            issueStatus.setText(issue.getStatus().toUpperCase());
            issueCategory.setText(issue.getCategory());

            // Load issue image
            String imageUrl = issue.getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(issueImage);
            } else {
                issueImage.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Show/hide action buttons based on status
            if (FirebaseConstants.STATUS_PENDING.equals(issue.getStatus())) {
                btnApprove.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                btnDone.setVisibility(View.GONE);
            } else if (FirebaseConstants.STATUS_APPROVED.equals(issue.getStatus()) ||
                    FirebaseConstants.STATUS_IN_PROGRESS.equals(issue.getStatus())) {
                btnApprove.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
                btnDone.setVisibility(View.VISIBLE);
            } else {
                btnApprove.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
                btnDone.setVisibility(View.GONE);
            }

            // Set click listeners for buttons
            btnApprove.setOnClickListener(v -> listener.onApproveIssue(issue));
            btnReject.setOnClickListener(v -> listener.onRejectIssue(issue));
            btnDone.setOnClickListener(v -> listener.onMarkAsDone(issue));

            // Set click listener for the entire card
            itemView.setOnClickListener(v -> listener.onIssueClick(issue));
        }
    }
}
