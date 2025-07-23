package com.example.scienceyouthhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class AllStudentsAdapter extends RecyclerView.Adapter<AllStudentsAdapter.StudentViewHolder> {
    private final List<UserModel> students;
    private final Set<String> myChildrenIds;
    private final OnChildActionListener listener;

    public interface OnChildActionListener {
        void onAdd(UserModel student);
        void onRemove(UserModel student);
    }

    public AllStudentsAdapter(List<UserModel> students, Set<String> myChildrenIds, OnChildActionListener listener) {
        this.students = students;
        this.myChildrenIds = myChildrenIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child, parent, false);
        return new StudentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int pos) {
        UserModel student = students.get(pos);
        holder.name.setText(student.getName());
        holder.age.setText("Age: " + student.getAge());

        boolean isAdded = myChildrenIds.contains(student.getId());
        holder.actionBtn.setText(isAdded ? "Remove" : "Add");

        holder.actionBtn.setOnClickListener(v -> {
            if (isAdded) listener.onRemove(student);
            else listener.onAdd(student);
        });
    }

    @Override
    public int getItemCount() { return students.size(); }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView name, age;
        Button actionBtn;
        StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.childNameText);
            age = itemView.findViewById(R.id.childAgeText);
            actionBtn = itemView.findViewById(R.id.actionBtn); // Make sure Button id in item_child.xml is actionBtn
        }
    }
}
