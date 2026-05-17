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
    private static final String OMDB_API_KEY = "API_KEY"; //check your gmail brother

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

    private String getSelectedType() {
        int checkedId = chipGroupType.getCheckedChipId();
        if (checkedId == R.id.chipMovies) {
            return "movie";
        } else if (checkedId == R.id.chipSeries) {
            return "series";
        }
        return null; // "All"
    }

    private void setupRetrofit() {
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit omdbRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.omdbapi.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        omdbApiService = omdbRetrofit.create(ImdbApiService.class);

        Retrofit cineMatchRetrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:4000/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        cineMatchApiService = cineMatchRetrofit.create(ApiService.class);
    }

    private void performSearch(String query, String type) {
        omdbApiService.searchMovies(OMDB_API_KEY, query, type).enqueue(new Callback<ImdbSearchResponse>() {
            @Override
            public void onResponse(Call<ImdbSearchResponse> call, Response<ImdbSearchResponse> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        ImdbSearchResponse data = response.body();
                        if ("True".equalsIgnoreCase(data.getResponse())) {
                            searchResults.clear();
                            if (data.getSearch() != null) {
                                searchResults.addAll(data.getSearch());
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            searchResults.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ImdbSearchResponse> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onMovieSelected(ImdbSearchResponse.SearchResult movie) {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Log in to interact", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(movie.getTitle());
        String[] options = {"Add to Bookmarks", "Write a Review"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                handleBookmark(userId, movie);
            } else {
                showReviewDialog(userId, movie);
            }
        });
        builder.show();
    }

    private void handleBookmark(String userId, ImdbSearchResponse.SearchResult movie) {
        String type = getSelectedType();
        if (type == null) type = "movie";

        BookmarkRequest request = new BookmarkRequest(userId, movie.getId(), movie.getTitle(), type);
        cineMatchApiService.createBookmark(request).enqueue(new Callback<BookmarksResponse>() {
            @Override
            public void onResponse(Call<BookmarksResponse> call, Response<BookmarksResponse> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Bookmarked!", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 409) {
                        Toast.makeText(getContext(), "Already bookmarked", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BookmarksResponse> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReviewDialog(String userId, ImdbSearchResponse.SearchResult movie) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        EditText etReview = dialogView.findViewById(R.id.etReview);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        new AlertDialog.Builder(getContext())
                .setTitle("Review " + movie.getTitle())
                .setView(dialogView)
                .setPositiveButton("Post", (dialog, which) -> {
                    String reviewText = etReview.getText().toString().trim();
                    int rating = (int) ratingBar.getRating();
                    if (!reviewText.isEmpty()) {
                        postReview(userId, movie, reviewText, rating);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void postReview(String userId, ImdbSearchResponse.SearchResult movie, String text, int rating) {
        ReviewRequest request = new ReviewRequest(userId, movie.getId(), movie.getTitle(), text, rating);
        cineMatchApiService.createReview(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Review posted!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}