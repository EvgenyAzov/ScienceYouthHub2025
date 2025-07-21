package com.example.scienceyouthhub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onEdit(UserModel user);
        void onDelete(UserModel user);
    }

    private List<UserModel> users;
    private final OnUserActionListener listener;
    private final String userRole;

    public UserAdapter(List<UserModel> users, Context context, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        this.userRole = prefs.getString("user_role", "Student");
    }

    public void setUsers(List<UserModel> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = users.get(position);
        holder.name.setText(user.getName());
        holder.role.setText(user.getType());
        holder.age.setText(String.valueOf(user.getAge()));

        // Показывать кнопки только для admin
        if ("Admin".equals(userRole)) {
            holder.editBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setVisibility(View.VISIBLE);

            holder.editBtn.setOnClickListener(v -> listener.onEdit(user));

            holder.deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Удалить пользователя")
                        .setMessage("Вы действительно хотите удалить этого пользователя?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            listener.onDelete(user);
                            Snackbar.make(holder.itemView, "Пользователь удалён", Snackbar.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
        } else {
            holder.editBtn.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, age, role;
        ImageButton editBtn, deleteBtn;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userNameText);
            age = itemView.findViewById(R.id.userAgeText);
            role = itemView.findViewById(R.id.userRoleText);
            editBtn = itemView.findViewById(R.id.editUserBtn);
            deleteBtn = itemView.findViewById(R.id.deleteUserBtn);
        }
    }
}
