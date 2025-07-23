package com.example.scienceyouthhub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class ChildrenFragment extends Fragment {

    private RecyclerView childrenRecyclerView;
    private ChildrenAdapter childrenAdapter;
    private List<UserModel> childrenList = new ArrayList<>();
    private FloatingActionButton addChildFab;
    private String parentId;
    private FirebaseFirestore db;
    private String parentName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_children, container, false);

        childrenRecyclerView = v.findViewById(R.id.childrenRecyclerView);
        addChildFab = v.findViewById(R.id.addChildFab);

        db = FirebaseFirestore.getInstance();

        // Get parent id (current user)
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        parentName = prefs.getString("user_name", "Parent");

        childrenAdapter = new ChildrenAdapter(childrenList, child -> showRemoveChildDialog(child));
        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        childrenRecyclerView.setAdapter(childrenAdapter);

        addChildFab.setOnClickListener(vv -> showAddChildDialog());

        loadChildren();

        return v;
    }

    private void loadChildren() {
        // Get the list of children from the "children" field in the parent document
        db.collection("users").document(parentId).get()
                .addOnSuccessListener(parentDoc -> {
                    List<String> childIds = (List<String>) parentDoc.get("children");
                    if (childIds == null) childIds = new ArrayList<>();
                    if (childIds.isEmpty()) {
                        childrenList.clear();
                        childrenAdapter.notifyDataSetChanged();
                        return;
                    }
                    // Fetch children's profiles by id
                    db.collection("users").whereIn("id", childIds)
                            .get()
                            .addOnSuccessListener(childSnaps -> {
                                childrenList.clear();
                                for (DocumentSnapshot doc : childSnaps) {
                                    UserModel child = doc.toObject(UserModel.class);
                                    if (child != null) childrenList.add(child);
                                }
                                childrenAdapter.notifyDataSetChanged();
                            });
                });
    }

    private void showAddChildDialog() {
        // Add child dialog by email
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_child, null, false);
        EditText emailInput = dialogView.findViewById(R.id.addChildEmailEditText);

        new AlertDialog.Builder(getContext())
                .setTitle("Add child")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(getContext(), "Enter the child's email!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Find the student by email and add to the parent's "children" array
                    db.collection("users")
                            .whereEqualTo("type", "Student")
                            .whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener(query -> {
                                if (query.isEmpty()) {
                                    Toast.makeText(getContext(), "Student not found!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                DocumentSnapshot studentDoc = query.getDocuments().get(0);
                                String studentId = studentDoc.getId();

                                // Add the id to the parent's "children" array
                                db.collection("users").document(parentId)
                                        .update("children", FieldValue.arrayUnion(studentId))
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Child added!", Toast.LENGTH_SHORT).show();
                                            loadChildren();
                                        });
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRemoveChildDialog(UserModel child) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove child")
                .setMessage("Are you sure you want to remove " + child.getName() + " from the list?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    db.collection("users").document(parentId)
                            .update("children", FieldValue.arrayRemove(child.getId()))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Child removed", Toast.LENGTH_SHORT).show();
                                loadChildren();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- Adapter ---
    private static class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {
        private final List<UserModel> children;
        private final OnRemoveListener removeListener;
        interface OnRemoveListener { void onRemove(UserModel child); }

        public ChildrenAdapter(List<UserModel> children, OnRemoveListener listener) {
            this.children = children;
            this.removeListener = listener;
        }

        @NonNull
        @Override
        public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_child, parent, false);
            return new ChildViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
            UserModel child = children.get(position);
            holder.childName.setText(child.getName());
            holder.childAge.setText("Age: " + child.getAge());
            holder.removeBtn.setOnClickListener(v -> removeListener.onRemove(child));
        }

        @Override
        public int getItemCount() { return children.size(); }

        static class ChildViewHolder extends RecyclerView.ViewHolder {
            TextView childName, childAge;
            ImageButton removeBtn;

            ChildViewHolder(@NonNull View itemView) {
                super(itemView);
                childName = itemView.findViewById(R.id.childNameText);
                childAge = itemView.findViewById(R.id.childAgeText);
                removeBtn = itemView.findViewById(R.id.actionBtn);
            }
        }
    }
}
