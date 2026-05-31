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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private TextView tvUser, tvEmail, tvTotalVolume, tvTotalSets, tvFavoriteExercise;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tvUser = view.findViewById(R.id.tvAccountUser);
        tvEmail = view.findViewById(R.id.tvAccountEmail);
        tvTotalVolume = view.findViewById(R.id.tvStatTotalVolume);
        tvTotalSets = view.findViewById(R.id.tvStatTotalSets);
        tvFavoriteExercise = view.findViewById(R.id.tvStatFavoriteExercise);

        SharedPreferences prefs = getActivity().getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "No disponible");
        String email = prefs.getString("email", "No disponible");
        String token = "Token " + prefs.getString("token", "");

        tvUser.setText("Nombre de usuario: " + username);
        tvEmail.setText("Email: " + email);

        loadStatistics(token);

        return view;
    }

    private void loadStatistics(String token) {
        RetrofitClient.getApiService().getWorkoutLogs(token).enqueue(new Callback<List<WorkoutLogResponse>>() {
            @Override
            public void onResponse(Call<List<WorkoutLogResponse>> call, Response<List<WorkoutLogResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<WorkoutLogResponse> logs = response.body();
                    
                    double totalKg = 0;
                    int totalSets = logs.size();
                    Map<String, Integer> exerciseCounts = new HashMap<>();

                    for (WorkoutLogResponse log : logs) {
                        totalKg += (log.getWeight() * log.getReps());
                        String name = log.getExerciseName();
                        if (name != null) {
                            exerciseCounts.put(name, exerciseCounts.getOrDefault(name, 0) + 1);
                        }
                    }

                    // Encontrar el favorito
                    String favorite = "Ninguno";
                    int max = 0;
                    for (Map.Entry<String, Integer> entry : exerciseCounts.entrySet()) {
                        if (entry.getValue() > max) {
                            max = entry.getValue();
                            favorite = entry.getKey();
                        }
                    }

                    // Actualizar interfaz
                    if (isAdded()) {
                        tvTotalVolume.setText(String.format(Locale.getDefault(), "%.0f", totalKg));
                        tvTotalSets.setText(String.valueOf(totalSets));
                        tvFavoriteExercise.setText(favorite);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutLogResponse>> call, Throwable t) {
                // Error silencioso
            }
        });
    }
}
