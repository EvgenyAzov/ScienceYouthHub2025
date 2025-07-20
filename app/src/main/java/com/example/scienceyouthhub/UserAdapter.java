package com.example.scienceyouthhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onEdit(UserModel user);
        void onDelete(UserModel user);
    }

    private List<UserModel> users;
    private final OnUserActionListener listener;

    public UserAdapter(List<UserModel> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
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
        holder.editBtn.setOnClickListener(v -> listener.onEdit(user));
        holder.deleteBtn.setOnClickListener(v -> listener.onDelete(user));
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
