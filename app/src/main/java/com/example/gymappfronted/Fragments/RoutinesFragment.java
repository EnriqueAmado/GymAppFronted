package com.example.gymappfronted.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymappfronted.Adapters.RoutinesAdapter;
import com.example.gymappfronted.LoginActivity;
import com.example.gymappfronted.Models.RoutineResponse;
import com.example.gymappfronted.R;
import com.example.gymappfronted.Remote.ApiService;
import com.example.gymappfronted.Remote.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutinesFragment extends Fragment {

    private RecyclerView rvRoutines;
    private RoutinesAdapter adapter;
    private List<RoutineResponse> routineList = new ArrayList<>();
    private FloatingActionButton fabAddRoutine;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routines, container, false);

        rvRoutines = view.findViewById(R.id.rvRoutines);
        fabAddRoutine = view.findViewById(R.id.fabAddRoutine);

        rvRoutines.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAddRoutine.setOnClickListener(v -> {
            final EditText etRoutineName = new EditText(getContext());
            etRoutineName.setHint("Ej: Rutina de Empuje, Pierna...");
            etRoutineName.setPadding(50, 40, 50, 40);

            new AlertDialog.Builder(getContext())
                    .setTitle("Nueva Rutina")
                    .setMessage("Escribe el nombre para tu rutina de entrenamiento:")
                    .setView(etRoutineName)
                    .setPositiveButton("Crear", (dialog, which) -> {
                        String name = etRoutineName.getText().toString().trim();
                        if (!name.isEmpty()) {
                            createNewRoutineInBackend(name);
                        } else {
                            Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        loadRoutines();

        return view;
    }

    private void loadRoutines() {
        SharedPreferences preferences = getActivity().getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
            return;
        }

        String authHeader = "Token " + token;
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getUserRoutines(authHeader).enqueue(new Callback<List<RoutineResponse>>() {
            @Override
            public void onResponse(Call<List<RoutineResponse>> call, Response<List<RoutineResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    routineList.clear();
                    routineList.addAll(response.body());
                    adapter = new RoutinesAdapter(routineList);
                    rvRoutines.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<RoutineResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createNewRoutineInBackend(String routineName) {
        SharedPreferences preferences = getActivity().getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);
        if (token == null) return;

        String authHeader = "Token " + token;
        RoutineResponse newRoutine = new RoutineResponse(routineName);

        RetrofitClient.getApiService().createRoutine(authHeader, newRoutine).enqueue(new Callback<RoutineResponse>() {
            @Override
            public void onResponse(Call<RoutineResponse> call, Response<RoutineResponse> response) {
                if (response.isSuccessful()) {
                    loadRoutines();
                }
            }

            @Override
            public void onFailure(Call<RoutineResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Fallo al crear rutina", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRoutines();
    }
}