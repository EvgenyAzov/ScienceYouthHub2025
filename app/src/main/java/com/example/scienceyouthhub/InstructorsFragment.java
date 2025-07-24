package com.example.scienceyouthhub;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.firestore.*;
import java.util.*;

public class InstructorsFragment extends Fragment {

    private RecyclerView instructorsRecyclerView;
    private InstructorAdapter instructorAdapter;
    private List<UserModel> instructors = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instructors, container, false);

        instructorsRecyclerView = view.findViewById(R.id.instructorsRecyclerView);
        instructorsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        instructorAdapter = new InstructorAdapter(instructors);
        instructorsRecyclerView.setAdapter(instructorAdapter);

        loadInstructors();

        return view;
    }

    private void loadInstructors() {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("type", "Instructor")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    instructors.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null) {
                            instructors.add(user);
                        }
                    }
                    instructorAdapter.notifyDataSetChanged();
                });
    }
}
