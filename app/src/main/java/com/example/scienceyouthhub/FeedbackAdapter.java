package com.example.scienceyouthhub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<FeedbackModel> feedbacks;
    private String currentUserRole;
    private String currentUserId;
    private FeedbackActionListener actionListener;

    public interface FeedbackActionListener {
        void onEdit(FeedbackModel feedback);
    }

    public FeedbackAdapter(List<FeedbackModel> feedbacks, String userRole, Context context, FeedbackActionListener listener) {
        this.feedbacks = feedbacks;
        this.currentUserRole = userRole;
        this.actionListener = listener;
        // Получаем текущий userId для сравнения с автором отзыва
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
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
        Context context = holder.itemView.getContext();

        holder.userTextView.setText(feedback.getUserName());
        holder.activityTextView.setText(feedback.getActivityName());
        holder.commentTextView.setText(feedback.getComment());
        holder.ratingBar.setRating(feedback.getRating());

        // --- Логика отображения иконок для разных ролей ---
        boolean canEdit = false;
        boolean canDelete = false;

        if ("Admin".equals(currentUserRole)) {
            canEdit = true;
            canDelete = true;
        } else if (("Student".equals(currentUserRole) || "Parent".equals(currentUserRole))
                && feedback.getUserId() != null
                && feedback.getUserId().equals(currentUserId)) {
            // Можно добавить свою логику для Student/Parent — редактировать и удалять только свои отзывы
            canEdit = true;
            canDelete = true;
        }
        // Instructor — всегда false (только просмотр)

        holder.editBtn.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        holder.deleteBtn.setVisibility(canDelete ? View.VISIBLE : View.GONE);

        holder.editBtn.setOnClickListener(null);
        holder.deleteBtn.setOnClickListener(null);

        if (canEdit && actionListener != null) {
            holder.editBtn.setOnClickListener(v -> actionListener.onEdit(feedback));
        }

        if (canDelete) {
            holder.deleteBtn.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Удалить отзыв")
                        .setMessage("Вы действительно хотите удалить этот отзыв?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            FirebaseFirestore.getInstance()
                                    .collection("activities")
                                    .document(feedback.getActivityId())
                                    .collection("feedbacks")
                                    .document(feedback.getFeedbackId())
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        int pos = holder.getAdapterPosition();
                                        if (pos != RecyclerView.NO_POSITION) {
                                            feedbacks.remove(pos);
                                            notifyItemRemoved(pos);
                                            Snackbar.make(holder.itemView, "Отзыв удалён", Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Snackbar.make(holder.itemView, "Ошибка удаления", Snackbar.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
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
