package com.example.scienceyouthhub;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class InstructorPhotosFragment extends Fragment {

    private Spinner activitySpinner;
    private Button uploadPhotoBtn;
    private RecyclerView photosRecyclerView;
    private InstructorPhotoAdapter photoAdapter;

    private List<ActivityModel> myActivities = new ArrayList<>();
    private List<InstructorPhotoModel> photos = new ArrayList<>();

    private String currentInstructorId;

    private static final int PICK_IMAGE_REQUEST = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instructor_photos, container, false);

        activitySpinner = view.findViewById(R.id.activitySpinner);
        uploadPhotoBtn = view.findViewById(R.id.uploadPhotoBtn);
        photosRecyclerView = view.findViewById(R.id.photosRecyclerView);

        photosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        photoAdapter = new InstructorPhotoAdapter(photos);
        photosRecyclerView.setAdapter(photoAdapter);

        currentInstructorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadMyActivities();

        uploadPhotoBtn.setOnClickListener(v -> pickImageFromGallery());

        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPhotos();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void loadMyActivities() {
        FirebaseFirestore.getInstance().collection("activities")
                .whereEqualTo("instructorId", currentInstructorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myActivities.clear();
                    List<String> activityNames = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ActivityModel act = doc.toObject(ActivityModel.class);
                        myActivities.add(act);
                        activityNames.add(act.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_dropdown_item, activityNames);
                    activitySpinner.setAdapter(adapter);

                    // Загрузить фото для первой активности, если есть
                    if (!myActivities.isEmpty())
                        loadPhotos();
                });
    }

    private void loadPhotos() {
        int pos = activitySpinner.getSelectedItemPosition();
        if (pos < 0 || myActivities.isEmpty()) {
            photos.clear();
            photoAdapter.notifyDataSetChanged();
            return;
        }
        String activityId = myActivities.get(pos).getId();
        FirebaseFirestore.getInstance().collection("photos")
                .whereEqualTo("activityId", activityId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    photos.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        InstructorPhotoModel p = doc.toObject(InstructorPhotoModel.class);
                        photos.add(p);
                    }
                    photoAdapter.setPhotos(photos);
                });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(data.getData()));
                String base64 = bitmapToBase64(bitmap);

                int pos = activitySpinner.getSelectedItemPosition();
                if (pos < 0 || myActivities.isEmpty()) return;
                String activityId = myActivities.get(pos).getId();

                InstructorPhotoModel newPhoto = new InstructorPhotoModel(UUID.randomUUID().toString(), activityId, currentInstructorId, new Date(), base64);

                FirebaseFirestore.getInstance().collection("photos")
                        .document(newPhoto.getId())
                        .set(newPhoto)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Фото загружено!", Toast.LENGTH_SHORT).show();
                            loadPhotos();
                        });
            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка загрузки фото: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Bitmap -> Base64
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}
