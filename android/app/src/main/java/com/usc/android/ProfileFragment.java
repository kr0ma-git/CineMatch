package com.usc.android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private TextView tvUsername, tvEmail;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMyBookmarks, rvMyReviews;
    private MovieAdapter bookmarkAdapter;
    private ReviewAdapter reviewAdapter;
    private List<Bookmark> bookmarkList = new ArrayList<>();
    private List<Review> reviewList = new ArrayList<>();
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        // Initialize Bookmarks with Click to Review and Long Click to Delete
        rvMyBookmarks = view.findViewById(R.id.rvMyBookmarks);
        rvMyBookmarks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bookmarkAdapter = new MovieAdapter(bookmarkList, this::onBookmarkClicked, this::showDeleteBookmarkDialog);
        rvMyBookmarks.setAdapter(bookmarkAdapter);

        // Initialize Reviews with Long Click to Delete
        rvMyReviews = view.findViewById(R.id.rvMyReviews);
        rvMyReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter(reviewList, this::showDeleteReviewDialog);
        rvMyReviews.setAdapter(reviewAdapter);

        setupRetrofit();

        swipeRefresh.setOnRefreshListener(this::refreshData);

        // Initial Load
        refreshData();
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
        fetchUserProfile();
        fetchUserBookmarks();
        fetchUserReviews();
    }

    private void fetchUserProfile() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) return;

        apiService.getUserProfile(userId).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    RegisterResponse.UserData user = response.body().getUser();
                    if (user != null) {
                        tvUsername.setText(user.getUsername());
                        tvEmail.setText(user.getEmail());
                    }
                }
                checkRefreshStatus();
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                if (isAdded()) checkRefreshStatus();
            }
        });
    }

    private void fetchUserBookmarks() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) return;

        apiService.getUserBookmarks(userId).enqueue(new Callback<BookmarksResponse>() {
            @Override
            public void onResponse(Call<BookmarksResponse> call, Response<BookmarksResponse> response) {
                if (isAdded()) {
                    bookmarkList.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        bookmarkList.addAll(response.body().getBookmarks());
                    }
                    bookmarkAdapter.notifyDataSetChanged();
                    checkRefreshStatus();
                }
            }

            @Override
            public void onFailure(Call<BookmarksResponse> call, Throwable t) {
                if (isAdded()) checkRefreshStatus();
            }
        });
    }

    private void fetchUserReviews() {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) return;

        apiService.getUserReviews(userId).enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {
                if (isAdded()) {
                    reviewList.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        reviewList.addAll(response.body().getReviews());
                    }
                    reviewAdapter.notifyDataSetChanged();
                    checkRefreshStatus();
                }
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                if (isAdded()) checkRefreshStatus();
            }
        });
    }

    private void onBookmarkClicked(Bookmark bookmark) {
        String userId = UserSession.getInstance().getUserId();
        if (userId == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        EditText etReview = dialogView.findViewById(R.id.etReview);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        new AlertDialog.Builder(getContext())
                .setTitle("Review " + bookmark.getImdbMovieTitle())
                .setView(dialogView)
                .setPositiveButton("Post", (dialog, which) -> {
                    String reviewText = etReview.getText().toString().trim();
                    int rating = (int) ratingBar.getRating();
                    if (!reviewText.isEmpty()) {
                        postReview(userId, bookmark, reviewText, rating);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void postReview(String userId, Bookmark bookmark, String text, int rating) {
        ReviewRequest request = new ReviewRequest(userId, bookmark.getImdbMovieId(), bookmark.getImdbMovieTitle(), text, rating);
        apiService.createReview(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Review posted!", Toast.LENGTH_SHORT).show();
                    fetchUserReviews(); // Refresh reviews list
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteBookmarkDialog(Bookmark bookmark) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove Bookmark")
                .setMessage("Remove " + bookmark.getImdbMovieTitle() + " from your profile?")
                .setPositiveButton("Delete", (d, w) -> deleteBookmark(bookmark))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBookmark(Bookmark bookmark) {
        String userId = UserSession.getInstance().getUserId();
        apiService.deleteBookmark(userId, bookmark.getImdbMovieId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
                    fetchUserBookmarks();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void showDeleteReviewDialog(Review review) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Review")
                .setMessage("Are you sure you want to delete your review for " + review.getImdbMovieTitle() + "?")
                .setPositiveButton("Delete", (d, w) -> deleteReview(review))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReview(Review review) {
        apiService.deleteReview(review.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Review Deleted", Toast.LENGTH_SHORT).show();
                    fetchUserReviews();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void checkRefreshStatus() {
        swipeRefresh.setRefreshing(false);
    }
}
