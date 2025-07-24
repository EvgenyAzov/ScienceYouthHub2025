package com.example.scienceyouthhub;

import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ParentChildAdapter extends RecyclerView.Adapter<ParentChildAdapter.ChildViewHolder> {

    private List<UserModel> children;
    private OnAddToActivityListener addToActivityListener;

    // Интерфейс для коллбэка
    public interface OnAddToActivityListener {
        void onAddToActivity(UserModel child);
    }

    public ParentChildAdapter(List<UserModel> children, OnAddToActivityListener listener) {
        this.children = children;
        this.addToActivityListener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_to_parent, parent, false);
        return new ChildViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        UserModel child = children.get(position);
        holder.nameText.setText(child.getName());
        holder.infoText.setText("Возраст: " + child.getAge() + " | ID: " + child.getId());

        holder.addToActivityBtn.setOnClickListener(v -> {
            if (addToActivityListener != null) {
                addToActivityListener.onAddToActivity(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, infoText;
        Button addToActivityBtn;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.childNameText);
            infoText = itemView.findViewById(R.id.childInfoText);
            addToActivityBtn = itemView.findViewById(R.id.addToActivityBtn);
        }
    }
}
