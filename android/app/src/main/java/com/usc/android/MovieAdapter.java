package com.usc.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Bookmark> movieList;
    private OnItemLongClickListener longClickListener;
    private OnItemClickListener clickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Bookmark bookmark);
    }

    public interface OnItemClickListener {
        void onItemClick(Bookmark bookmark);
    }

    public MovieAdapter(List<Bookmark> movieList) {
        this.movieList = movieList;
    }

    public MovieAdapter(List<Bookmark> movieList, OnItemLongClickListener longClickListener) {
        this.movieList = movieList;
        this.longClickListener = longClickListener;
    }

    public MovieAdapter(List<Bookmark> movieList, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.movieList = movieList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Bookmark movie = movieList.get(position);
        holder.tvMovieTitle.setText(movie.getImdbMovieTitle());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(movie);
            }
        });

        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(movie);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
        }
    }
}