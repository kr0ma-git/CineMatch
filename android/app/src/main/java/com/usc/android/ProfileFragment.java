package com.usc.android;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private TextView tvUsername, tvEmail;
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

        setupRetrofit();
        fetchUserProfile();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:4000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void fetchUserProfile() {
        String userId = UserSession.getInstance().getUserId();

        if (userId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getUserProfile(userId).enqueue(new Callback<RegisterResponse.UserData>() {
            @Override
            public void onResponse(Call<RegisterResponse.UserData> call, Response<RegisterResponse.UserData> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    RegisterResponse.UserData user = response.body();
                    tvUsername.setText(user.getUsername());
                    tvEmail.setText(user.getEmail());
                } else {
                    Log.e(TAG, "Failed to fetch profile: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse.UserData> call, Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "Network error fetching profile", t);
                }
            }
        });
    }
}