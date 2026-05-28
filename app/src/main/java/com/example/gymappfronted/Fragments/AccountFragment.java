package com.example.gymappfronted.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gymappfronted.Models.WorkoutLogResponse;
import com.example.gymappfronted.R;
import com.example.gymappfronted.Remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private TextView tvUser, tvEmail, tvTotalWorkouts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tvUser = view.findViewById(R.id.tvAccountUser);
        tvEmail = view.findViewById(R.id.tvAccountEmail);
        tvTotalWorkouts = view.findViewById(R.id.tvTotalWorkouts);

        SharedPreferences prefs = getActivity().getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "No disponible");
        String email = prefs.getString("email", "No disponible");
        String token = "Token " + prefs.getString("token", "");

        tvUser.setText("Nombre de usuario: " + username);
        tvEmail.setText("Email: " + email);

        loadWorkoutCount(token);

        return view;
    }

    private void loadWorkoutCount(String token) {
        RetrofitClient.getApiService().getWorkoutLogs(token).enqueue(new Callback<List<WorkoutLogResponse>>() {
            @Override
            public void onResponse(Call<List<WorkoutLogResponse>> call, Response<List<WorkoutLogResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().size();
                    tvTotalWorkouts.setText("Total de series registradas: " + count);
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutLogResponse>> call, Throwable t) {
                if (isAdded()) {
                    tvTotalWorkouts.setText("Total de series: Error al cargar");
                }
            }
        });
    }
}