package com.example.scienceyouthhub;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class ParentFeedbacksAdapter extends RecyclerView.Adapter<ParentFeedbacksAdapter.ViewHolder> {
    private List<FeedbackModel> feedbacks;

    public ParentFeedbacksAdapter(List<FeedbackModel> feedbacks) {
        this.feedbacks = feedbacks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_feedback, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedbackModel f = feedbacks.get(position);
        holder.authorTextView.setText(f.getAuthorName());
        holder.ratingBar.setRating(f.getRating());
        holder.commentTextView.setText(f.getComment());
        if (f.getDate() != null)
            holder.dateTextView.setText(android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", f.getDate()));
        else
            holder.dateTextView.setText("—");
    }

    @Override
    public int getItemCount() {
        return feedbacks == null ? 0 : feedbacks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView authorTextView, commentTextView, dateTextView;
        RatingBar ratingBar;
        ViewHolder(View v) {
            super(v);
            authorTextView = v.findViewById(R.id.parentFeedbackAuthorTextView);
            ratingBar = v.findViewById(R.id.parentFeedbackRatingBar);
            commentTextView = v.findViewById(R.id.parentFeedbackCommentTextView);
            dateTextView = v.findViewById(R.id.parentFeedbackDateTextView);
        }
    }
}
