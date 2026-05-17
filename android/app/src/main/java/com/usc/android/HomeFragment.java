package com.usc.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private RecyclerView rvReviews, rvMovies;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyStateHome;
    private Button btnGoToSearch;
    private TextView tvDiscoverTitle, tvSectionTitle;
    private ReviewAdapter reviewAdapter;
    private MovieAdapter movieAdapter;
    private final List<Review> reviewList = new ArrayList<>();
    private final List<Bookmark> movieList = new ArrayList<>();
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvReviews = view.findViewById(R.id.rvReviews);
        rvMovies = view.findViewById(R.id.rvMovies);
        emptyStateHome = view.findViewById(R.id.emptyStateHome);
        btnGoToSearch = view.findViewById(R.id.btnGoToSearch);
        tvDiscoverTitle = view.findViewById(R.id.tvDiscoverTitle);
        tvSectionTitle = view.findViewById(R.id.tvSectionTitle);

        // Setup RecyclerViews
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(reviewAdapter);

        rvMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        movieAdapter = new MovieAdapter(movieList);
        rvMovies.setAdapter(movieAdapter);

        setupRetrofit();

        swipeRefresh.setOnRefreshListener(this::refreshContent);

        btnGoToSearch.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                if (nav != null) {
                    nav.setSelectedItemId(R.id.nav_search);
                }
            }
        });

        // Initial Load
        refreshContent();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:4000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void refreshContent() {
        swipeRefresh.setRefreshing(true);
        fetchMovies();
        fetchReviews();
    }

    private void fetchMovies() {
        apiService.getAllBookmarks().enqueue(new Callback<BookmarksResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookmarksResponse> call, @NonNull Response<BookmarksResponse> response) {
                if (isAdded()) {
                    movieList.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        List<Bookmark> allBookmarks = response.body().getBookmarks();
                        Set<String> seenIds = new HashSet<>();
                        for (Bookmark b : allBookmarks) {
                            if (b.getImdbMovieId() != null && !seenIds.contains(b.getImdbMovieId())) {
                                movieList.add(b);
                                seenIds.add(b.getImdbMovieId());
                            }
                        }
                    }
                    movieAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    checkRefreshStatus();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookmarksResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    updateEmptyState();
                    checkRefreshStatus();
                }
            }
        });
    }

    private void fetchReviews() {
        apiService.getAllReviews().enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReviewsResponse> call, @NonNull Response<ReviewsResponse> response) {
                if (isAdded()) {
                    reviewList.clear();
                    if (response.isSuccessful() && response.body() != null) {
                        reviewList.addAll(response.body().getReviews());
                    }
                    reviewAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    checkRefreshStatus();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReviewsResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    updateEmptyState();
                    checkRefreshStatus();
                }
            }
        });
    }

    private void updateEmptyState() {
        boolean noContent = movieList.isEmpty() && reviewList.isEmpty();
        
        emptyStateHome.setVisibility(noContent ? View.VISIBLE : View.GONE);
        
        tvDiscoverTitle.setVisibility(movieList.isEmpty() ? View.GONE : View.VISIBLE);
        rvMovies.setVisibility(movieList.isEmpty() ? View.GONE : View.VISIBLE);
        
        tvSectionTitle.setVisibility(reviewList.isEmpty() ? View.GONE : View.VISIBLE);
        rvReviews.setVisibility(reviewList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void checkRefreshStatus() {
        swipeRefresh.setRefreshing(false);
    }
}
