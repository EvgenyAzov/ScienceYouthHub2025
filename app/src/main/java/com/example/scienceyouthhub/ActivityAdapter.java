package com.example.scienceyouthhub;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    public interface OnActivityActionListener {
        void onEdit(ActivityModel activity);
        void onDelete(ActivityModel activity);
    }

    private List<ActivityModel> activities;
    private final OnActivityActionListener listener;
    private final String currentUserId;
    private final String userRole;
    private List<String> myActivities; // list of student's enrolled activities

    private Context context;

    public ActivityAdapter(List<ActivityModel> activities, Context context, OnActivityActionListener listener) {
        this.activities = activities;
        this.context = context;
        this.listener = listener;
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        this.userRole = prefs.getString("user_role", "");
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        // myActivities should be set from the fragment!
    }

    public void setActivities(List<ActivityModel> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    public void setMyActivities(List<String> myActivities) {
        this.myActivities = myActivities;
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

        // --- Logic for showing buttons based on role ---
        boolean canEdit = false;
        if ("Admin".equals(userRole)) {
            canEdit = true;
        } else if ("Instructor".equals(userRole) && TextUtils.equals(activity.getInstructorId(), currentUserId)) {
            canEdit = true;
        }
        holder.editBtn.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        holder.deleteBtn.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        if (canEdit) {
            holder.editBtn.setOnClickListener(v -> listener.onEdit(activity));
            holder.deleteBtn.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Delete activity")
                        .setMessage("Are you sure you want to delete this activity?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            listener.onDelete(activity);
                            Snackbar.make(holder.itemView, "Activity deleted", Snackbar.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            holder.editBtn.setOnClickListener(null);
            holder.deleteBtn.setOnClickListener(null);
        }

        // ======= "Join" BUTTON for STUDENT =======
        if ("Student".equals(userRole)) {
            holder.joinBtn.setVisibility(View.VISIBLE);

            boolean alreadyJoined = myActivities != null && myActivities.contains(activity.getId());
            holder.joinBtn.setEnabled(!alreadyJoined);
            holder.joinBtn.setText(alreadyJoined ? "Enrolled" : "Join");

            holder.joinBtn.setOnClickListener(null);

            if (!alreadyJoined) {
                holder.joinBtn.setOnClickListener(v -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").document(currentUserId)
                            .update("myActivities", com.google.firebase.firestore.FieldValue.arrayUnion(activity.getId()))
                            .addOnSuccessListener(aVoid -> {
                                if (myActivities != null) myActivities.add(activity.getId());
                                notifyItemChanged(holder.getAdapterPosition());
                                Snackbar.make(holder.itemView, "You have successfully joined the activity!", Snackbar.LENGTH_SHORT).show();
                            });
                });
            }
        } else {
            holder.joinBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return activities == null ? 0 : activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, ageRange, instructor;
        ImageButton editBtn, deleteBtn;
        Button joinBtn;

        ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.activityNameText);
            category = itemView.findViewById(R.id.activityCategoryText);
            ageRange = itemView.findViewById(R.id.activityAgeRangeText);
            instructor = itemView.findViewById(R.id.activityInstructorText);
            editBtn = itemView.findViewById(R.id.editActivityBtn);
            deleteBtn = itemView.findViewById(R.id.deleteActivityBtn);
            joinBtn = itemView.findViewById(R.id.joinActivityBtn); // Should exist in item_activity.xml
        }
    }
}
