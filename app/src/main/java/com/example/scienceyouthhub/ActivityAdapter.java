package com.example.scienceyouthhub;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    public interface OnActionListener {
        void onEdit(ActivityModel activity);
        void onDelete(ActivityModel activity);
    }

    private List<ActivityModel> activities;
    private final OnActionListener listener;

    public ActivityAdapter(List<ActivityModel> activities, OnActionListener listener) {
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel model = activities.get(position);
        holder.name.setText(model.getName());
        holder.category.setText("Category: " + model.getCategory());
        holder.subcategory.setText("Subcategory: " + model.getSubcategory());
        holder.ageRange.setText("Ages: " + model.getAgeRange());
        holder.instructor.setText("Instructor: " + model.getInstructorName());

        // Дата
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String dateRange = "";
        if (model.getStartDate() != null && model.getEndDate() != null) {
            dateRange = fmt.format(model.getStartDate()) + " - " + fmt.format(model.getEndDate());
        }
        holder.date.setText("Dates: " + dateRange);

        // Дни недели (если есть)
        if (model.getDaysOfWeek() != null && !model.getDaysOfWeek().isEmpty()) {
            holder.daysOfWeek.setText("Days: " + String.join(", ", model.getDaysOfWeek()));
        } else {
            holder.daysOfWeek.setText("");
        }

        // Специальное событие (цвет + блок CRUD)
        if (!model.isApprovedByAdmin()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF6B8")); // Светло-жёлтый
            holder.editBtn.setEnabled(false);
            holder.deleteBtn.setEnabled(false);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.editBtn.setEnabled(true);
            holder.deleteBtn.setEnabled(true);
        }

        holder.editBtn.setOnClickListener(v -> {
            if (model.isApprovedByAdmin() && listener != null) listener.onEdit(model);
        });
        holder.deleteBtn.setOnClickListener(v -> {
            if (model.isApprovedByAdmin() && listener != null) listener.onDelete(model);
        });
    }

    @Override
    public int getItemCount() {
        return activities == null ? 0 : activities.size();
    }

    public interface OnActivityActionListener {
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, subcategory, ageRange, instructor, date, daysOfWeek;
        ImageButton editBtn, deleteBtn;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.activityNameText);
            category = itemView.findViewById(R.id.activityCategoryText);
            subcategory = itemView.findViewById(R.id.activitySubcategoryText);
            ageRange = itemView.findViewById(R.id.activityAgeRangeText);
            instructor = itemView.findViewById(R.id.activityInstructorText);
            date = itemView.findViewById(R.id.activityDateText);
            daysOfWeek = itemView.findViewById(R.id.activityDaysOfWeekText);
            editBtn = itemView.findViewById(R.id.editActivityBtn);
            deleteBtn = itemView.findViewById(R.id.deleteActivityBtn);
        }
    }
}
