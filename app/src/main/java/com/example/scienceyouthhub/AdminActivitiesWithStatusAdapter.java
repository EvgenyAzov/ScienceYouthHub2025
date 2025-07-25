package com.example.scienceyouthhub;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class AdminActivitiesWithStatusAdapter extends RecyclerView.Adapter<AdminActivitiesWithStatusAdapter.ViewHolder> {
    private List<ActivityModel> activities;
    private static final String[] STATUS_OPTIONS = {"Плохо", "Удовлетворительно", "Отлично"};

    public AdminActivitiesWithStatusAdapter(List<ActivityModel> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_activity_status, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityModel act = activities.get(position);
        holder.activityNameTextView.setText(act.getName());
        holder.categoryTextView.setText("Категория: " + act.getCategory());
        holder.instructorTextView.setText("Инструктор: " + act.getInstructorName());

        // Статус
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                STATUS_OPTIONS
        );
        holder.statusSpinner.setAdapter(spinnerAdapter);

        // Проставляем текущее значение статуса, если есть
        int selIndex = 0;
        if (act.getStatus() != null) {
            for (int i = 0; i < STATUS_OPTIONS.length; i++) {
                if (STATUS_OPTIONS[i].equals(act.getStatus())) {
                    selIndex = i;
                    break;
                }
            }
        }
        holder.statusSpinner.setSelection(selIndex);

        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstCall = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (firstCall) { firstCall = false; return; }
                String newStatus = STATUS_OPTIONS[pos];
                FirebaseFirestore.getInstance().collection("activities")
                        .document(act.getId())
                        .update("status", newStatus)
                        .addOnSuccessListener(aVoid -> {
                            act.setStatus(newStatus);
                            Toast.makeText(holder.itemView.getContext(), "Статус обновлён!", Toast.LENGTH_SHORT).show();
                        });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public int getItemCount() {
        return activities == null ? 0 : activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityNameTextView, categoryTextView, instructorTextView, statusLabel;
        Spinner statusSpinner;
        ViewHolder(View v) {
            super(v);
            activityNameTextView = v.findViewById(R.id.activityNameTextView);
            categoryTextView = v.findViewById(R.id.categoryTextView);
            instructorTextView = v.findViewById(R.id.instructorTextView);
            statusLabel = v.findViewById(R.id.statusLabel);
            statusSpinner = v.findViewById(R.id.statusSpinner);
        }
    }
}
