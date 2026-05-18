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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RandomMovieFragment extends Fragment {

    private static final String TAG = "RandomMovieFragment";
    private TextView tvMovieTitle, tvReviewsHeader, tvNoReviews;
    private Button btnRefreshRandom, btnWriteReview, btnBookmark;
    private RecyclerView rvMovieReviews;
    private SwipeRefreshLayout swipeRefresh;
    private ReviewAdapter adapter;
    private List<Review> reviewList = new ArrayList<>();
    private final Map<String, String> userMap = new HashMap<>();
    private ApiService apiService;
    private String currentMovieId = null;
    private String currentMovieTitle = null;

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
        btnBookmark = view.findViewById(R.id.btnBookmark);
        rvMovieReviews = view.findViewById(R.id.rvMovieReviews);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        btnRefreshRandom.setVisibility(View.GONE);

        rvMovieReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReviewAdapter(reviewList, this::onReviewClicked, null);
        rvMovieReviews.setAdapter(adapter);

        setupRetrofit();

        btnWriteReview.setOnClickListener(v -> showReviewDialog());
        btnBookmark.setOnClickListener(v -> handleBookmark());

        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null) {
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();
                    if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                        if (diffX < 0) { pickRandomMovie(); return true; }
                    }
                }
                return false;
            }
        });

        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        swipeRefresh.setOnRefreshListener(this::refreshData);

        refreshData();
    }

    private void onReviewClicked(Review review) {
        ProfileFragment profileFragment = ProfileFragment.newInstance(review.getUser());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:4000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void refreshData() {
        swipeRefresh.setRefreshing(true);
        fetchUsers();
        pickRandomMovie();
    }

    private void fetchUsers() {
        apiService.getAllUsers().enqueue(new Callback<UsersResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    userMap.clear();
                    if (response.body().getUserData() != null) {
                        for (RegisterResponse.UserData user : response.body().getUserData()) {
                            userMap.put(user.getId(), user.getUsername());
                        }
                    }
                    adapter.setUserMap(userMap);
                }
            }
            @Override
            public void onFailure(@NonNull Call<UsersResponse> call, @NonNull Throwable t) {}
        });
    }

    private void pickRandomMovie() {
        Random rand = new Random();
        if (rand.nextInt(10) < 4) { useHardcodedRandom(); return; }

        apiService.getAllBookmarks().enqueue(new Callback<BookmarksResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookmarksResponse> call, @NonNull Response<BookmarksResponse> response) {
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
            public void onFailure(@NonNull Call<BookmarksResponse> call, @NonNull Throwable t) {
                if (isAdded()) useHardcodedRandom();
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
            public void onResponse(@NonNull Call<ReviewsResponse> call, @NonNull Response<ReviewsResponse> response) {
                if (isAdded()) {
                    reviewList.clear();
                    if (response.isSuccessful() && response.body() != null && response.body().getReviews() != null) {
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
            public void onFailure(@NonNull Call<ReviewsResponse> call, @NonNull Throwable t) {
                if (isAdded()) swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void handleBookmark() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Log in to bookmark", Toast.LENGTH_SHORT).show();
            return;
        }

        BookmarkRequest request = new BookmarkRequest(userId, currentMovieId, currentMovieTitle, "movie");
        apiService.createBookmark(request).enqueue(new Callback<BookmarksResponse>() {
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

    private void showReviewDialog() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) return;
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        EditText et = v.findViewById(R.id.etReview);
        RatingBar rb = v.findViewById(R.id.ratingBar);
        new AlertDialog.Builder(getContext()).setTitle("Review " + currentMovieTitle).setView(v)
                .setPositiveButton("Post", (d, w) -> postReview(userId, et.getText().toString(), (int) rb.getRating()))
                .setNegativeButton("Cancel", null).show();
    }

    private void postReview(String userId, String text, int rating) {
        apiService.createReview(new ReviewRequest(userId, currentMovieId, currentMovieTitle, text, rating))
                .enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Review posted!", Toast.LENGTH_SHORT).show();
                    fetchReviews(currentMovieId);
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
    }
}
