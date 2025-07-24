package com.example.scienceyouthhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.InstructorViewHolder> {

    private List<UserModel> instructors;

    public InstructorAdapter(List<UserModel> instructors) {
        this.instructors = instructors;
    }

    @NonNull
    @Override
    public InstructorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instructor, parent, false);
        return new InstructorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructorViewHolder holder, int position) {
        UserModel instructor = instructors.get(position);
        holder.name.setText(instructor.getName());
        holder.category.setText("Category: " + (instructor.getCategory() != null ? instructor.getCategory() : "-"));
        holder.subcategory.setText("Skill: " + (instructor.getSubcategory() != null ? instructor.getSubcategory() : "-"));
        holder.age.setText("Age: " + instructor.getAge());
    }

    @Override
    public int getItemCount() {
        return instructors != null ? instructors.size() : 0;
    }

    static class InstructorViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, subcategory, age;
        InstructorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.instructorNameText);
            category = itemView.findViewById(R.id.instructorCategoryText);
            subcategory = itemView.findViewById(R.id.instructorSubcategoryText);
            age = itemView.findViewById(R.id.instructorAgeText);
        }
    }
}
