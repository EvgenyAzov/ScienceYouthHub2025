package com.example.scienceyouthhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<FeedbackModel> feedbacks;
    private String currentUserRole; // передать в адаптере
    private FeedbackActionListener actionListener;

    public interface FeedbackActionListener {
        void onEdit(FeedbackModel feedback);
    }

    public FeedbackAdapter(List<FeedbackModel> feedbacks, String userRole, FeedbackActionListener listener) {
        this.feedbacks = feedbacks;
        this.currentUserRole = userRole;
        this.actionListener = listener;
    }

    public void setFeedbacks(List<FeedbackModel> feedbacks) {
        this.feedbacks = feedbacks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        FeedbackModel feedback = feedbacks.get(position);

        holder.userTextView.setText(feedback.getUserName());
        holder.activityTextView.setText(feedback.getActivityName());
        holder.commentTextView.setText(feedback.getComment());
        holder.ratingBar.setRating(feedback.getRating());

        if ("Admin".equals(currentUserRole)) {
            holder.editBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setVisibility(View.VISIBLE);

            holder.editBtn.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onEdit(feedback);
            });
            holder.deleteBtn.setOnClickListener(v -> {
                FirebaseFirestore.getInstance()
                        .collection("activities")
                        .document(feedback.getActivityId())
                        .collection("feedbacks")
                        .document(feedback.getFeedbackId())
                        .delete();
                feedbacks.remove(position);
                notifyItemRemoved(position);
            });
        } else {
            holder.editBtn.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return feedbacks.size();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView userTextView, activityTextView, commentTextView;
        RatingBar ratingBar;
        ImageButton editBtn, deleteBtn;

        FeedbackViewHolder(View itemView) {
            super(itemView);
            userTextView = itemView.findViewById(R.id.feedbackUser);
            activityTextView = itemView.findViewById(R.id.feedbackActivityName);
            commentTextView = itemView.findViewById(R.id.feedbackComment);
            ratingBar = itemView.findViewById(R.id.feedbackRatingBar);
            editBtn = itemView.findViewById(R.id.editFeedbackBtn);
            deleteBtn = itemView.findViewById(R.id.deleteFeedbackBtn);
        }
    }
}
