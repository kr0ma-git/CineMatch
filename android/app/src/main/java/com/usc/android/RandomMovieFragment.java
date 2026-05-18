package com.usc.android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RandomMovieFragment extends Fragment {

    private static final String TAG = "RandomMovieFragment";
    private TextView tvMovieTitle, tvReviewsHeader, tvNoReviews;
    private Button btnRefreshRandom, btnWriteReview;
    private RecyclerView rvMovieReviews;
    private SwipeRefreshLayout swipeRefresh;
    private ReviewAdapter adapter;
    private List<Review> reviewList = new ArrayList<>();
    private ApiService apiService;
    private String currentMovieId = null;
    private String currentMovieTitle = null;

    // Hardcoded fallback movies
    private List<String[]> hardcodedMovies = new ArrayList<String[]>() {{
        add(new String[]{"The Dark Knight", "tt0468569"});
        add(new String[]{"Inception", "tt1375666"});
        add(new String[]{"Interstellar", "tt0816692"});
        add(new String[]{"The Godfather", "tt0068646"});
        add(new String[]{"Pulp Fiction", "tt0110912"});
        add(new String[]{"The Matrix", "tt0133093"});
    }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_random_movie, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMovieTitle = view.findViewById(R.id.tvMovieTitle);
        tvReviewsHeader = view.findViewById(R.id.tvReviewsHeader);
        tvNoReviews = view.findViewById(R.id.tvNoReviews);
        btnRefreshRandom = view.findViewById(R.id.btnRefreshRandom);
        btnWriteReview = view.findViewById(R.id.btnWriteReview);
        rvMovieReviews = view.findViewById(R.id.rvMovieReviews);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        // Switching logic is now on swipe-left, so we hide the button
        btnRefreshRandom.setVisibility(View.GONE);

        rvMovieReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReviewAdapter(reviewList);
        rvMovieReviews.setAdapter(adapter);

        setupRetrofit();

        btnWriteReview.setOnClickListener(v -> showReviewDialog());

        // Setup Gesture Detector for Swipe Left
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null) {
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();
                    // Detect horizontal swipe
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                            if (diffX < 0) { // Swipe Left
                                pickRandomMovie();
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        });

        // Delegate touch events from the main container and the scroll view
        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        
        // Setup Pull-to-refresh to refresh reviews for the CURRENTly displayed movie
        swipeRefresh.setOnRefreshListener(() -> {
            if (currentMovieId != null) {
                fetchReviews(currentMovieId);
            } else {
                pickRandomMovie();
            }
        });

        pickRandomMovie();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:4000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void pickRandomMovie() {
        swipeRefresh.setRefreshing(true);
        
        Random rand = new Random();
        // 40% chance to pick a hardcoded movie immediately for variety
        if (rand.nextInt(10) < 4) {
            useHardcodedRandom();
            return;
        }

        apiService.getAllBookmarks().enqueue(new Callback<BookmarksResponse>() {
            @Override
            public void onResponse(Call<BookmarksResponse> call, Response<BookmarksResponse> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null && !response.body().getBookmarks().isEmpty()) {
                        List<Bookmark> bookmarks = response.body().getBookmarks();
                        Bookmark randomMovie = bookmarks.get(rand.nextInt(bookmarks.size()));
                        displayMovie(randomMovie.getImdbMovieTitle(), randomMovie.getImdbMovieId());
                    } else {
                        useHardcodedRandom();
                    }
                }
            }

            @Override
            public void onFailure(Call<BookmarksResponse> call, Throwable t) {
                if (isAdded()) {
                    useHardcodedRandom();
                }
            }
        });
    }

    private void useHardcodedRandom() {
        String[] movie = hardcodedMovies.get(new Random().nextInt(hardcodedMovies.size()));
        displayMovie(movie[0], movie[1]);
    }

    private void displayMovie(String title, String id) {
        currentMovieId = id;
        currentMovieTitle = title;
        tvMovieTitle.setText(title);
        fetchReviews(id);
    }

    private void fetchReviews(String movieId) {
        apiService.getMovieReviews(movieId).enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {
                if (isAdded()) {
                    reviewList.clear();
                    if (response.isSuccessful() && response.body() != null && response.body().getReviews() != null && !response.body().getReviews().isEmpty()) {
                        reviewList.addAll(response.body().getReviews());
                        tvReviewsHeader.setVisibility(View.VISIBLE);
                        tvNoReviews.setVisibility(View.GONE);
                    } else {
                        tvReviewsHeader.setVisibility(View.GONE);
                        tvNoReviews.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                if (isAdded()) {
                    reviewList.clear();
                    adapter.notifyDataSetChanged();
                    tvReviewsHeader.setVisibility(View.GONE);
                    tvNoReviews.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    private void showReviewDialog() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Log in to review", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        EditText etReview = dialogView.findViewById(R.id.etReview);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        new AlertDialog.Builder(getContext())
                .setTitle("Review " + currentMovieTitle)
                .setView(dialogView)
                .setPositiveButton("Post", (dialog, which) -> {
                    String reviewText = etReview.getText().toString().trim();
                    int rating = (int) ratingBar.getRating();
                    if (!reviewText.isEmpty()) {
                        postReview(userId, reviewText, rating);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void postReview(String userId, String text, int rating) {
        ReviewRequest request = new ReviewRequest(userId, currentMovieId, currentMovieTitle, text, rating);
        apiService.createReview(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Review posted!", Toast.LENGTH_SHORT).show();
                    fetchReviews(currentMovieId); // Refresh reviews list for current movie
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
