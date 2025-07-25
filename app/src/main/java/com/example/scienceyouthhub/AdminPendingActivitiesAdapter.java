package com.example.scienceyouthhub;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class AdminPendingActivitiesAdapter extends RecyclerView.Adapter<AdminPendingActivitiesAdapter.ViewHolder> {
    private List<ActivityModel> activities;

    public AdminPendingActivitiesAdapter(List<ActivityModel> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_pending_activity, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityModel act = activities.get(position);
        holder.activityNameTextView.setText(act.getName());
        holder.categoryTextView.setText("Категория: " + act.getCategory());
        String dateStr = "";
        if (act.getStartDate() != null)
            dateStr += "с " + android.text.format.DateFormat.format("dd.MM.yyyy", act.getStartDate());
        if (act.getEndDate() != null)
            dateStr += " по " + android.text.format.DateFormat.format("dd.MM.yyyy", act.getEndDate());
        holder.dateTextView.setText(dateStr);
        holder.instructorTextView.setText("Инструктор: " + act.getInstructorName());

        holder.approveButton.setOnClickListener(v -> {
            Context ctx = holder.itemView.getContext();
            new AlertDialog.Builder(ctx)
                    .setTitle("Подтвердить курс")
                    .setMessage("Вы уверены, что хотите утвердить это мероприятие?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        FirebaseFirestore.getInstance().collection("activities")
                                .document(act.getId())
                                .update("approvedByAdmin", true)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ctx, "Курс утвержден", Toast.LENGTH_SHORT).show();
                                    activities.remove(position);
                                    notifyItemRemoved(position);
                                });
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        holder.cancelButton.setOnClickListener(v -> {
            Context ctx = holder.itemView.getContext();
            new AlertDialog.Builder(ctx)
                    .setTitle("Отклонить курс")
                    .setMessage("Вы уверены, что хотите отклонить/удалить это мероприятие?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        FirebaseFirestore.getInstance().collection("activities")
                                .document(act.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ctx, "Курс удалён", Toast.LENGTH_SHORT).show();
                                    activities.remove(position);
                                    notifyItemRemoved(position);
                                });
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return activities == null ? 0 : activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityNameTextView, categoryTextView, dateTextView, instructorTextView;
        Button approveButton, cancelButton;

        ViewHolder(View v) {
            super(v);
            activityNameTextView = v.findViewById(R.id.activityNameTextView);
            categoryTextView = v.findViewById(R.id.categoryTextView);
            dateTextView = v.findViewById(R.id.dateTextView);
            instructorTextView = v.findViewById(R.id.instructorTextView);
            approveButton = v.findViewById(R.id.approveButton);
            cancelButton = v.findViewById(R.id.cancelButton);
        }
    }
}
