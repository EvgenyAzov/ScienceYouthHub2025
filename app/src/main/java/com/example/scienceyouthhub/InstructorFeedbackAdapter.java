package com.example.scienceyouthhub;

import android.view.*;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InstructorFeedbackAdapter extends RecyclerView.Adapter<InstructorFeedbackAdapter.FeedbackViewHolder> {

    private List<InstructorFeedbackModel> feedbacks;

    public InstructorFeedbackAdapter(List<InstructorFeedbackModel> feedbacks) {
        this.feedbacks = feedbacks;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instructor_feedback, parent, false);
        return new FeedbackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        InstructorFeedbackModel model = feedbacks.get(position);
        holder.feedbackComment.setText(model.getComment());
        holder.feedbackDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(model.getDate()));
        holder.feedbackScore.setRating(model.getScore());
        holder.studentName.setText("Student: " + model.getStudentId());
    }

    @Override
    public int getItemCount() {
        return feedbacks != null ? feedbacks.size() : 0;
    }

    public void setFeedbacks(List<InstructorFeedbackModel> feedbacks) {
        this.feedbacks = feedbacks;
        notifyDataSetChanged();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, feedbackComment, feedbackDate;
        RatingBar feedbackScore;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.feedbackStudentName);
            feedbackComment = itemView.findViewById(R.id.feedbackComment);
            feedbackDate = itemView.findViewById(R.id.feedbackDate);
            feedbackScore = itemView.findViewById(R.id.feedbackScore);
        }
    }
}
