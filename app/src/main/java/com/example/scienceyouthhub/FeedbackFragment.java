package com.example.scienceyouthhub;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

public class FeedbackFragment extends Fragment {
    private RecyclerView recyclerView;
    private FeedbackAdapter adapter;
    private List<FeedbackModel> feedbackList = new ArrayList<>();
    private Map<String, String> userNames = new HashMap<>();
    private FirebaseFirestore db;
    private String activityId;
    private String currentUserId;
    private String currentUserRole = "User";

    // Для фото:
    private List<Uri> selectedImageUris = new ArrayList<>();
    private PhotoPreviewAdapter photoPreviewAdapter;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUris.clear();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedImageUris.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        selectedImageUris.add(result.getData().getData());
                    }
                    photoPreviewAdapter.setUris(selectedImageUris);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        recyclerView = view.findViewById(R.id.feedbackRecyclerView);
        FloatingActionButton addFab = view.findViewById(R.id.addFeedbackFab);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new FeedbackAdapter(feedbackList, userId -> userNames.getOrDefault(userId, "—"), this::onDeleteFeedback, currentUserId, currentUserRole);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Получить activityId из аргументов
        this.activityId = getArguments() != null ? getArguments().getString("activityId") : null;
        if (this.activityId == null) {
            Toast.makeText(getContext(), "Нет кружка", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Узнать роль пользователя
        db.collection("users").document(currentUserId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String type = snapshot.getString("type");
                if (type != null) currentUserRole = type;
                adapter.setUserRole(currentUserRole);
            }
        });

        addFab.setOnClickListener(v -> showAddFeedbackDialog());

        loadUsers();
        loadFeedbacks();
        return view;
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(usersSnapshot -> {
                    userNames.clear();
                    for (DocumentSnapshot doc : usersSnapshot) {
                        userNames.put(doc.getId(), doc.getString("name"));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void loadFeedbacks() {
        db.collection("feedbacks")
                .whereEqualTo("activityId", activityId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    feedbackList.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        FeedbackModel feedback = doc.toObject(FeedbackModel.class);
                        feedbackList.add(feedback);
                    }
                    adapter.setFeedbacks(feedbackList);
                });
    }

    private void showAddFeedbackDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_feedback, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.feedbackRatingBar);
        EditText commentInput = dialogView.findViewById(R.id.feedbackComment);
        Button addPhotoBtn = dialogView.findViewById(R.id.addPhotoBtn);
        RecyclerView photosRecyclerView = dialogView.findViewById(R.id.photosRecyclerView);

        photoPreviewAdapter = new PhotoPreviewAdapter();
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        photosRecyclerView.setAdapter(photoPreviewAdapter);

        addPhotoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Оставить отзыв")
                .setView(dialogView)
                .setPositiveButton("Оставить", (d, w) -> {
                    int score = (int) ratingBar.getRating();
                    String comment = commentInput.getText().toString().trim();
                    if (score == 0 || TextUtils.isEmpty(comment)) {
                        Toast.makeText(getContext(), "Поставьте оценку и напишите отзыв", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    encodeImagesAndSaveFeedback(comment, score);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void encodeImagesAndSaveFeedback(String comment, int score) {
        List<String> photoBase64List = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            String base64 = encodeImageToBase64(requireContext(), uri);
            if (base64 != null) photoBase64List.add(base64);
        }
        saveFeedback(comment, score, photoBase64List);
    }

    private void saveFeedback(String comment, int score, List<String> photoBase64List) {
        String userId = currentUserId;
        String id = db.collection("feedbacks").document().getId();
        FeedbackModel feedback = new FeedbackModel(
                id, activityId, userId, score, comment, System.currentTimeMillis(), photoBase64List);
        db.collection("feedbacks").document(id)
                .set(feedback)
                .addOnSuccessListener(aVoid -> {
                    loadFeedbacks();
                    Toast.makeText(getContext(), "Спасибо за отзыв!", Toast.LENGTH_SHORT).show();
                    selectedImageUris.clear();
                });
    }

    // Преобразует Uri в base64-строку
    public static String encodeImageToBase64(android.content.Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Удалить отзыв (admin или свой)
    private void onDeleteFeedback(FeedbackModel feedback) {
        db.collection("feedbacks").document(feedback.getId())
                .delete()
                .addOnSuccessListener(aVoid -> loadFeedbacks());
    }

    // Мини-адаптер для предпросмотра фото в диалоге
    private class PhotoPreviewAdapter extends RecyclerView.Adapter<PhotoPreviewAdapter.PhotoViewHolder> {
        private List<Uri> uris = new ArrayList<>();
        public void setUris(List<Uri> uris) {
            this.uris = new ArrayList<>(uris);
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView img = new ImageView(parent.getContext());
            int size = (int) (64 * parent.getContext().getResources().getDisplayMetrics().density);
            img.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new PhotoViewHolder(img);
        }
        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            holder.img.setImageURI(uris.get(position));
        }
        @Override
        public int getItemCount() { return uris.size(); }
        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                img = (ImageView) itemView;
            }
        }
    }
}
