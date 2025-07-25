package com.example.scienceyouthhub;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class ParentPhotosFragment extends Fragment {

    private Spinner childSpinner, activitySpinner;
    private RecyclerView photosRecyclerView;
    private PhotoAdapter photoAdapter;

    private List<UserModel> myChildren = new ArrayList<>();
    private List<ActivityModel> childActivities = new ArrayList<>();
    private List<PhotoModel> filteredPhotos = new ArrayList<>();

    private String parentId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_photos, container, false);

        childSpinner = view.findViewById(R.id.childSpinner);
        activitySpinner = view.findViewById(R.id.activitySpinner);
        photosRecyclerView = view.findViewById(R.id.photosRecyclerView);

        photosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        photoAdapter = new PhotoAdapter(filteredPhotos, "Parent", null);
        photosRecyclerView.setAdapter(photoAdapter);

        parentId = getParentId();

        loadMyChildren();

        childSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadChildActivities();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPhotos();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private String getParentId() {
        return requireContext().getSharedPreferences("user_prefs", 0).getString("user_id", "");
    }

    private void loadMyChildren() {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("type", "Student")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myChildren.clear();
                    List<String> names = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        UserModel child = doc.toObject(UserModel.class);
                        if (child != null) {
                            myChildren.add(child);
                            names.add(child.getName());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
                    childSpinner.setAdapter(adapter);

                    if (!myChildren.isEmpty()) {
                        loadChildActivities();
                    } else {
                        childActivities.clear();
                        filteredPhotos.clear();
                        photoAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadChildActivities() {
        int pos = childSpinner.getSelectedItemPosition();
        if (pos < 0 || myChildren.isEmpty()) {
            childActivities.clear();
            filteredPhotos.clear();
            photoAdapter.notifyDataSetChanged();
            return;
        }
        String childId = myChildren.get(pos).getId();

        // Получаем все активности, где этот ребёнок есть в participants
        FirebaseFirestore.getInstance().collection("activities")
                .whereArrayContains("participants", childId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    childActivities.clear();
                    List<String> names = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ActivityModel activity = doc.toObject(ActivityModel.class);
                        if (activity != null) {
                            childActivities.add(activity);
                            names.add(activity.getName());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
                    activitySpinner.setAdapter(adapter);

                    if (!childActivities.isEmpty()) {
                        loadPhotos();
                    } else {
                        filteredPhotos.clear();
                        photoAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadPhotos() {
        int childPos = childSpinner.getSelectedItemPosition();
        int activityPos = activitySpinner.getSelectedItemPosition();

        filteredPhotos.clear();
        if (childPos < 0 || activityPos < 0 || myChildren.isEmpty() || childActivities.isEmpty()) {
            photoAdapter.notifyDataSetChanged();
            return;
        }

        String childId = myChildren.get(childPos).getId();
        String activityId = childActivities.get(activityPos).getId();

        // Сначала ищем активность и проверяем, что ребёнок действительно есть в участниках
        ActivityModel activity = null;
        for (ActivityModel act : childActivities) {
            if (act.getId().equals(activityId)) {
                activity = act;
                break;
            }
        }
        if (activity == null || activity.getParticipants() == null || !activity.getParticipants().contains(childId)) {
            photoAdapter.notifyDataSetChanged();
            return;
        }

        FirebaseFirestore.getInstance().collection("photos")
                .whereEqualTo("activityId", activityId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    filteredPhotos.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        PhotoModel photo = doc.toObject(PhotoModel.class);
                        if (photo != null) {
                            filteredPhotos.add(photo);
                        }
                    }
                    photoAdapter.notifyDataSetChanged();
                });
    }
}
