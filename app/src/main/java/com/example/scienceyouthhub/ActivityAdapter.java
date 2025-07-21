package com.example.scienceyouthhub;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    public interface OnActivityActionListener {
        void onEdit(ActivityModel activity);
        void onDelete(ActivityModel activity);
    }

    private List<ActivityModel> activities;
    private final OnActivityActionListener listener;

    public ActivityAdapter(List<ActivityModel> activities, OnActivityActionListener listener) {
        this.activities = activities;
        this.listener = listener;
    }

    public void setActivities(List<ActivityModel> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = activities.get(position);
        holder.name.setText(activity.getName());
        holder.category.setText(activity.getCategory());
        holder.ageRange.setText(activity.getAgeRange());
        holder.instructor.setText(activity.getInstructorName());

        holder.editBtn.setOnClickListener(v -> listener.onEdit(activity));

        holder.deleteBtn.setOnClickListener(v -> {
            // AlertDialog для подтверждения удаления
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Удалить кружок")
                    .setMessage("Вы действительно хотите удалить этот кружок?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        listener.onDelete(activity);
                        Snackbar.make(holder.itemView, "Кружок удалён", Snackbar.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return activities == null ? 0 : activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, ageRange, instructor;
        ImageButton editBtn, deleteBtn;

        ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.activityNameText);
            category = itemView.findViewById(R.id.activityCategoryText);
            ageRange = itemView.findViewById(R.id.activityAgeRangeText);
            instructor = itemView.findViewById(R.id.activityInstructorText);
            editBtn = itemView.findViewById(R.id.editActivityBtn);
            deleteBtn = itemView.findViewById(R.id.deleteActivityBtn);
        }
    }
}
