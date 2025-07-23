package com.example.scienceyouthhub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class PhotosFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1001;

    private RecyclerView photosRecyclerView;
    private PhotoAdapter photoAdapter;
    private List<PhotoModel> photosList = new ArrayList<>();

    private FloatingActionButton addPhotoFab;
    private Uri selectedImageUri;
    private String selectedActivityId;
    private String selectedActivityName;

    private List<String> activityIds = new ArrayList<>();
    private List<String> activityNames = new ArrayList<>();
    private String userRole = "Admin"; // <-- Set dynamically if needed

    public PhotosFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photos, container, false);

        photosRecyclerView = v.findViewById(R.id.photosRecyclerView);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        photoAdapter = new PhotoAdapter(photosList, userRole, photo -> showEditPhotoDialog(photo));
        photosRecyclerView.setAdapter(photoAdapter);

        addPhotoFab = v.findViewById(R.id.addPhotoFab);
        addPhotoFab.setOnClickListener(view -> showAddPhotoDialog());

        loadAllPhotos();

        return v;
    }

    private void loadAllPhotos() {
        photosList.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("activities")
                .get()
                .addOnSuccessListener(activitySnapshots -> {
                    for (QueryDocumentSnapshot activityDoc : activitySnapshots) {
                        String activityId = activityDoc.getId();
                        String activityName = activityDoc.getString("name");
                        db.collection("activities")
                                .document(activityId)
                                .collection("photos")
                                .get()
                                .addOnSuccessListener(photoSnapshots -> {
                                    for (QueryDocumentSnapshot photoDoc : photoSnapshots) {
                                        String photoBase64 = photoDoc.getString("photoBase64");
                                        String userId = photoDoc.getString("userId");
                                        String userName = photoDoc.getString("userName");
                                        String photoId = photoDoc.getId();
                                        if (!TextUtils.isEmpty(photoBase64)) {
                                            photosList.add(new PhotoModel(photoId, photoBase64, activityId, activityName, userId, userName));
                                        }
                                    }
                                    photoAdapter.setPhotos(photosList);
                                });
                    }
                });
    }

    private void showAddPhotoDialog() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("activities")
                .get()
                .addOnSuccessListener(activitySnapshots -> {
                    activityIds.clear();
                    activityNames.clear();
                    for (QueryDocumentSnapshot doc : activitySnapshots) {
                        activityIds.add(doc.getId());
                        activityNames.add(doc.getString("name"));
                    }
                    if (activityIds.isEmpty()) {
                        Toast.makeText(getContext(), "No available activities!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_photo, null, false);
                    Spinner activitySpinner = dialogView.findViewById(R.id.activitySpinner);
                    Button selectPhotoBtn = dialogView.findViewById(R.id.selectPhotoBtn);
                    ImageView previewImage = dialogView.findViewById(R.id.previewImageView);

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, activityNames);
                    activitySpinner.setAdapter(spinnerAdapter);

                    activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedActivityId = activityIds.get(position);
                            selectedActivityName = activityNames.get(position);
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    selectPhotoBtn.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    });

                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Add photo")
                            .setView(dialogView)
                            .setPositiveButton("Save", (d, w) -> {
                                if (selectedImageUri == null) {
                                    Toast.makeText(getContext(), "Please select a photo!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                                    String base64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);

                                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    String userName = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                                    Map<String, Object> data = new HashMap<>();
                                    data.put("photoBase64", base64);
                                    data.put("userId", userId);
                                    data.put("userName", userName);
                                    data.put("activityName", selectedActivityName);
                                    data.put("timestamp", System.currentTimeMillis());

                                    db.collection("activities")
                                            .document(selectedActivityId)
                                            .collection("photos")
                                            .add(data)
                                            .addOnSuccessListener(ref -> {
                                                Toast.makeText(getContext(), "Photo added!", Toast.LENGTH_SHORT).show();
                                                loadAllPhotos();
                                            });

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getContext(), "Photo upload error", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create();
                    dialog.show();
                });
    }

    // Photo edit dialog (you can just replace the photo)
    private void showEditPhotoDialog(PhotoModel photo) {
        // Implement similar to showAddPhotoDialog()
        // You can allow to select a new image and save it over the old one (using .set() instead of .add())
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            // You can show a preview if you add ImageView in the dialog
        }
    }
}
