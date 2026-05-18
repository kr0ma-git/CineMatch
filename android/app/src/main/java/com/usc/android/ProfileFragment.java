package com.usc.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String ARG_USER_ID = "user_id";

    private TextView tvUsername, tvEmail, tvProfileTitle, tvEmailLabel;
    private Button btnLogout;
    private ImageButton btnBack;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMyBookmarks, rvMyReviews;
    private MovieAdapter bookmarkAdapter;
    private ReviewAdapter reviewAdapter;
    private List<Bookmark> bookmarkList = new ArrayList<>();
    private List<Review> reviewList = new ArrayList<>();
    private Map<String, String> userMap = new HashMap<>();
    private ApiService apiService;
    private String targetUserId;

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetUserId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvProfileTitle = view.findViewById(R.id.tvProfileTitle);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvEmailLabel = view.findViewById(R.id.tvEmailLabel);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnBack = view.findViewById(R.id.btnBack);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        String sessionUserId = UserSession.getInstance().getUserId();
        if (targetUserId == null) {
            targetUserId = sessionUserId;
        }

        boolean isOwnProfile = targetUserId != null && targetUserId.equals(sessionUserId);

        if (!isOwnProfile) {
            tvProfileTitle.setText("User Profile");
            btnLogout.setVisibility(View.GONE);
            btnBack.setVisibility(View.VISIBLE);
            tvEmail.setVisibility(View.GONE); // Hide email for other users
            tvEmailLabel.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Initialize Bookmarks: Click to Review (only if own), Long Click to Delete (only if own)
        rvMyBookmarks = view.findViewById(R.id.rvMyBookmarks);
        rvMyBookmarks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bookmarkAdapter = new MovieAdapter(bookmarkList, 
                isOwnProfile ? this::onBookmarkClicked : null, 
                isOwnProfile ? this::showDeleteBookmarkDialog : null);
        rvMyBookmarks.setAdapter(bookmarkAdapter);

        // Initialize Reviews: Click to Edit (only if own), Long Click to Delete (only if own)
        rvMyReviews = view.findViewById(R.id.rvMyReviews);
        rvMyReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter(reviewList, 
                isOwnProfile ? this::onReviewClicked : null, 
                isOwnProfile ? this::showDeleteReviewDialog : null);
        rvMyReviews.setAdapter(reviewAdapter);

        btnLogout.setOnClickListener(v -> handleLogout());

        setupRetrofit();
        swipeRefresh.setOnRefreshListener(this::refreshData);
        refreshData();
    }

    private void handleLogout() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    UserSession.getInstance().setUserId(null);
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        fetchUserProfile();
        fetchUserBookmarks();
        fetchUserReviews();
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
                    reviewAdapter.setUserMap(userMap);
                }
            }
            @Override
            public void onFailure(@NonNull Call<UsersResponse> call, @NonNull Throwable t) {}
        });
    }

    private void fetchUserProfile() {
        if (targetUserId == null) return;

        apiService.getUserProfile(targetUserId).enqueue(new Callback<RegisterResponse>() {
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
        if (targetUserId == null) return;

        apiService.getUserBookmarks(targetUserId).enqueue(new Callback<BookmarksResponse>() {
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
        if (targetUserId == null) return;

        apiService.getUserReviews(targetUserId).enqueue(new Callback<ReviewsResponse>() {
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
        String sessionUserId = UserSession.getInstance().getUserId();
        if (sessionUserId == null) return;

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
                        postReview(sessionUserId, bookmark.getImdbMovieId(), bookmark.getImdbMovieTitle(), reviewText, rating);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onReviewClicked(Review review) {
        String sessionUserId = UserSession.getInstance().getUserId();
        if (sessionUserId == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        EditText etReview = dialogView.findViewById(R.id.etReview);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        etReview.setText(review.getMovieReviewString());
        ratingBar.setRating(review.getMovieRatingStars());

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Review: " + review.getImdbMovieTitle())
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newText = etReview.getText().toString().trim();
                    int newRating = (int) ratingBar.getRating();
                    if (!newText.isEmpty()) {
                        handleReviewUpdate(review.getId(), sessionUserId, review.getImdbMovieId(), review.getImdbMovieTitle(), newText, newRating);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleReviewUpdate(String oldReviewId, String userId, String movieId, String title, String text, int rating) {
        apiService.deleteReview(oldReviewId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    postReview(userId, movieId, title, text, rating);
                } else {
                    Toast.makeText(getContext(), "Update failed at deletion step", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network error during update", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postReview(String userId, String movieId, String title, String text, int rating) {
        ReviewRequest request = new ReviewRequest(userId, movieId, title, text, rating);
        apiService.createReview(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded() && response.isSuccessful()) {
                    Toast.makeText(getContext(), "Review updated!", Toast.LENGTH_SHORT).show();
                    fetchUserReviews(); 
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Network error posting new review", Toast.LENGTH_SHORT).show();
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
        String sessionUserId = UserSession.getInstance().getUserId();
        apiService.deleteBookmark(sessionUserId, bookmark.getImdbMovieId()).enqueue(new Callback<Void>() {
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
