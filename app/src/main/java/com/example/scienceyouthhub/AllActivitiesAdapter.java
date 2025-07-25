package com.example.scienceyouthhub;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class AllActivitiesAdapter extends RecyclerView.Adapter<AllActivitiesAdapter.ViewHolder> {
    private List<ActivityModel> activities;

    public AllActivitiesAdapter(List<ActivityModel> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_activity, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityModel act = activities.get(position);
        holder.activityNameTextView.setText(act.getName());
        holder.categoryTextView.setText(
                "Категория: " + act.getCategory() +
                        (act.getSubcategory() != null && !act.getSubcategory().isEmpty() ? " | " + act.getSubcategory() : "")
        );
        holder.instructorTextView.setText("Инструктор: " + (act.getInstructorName() == null ? "—" : act.getInstructorName()));
        holder.ageRangeTextView.setText("Возраст: " + (act.getAgeRange() == null ? "—" : act.getAgeRange()));

        StringBuilder dateInfo = new StringBuilder();
        if (act.getStartDate() != null)
            dateInfo.append("c ").append(android.text.format.DateFormat.format("dd.MM.yyyy", act.getStartDate()));
        if (act.getEndDate() != null)
            dateInfo.append(" по ").append(android.text.format.DateFormat.format("dd.MM.yyyy", act.getEndDate()));
        if (act.getDaysOfWeek() != null && !act.getDaysOfWeek().isEmpty())
            dateInfo.append(" (").append(joinDays(act.getDaysOfWeek())).append(")");
        holder.datesTextView.setText(dateInfo.toString());

        int part = act.getParticipants() == null ? 0 : act.getParticipants().size();
        int max = act.getMaxParticipants();
        holder.participantsTextView.setText(
                "Участников: " + part + (max > 0 ? " / " + max : "")
        );

        holder.descriptionTextView.setText(act.getDescription() == null ? "" : act.getDescription());
    }

    @Override
    public int getItemCount() {
        return activities == null ? 0 : activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityNameTextView, categoryTextView, instructorTextView, ageRangeTextView,
                datesTextView, participantsTextView, descriptionTextView;
        ViewHolder(View v) {
            super(v);
            activityNameTextView = v.findViewById(R.id.activityNameTextView);
            categoryTextView = v.findViewById(R.id.categoryTextView);
            instructorTextView = v.findViewById(R.id.instructorTextView);
            ageRangeTextView = v.findViewById(R.id.ageRangeTextView);
            datesTextView = v.findViewById(R.id.datesTextView);
            participantsTextView = v.findViewById(R.id.participantsTextView);
            descriptionTextView = v.findViewById(R.id.descriptionTextView);
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
