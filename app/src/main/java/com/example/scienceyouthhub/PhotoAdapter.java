package com.example.scienceyouthhub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<PhotoModel> photos;
    private String currentUserRole;
    private PhotoActionListener actionListener;

    public interface PhotoActionListener {
        void onEdit(PhotoModel photo);
    }

    public PhotoAdapter(List<PhotoModel> photos, String userRole, PhotoActionListener listener) {
        this.photos = photos;
        this.currentUserRole = userRole;
        this.actionListener = listener;
    }

    public void setPhotos(List<PhotoModel> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoModel photo = photos.get(position);
        Context context = holder.itemView.getContext();

        // === SAFE BASE64 DECODE ===
        Bitmap bitmap = null;
        String base64 = photo.getPhotoBase64();
        if (base64 != null && !base64.trim().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception e) {
                bitmap = null;
            }
        }

        if (bitmap != null) {
            holder.imageView.setImageBitmap(bitmap);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_no_photo); // добавь заглушку в drawable!
        }
        // ===

        holder.userTextView.setText(photo.getUserName());
        holder.clubTextView.setText(photo.getActivityName());

        if ("Admin".equals(currentUserRole)) {
            holder.editBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setVisibility(View.VISIBLE);

            holder.editBtn.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onEdit(photo);
            });

            holder.deleteBtn.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Delete photo")
                        .setMessage("Are you sure you want to delete this photo?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseFirestore.getInstance()
                                    .collection("activities")
                                    .document(photo.getActivityId())
                                    .collection("photos")
                                    .document(photo.getPhotoId())
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        int pos = holder.getAdapterPosition();
                                        if (pos != RecyclerView.NO_POSITION) {
                                            photos.remove(pos);
                                            notifyItemRemoved(pos);
                                            Snackbar.make(holder.itemView, "Photo deleted", Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Snackbar.make(holder.itemView, "Delete error", Snackbar.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            holder.editBtn.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return photos == null ? 0 : photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView userTextView;
        TextView clubTextView;
        ImageButton editBtn, deleteBtn;

        PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoImageView);
            userTextView = itemView.findViewById(R.id.photoUserTextView);
            clubTextView = itemView.findViewById(R.id.photoActivityNameTextView);
            editBtn = itemView.findViewById(R.id.editPhotoBtn);
            deleteBtn = itemView.findViewById(R.id.deletePhotoBtn);
        }
    }
}
