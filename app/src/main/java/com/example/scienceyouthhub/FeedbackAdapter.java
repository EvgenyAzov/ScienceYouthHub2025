package com.example.scienceyouthhub;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<FeedbackModel> feedbacks;
    private final UserNameProvider userNameProvider;
    private final OnDeleteListener deleteListener;
    private String currentUserId;
    private String userRole = "User";

    public interface UserNameProvider {
        String getUserNameById(String userId);
    }
    public interface OnDeleteListener {
        void onDelete(FeedbackModel feedback);
    }

    public FeedbackAdapter(List<FeedbackModel> feedbacks, UserNameProvider userNameProvider, OnDeleteListener deleteListener, String currentUserId, String userRole) {
        this.feedbacks = feedbacks;
        this.userNameProvider = userNameProvider;
        this.deleteListener = deleteListener;
        this.currentUserId = currentUserId;
        this.userRole = userRole;
    }

    public void setFeedbacks(List<FeedbackModel> feedbacks) {
        this.feedbacks = feedbacks;
        notifyDataSetChanged();
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        FeedbackModel feedback = feedbacks.get(position);
        holder.user.setText(userNameProvider.getUserNameById(feedback.getUserId()));
        holder.ratingBar.setRating(feedback.getScore());
        holder.comment.setText(feedback.getComment());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        holder.date.setText(sdf.format(new Date(feedback.getTimestamp())));

        // Кнопка удалить: показываем админу или владельцу
        if ("Admin".equals(userRole) || currentUserId.equals(feedback.getUserId())) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setOnClickListener(v -> deleteListener.onDelete(feedback));
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        // Показываем фото
        if (feedback.getPhotos() != null && !feedback.getPhotos().isEmpty()) {
            holder.photosRecyclerView.setVisibility(View.VISIBLE);
            holder.photosRecyclerView.setAdapter(new PhotoMiniAdapter(feedback.getPhotos()));
        } else {
            holder.photosRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return feedbacks == null ? 0 : feedbacks.size();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView user, comment, date;
        RatingBar ratingBar;
        RecyclerView photosRecyclerView;
        Button deleteBtn;
        FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.feedbackUser);
            ratingBar = itemView.findViewById(R.id.feedbackRatingBar);
            comment = itemView.findViewById(R.id.feedbackComment);
            date = itemView.findViewById(R.id.feedbackDate);
            photosRecyclerView = itemView.findViewById(R.id.feedbackPhotosRecyclerView);
            deleteBtn = itemView.findViewById(R.id.deleteFeedbackBtn);
            photosRecyclerView.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        }
    }

    static class PhotoMiniAdapter extends RecyclerView.Adapter<PhotoMiniAdapter.PhotoViewHolder> {
        private final List<String> photoBase64s;

        PhotoMiniAdapter(List<String> photoBase64s) { this.photoBase64s = photoBase64s; }

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
            Bitmap bmp = decodeBase64ToBitmap(photoBase64s.get(position));
            holder.img.setImageBitmap(bmp);
        }

        @Override
        public int getItemCount() { return photoBase64s.size(); }

        static class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                img = (ImageView) itemView;
            }
        }

        static Bitmap decodeBase64ToBitmap(String base64Str) {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        }
    }
}
