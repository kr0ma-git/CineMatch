package com.usc.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextInputEditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupRetrofit();

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:4000/") 
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void handleRegistration() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Check if empty
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Username checks
        if (!username.matches("^[a-zA-Z0-9]*$")) {
            etUsername.setError("Username must be alphanumeric (letters/numbers only)");
            return;
        }
        if (username.length() > 15) {
            etUsername.setError("Username must be 15 characters or less");
            return;
        }

        // 3. Password length check (min 6 characters)
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        RegisterRequest request = new RegisterRequest(username, email, password);
        Log.d(TAG, "Sending Registration: " + username + ", " + email);
        
        apiService.registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Success: " + response.body().getMessage());
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    finish(); 
                } else {
                    String errorDetail = "No error body";
                    try {
                        if (response.errorBody() != null) {
                            errorDetail = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        errorDetail = "Error reading error body: " + e.getMessage();
                    }
                    
                    Log.e(TAG, "SERVER ERROR CODE: " + response.code());
                    Log.e(TAG, "SERVER ERROR BODY: " + errorDetail);
                    
                    Toast.makeText(RegisterActivity.this, "Server Error (" + response.code() + "). Check Logcat.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Log.e(TAG, "NETWORK FAILURE: ", t);
                Toast.makeText(RegisterActivity.this, "Network Failed! Check server.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
