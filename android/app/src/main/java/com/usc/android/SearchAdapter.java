package com.usc.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<ImdbSearchResponse.SearchResult> results;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(ImdbSearchResponse.SearchResult movie);
    }

    public SearchAdapter(List<ImdbSearchResponse.SearchResult> results, OnMovieClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        ImdbSearchResponse.SearchResult movie = results.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvDescription.setText(movie.getDescription());
        
        holder.btnAction.setOnClickListener(v -> listener.onMovieClick(movie));
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        Button btnAction;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}