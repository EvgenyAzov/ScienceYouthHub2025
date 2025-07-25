package com.example.scienceyouthhub;

import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class StudentScheduleAdapter extends RecyclerView.Adapter<StudentScheduleAdapter.ViewHolder> {
    private List<ActivityModel> activities;

    public StudentScheduleAdapter(List<ActivityModel> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityModel act = activities.get(position);
        holder.activityNameTextView.setText(act.getName());
        holder.categoryTextView.setText("Категория: " + act.getCategory());
        // Дата и дни недели
        StringBuilder dates = new StringBuilder();
        if (act.getStartDate() != null)
            dates.append("с ").append(android.text.format.DateFormat.format("dd.MM.yyyy", act.getStartDate()));
        if (act.getEndDate() != null)
            dates.append(" по ").append(android.text.format.DateFormat.format("dd.MM.yyyy", act.getEndDate()));
        if (act.getDaysOfWeek() != null && !act.getDaysOfWeek().isEmpty())
            dates.append(" (").append(joinDays(act.getDaysOfWeek())).append(")");
        holder.datesTextView.setText(dates.toString());

        holder.instructorTextView.setText("Инструктор: " + act.getInstructorName());

        // Если занятие уже закончилось — статус “Завершено”
        if (act.getEndDate() != null && act.getEndDate().before(new Date())) {
            holder.statusTextView.setText("Завершено");
            holder.statusTextView.setTextColor(Color.GRAY);
        } else {
            holder.statusTextView.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return activities == null ? 0 : activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityNameTextView, categoryTextView, datesTextView, instructorTextView, statusTextView;

        ViewHolder(View v) {
            super(v);
            activityNameTextView = v.findViewById(R.id.activityNameTextView);
            categoryTextView = v.findViewById(R.id.categoryTextView);
            datesTextView = v.findViewById(R.id.datesTextView);
            instructorTextView = v.findViewById(R.id.instructorTextView);
            statusTextView = v.findViewById(R.id.statusTextView);
        }
    }

    private static String joinDays(List<String> days) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.size(); i++) {
            sb.append(days.get(i));
            if (i < days.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
