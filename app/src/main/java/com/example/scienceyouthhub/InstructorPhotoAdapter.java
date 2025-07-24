package com.example.scienceyouthhub;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InstructorPhotoAdapter extends RecyclerView.Adapter<InstructorPhotoAdapter.PhotoViewHolder> {

    private List<InstructorPhotoModel> photos;

    public InstructorPhotoAdapter(List<InstructorPhotoModel> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instructor_photo, parent, false);
        return new PhotoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        InstructorPhotoModel model = photos.get(position);
        holder.photoDateText.setText(model.getDateString());
        if (model.getImageBase64() != null && !model.getImageBase64().isEmpty()) {
            byte[] decoded = Base64.decode(model.getImageBase64(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            holder.photoImageView.setImageBitmap(bitmap);
        } else {
            holder.photoImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    public void setPhotos(List<InstructorPhotoModel> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        TextView photoDateText;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            photoDateText = itemView.findViewById(R.id.photoDateText);
        }
    }
}
