package com.usc.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;
    private Map<String, String> userMap = new HashMap<>();
    private OnItemLongClickListener longClickListener;
    private OnItemClickListener clickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Review review);
    }

    public interface OnItemClickListener {
        void onItemClick(Review review);
    }

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    public ReviewAdapter(List<Review> reviewList, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.reviewList = reviewList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setUserMap(Map<String, String> userMap) {
        this.userMap = userMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.tvMovieTitle.setText(review.getImdbMovieTitle());
        holder.ratingBar.setRating(review.getMovieRatingStars());
        holder.tvReviewText.setText(review.getMovieReviewString());
        
        String userId = review.getUser();
        String username = userMap.getOrDefault(userId, "Anonymous");
        
        holder.tvAuthorName.setText("— " + username);
        holder.tvAuthorName.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(review);
            }
        });

        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(review);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvReviewText, tvAuthorName;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}