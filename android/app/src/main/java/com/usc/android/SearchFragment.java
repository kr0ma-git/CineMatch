package com.usc.android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final String OMDB_API_KEY = "API_KEY";

    private SearchView searchView;
    private RecyclerView rvSearchResults;
    private SearchAdapter adapter;
    private ChipGroup chipGroupType;
    private List<ImdbSearchResponse.SearchResult> searchResults = new ArrayList<>();
    private ImdbApiService omdbApiService;
    private ApiService cineMatchApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchView = view.findViewById(R.id.searchView);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        chipGroupType = view.findViewById(R.id.chipGroupType);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchAdapter(searchResults, this::onMovieSelected);
        rvSearchResults.setAdapter(adapter);

        setupRetrofit();
        fetchUserBookmarks(); // Get initial bookmarks to show "Bookmarked" status

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query, getSelectedType());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String query = searchView.getQuery().toString();
            if (!query.isEmpty()) {
                performSearch(query, getSelectedType());
            }
        });
    }

    private void setupRetrofit() {
        Gson gson = new GsonBuilder().setLenient().create();
        omdbApiService = new Retrofit.Builder()
                .baseUrl("https://www.omdbapi.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(ImdbApiService.class);

        cineMatchApiService = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:4000/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(ApiService.class);
    }

    private void fetchUserBookmarks() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) return;

        cineMatchApiService.getUserBookmarks(userId).enqueue(new Callback<BookmarksResponse>() {
            @Override
            public void onResponse(Call<BookmarksResponse> call, Response<BookmarksResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    List<String> bookmarkedIds = new ArrayList<>();
                    for (Bookmark b : response.body().getBookmarks()) {
                        bookmarkedIds.add(b.getImdbMovieId());
                    }
                    adapter.setBookmarkedIds(bookmarkedIds);
                }
            }
            @Override
            public void onFailure(Call<BookmarksResponse> call, Throwable t) {}
        });
    }

    private String getSelectedType() {
        int checkedId = chipGroupType.getCheckedChipId();
        if (checkedId == R.id.chipMovies) return "movie";
        if (checkedId == R.id.chipSeries) return "series";
        return null;
    }

    private void performSearch(String query, String type) {
        omdbApiService.searchMovies(OMDB_API_KEY, query, type).enqueue(new Callback<ImdbSearchResponse>() {
            @Override
            public void onResponse(Call<ImdbSearchResponse> call, Response<ImdbSearchResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    ImdbSearchResponse data = response.body();
                    if ("True".equalsIgnoreCase(data.getResponse())) {
                        searchResults.clear();
                        searchResults.addAll(data.getSearch());
                        adapter.notifyDataSetChanged();
                    } else {
                        searchResults.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<ImdbSearchResponse> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onMovieSelected(ImdbSearchResponse.SearchResult movie) {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle(movie.getTitle())
                .setItems(new String[]{"Add to Bookmarks", "Write a Review"}, (dialog, which) -> {
                    if (which == 0) handleBookmark(userId, movie);
                    else showReviewDialog(userId, movie);
                }).show();
    }

    private void handleBookmark(String userId, ImdbSearchResponse.SearchResult movie) {
        String type = getSelectedType() == null ? "movie" : getSelectedType();
        cineMatchApiService.createBookmark(new BookmarkRequest(userId, movie.getId(), movie.getTitle(), type))
                .enqueue(new Callback<BookmarksResponse>() {
            @Override
            public void onResponse(Call<BookmarksResponse> call, Response<BookmarksResponse> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Bookmarked!", Toast.LENGTH_SHORT).show();
                    fetchUserBookmarks(); // Update "Bookmarked" indicators
                }
            }
            @Override
            public void onFailure(Call<BookmarksResponse> call, Throwable t) {}
        });
    }

    private void showReviewDialog(String userId, ImdbSearchResponse.SearchResult movie) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        EditText et = v.findViewById(R.id.etReview);
        RatingBar rb = v.findViewById(R.id.ratingBar);

        new AlertDialog.Builder(getContext()).setTitle("Review " + movie.getTitle()).setView(v)
                .setPositiveButton("Post", (d, w) -> postReview(userId, movie, et.getText().toString(), (int) rb.getRating()))
                .setNegativeButton("Cancel", null).show();
    }

    private void postReview(String userId, ImdbSearchResponse.SearchResult movie, String text, int rating) {
        cineMatchApiService.createReview(new ReviewRequest(userId, movie.getId(), movie.getTitle(), text, rating))
                .enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) Toast.makeText(getContext(), "Review posted!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}