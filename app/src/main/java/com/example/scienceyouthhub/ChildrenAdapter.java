package com.example.scienceyouthhub;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {

    private final List<UserModel> children;

    public ChildrenAdapter(List<UserModel> children) {
        this.children = children;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        UserModel child = children.get(position);
        holder.name.setText(child.getName());
        holder.age.setText("Age: " + child.getAge());
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView name, age;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.childNameText);
            age = itemView.findViewById(R.id.childAgeText);
        }
    }
}
