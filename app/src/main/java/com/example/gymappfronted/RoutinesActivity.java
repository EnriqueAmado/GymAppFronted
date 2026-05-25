package com.example.gymappfronted;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymappfronted.Adapters.RoutinesAdapter;
import com.example.gymappfronted.Remote.ApiService;
import com.example.gymappfronted.Remote.RetrofitClient;
import com.example.gymappfronted.Models.RoutineResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.gymappfronted.R;

public class RoutinesActivity extends AppCompatActivity {

    private RecyclerView rvRoutines;
    private RoutinesAdapter adapter;
    private List<RoutineResponse> routineList = new ArrayList<>();
    private FloatingActionButton fabAddRoutine;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routines);
        rvRoutines = findViewById(R.id.rvRoutines);
        fabAddRoutine = findViewById(R.id.fabAddRoutine);

        rvRoutines.setLayoutManager(new LinearLayoutManager(this));

        // Botón flotante para cuando programemos la creación de rutinas
        fabAddRoutine.setOnClickListener(v -> {
            final android.widget.EditText etRoutineName = new android.widget.EditText(this);
            etRoutineName.setHint("Ej: Rutina de Empuje, Pierna...");
            etRoutineName.setPadding(50, 40, 50, 40); // Un poco de espacio elegante

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Nueva Rutina")
                    .setMessage("Escribe el nombre para tu rutina de entrenamiento:")
                    .setView(etRoutineName)
                    .setPositiveButton("Crear", (dialog, which) -> {
                        String name = etRoutineName.getText().toString().trim();
                        if (!name.isEmpty()) {
                            createNewRoutineInBackend(name);
                        } else {
                            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        loadRoutines();
    }

    private void loadRoutines() {
        // Recorremos las SharedPreferences para sacar el token guardado en el login
        SharedPreferences preferences = getSharedPreferences("GymAppPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Sesión expirada. Inicia sesión de nuevo.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RoutinesActivity.this, LoginActivity.class));
            finish();
            return;
        }

        //Django exige el prefijo "Token " en la cabecera
        String authHeader = "Token " + token;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getUserRoutines(authHeader).enqueue(new Callback<List<RoutineResponse>>() {
            @Override
            public void onResponse(Call<List<RoutineResponse>> call, Response<List<RoutineResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    routineList.clear();
                    routineList.addAll(response.body());
                    adapter = new RoutinesAdapter(routineList);
                    rvRoutines.setAdapter(adapter);
                } else {
                    Toast.makeText(RoutinesActivity.this, "Error al cargar rutinas: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RoutineResponse>> call, Throwable t) {
                Toast.makeText(RoutinesActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void createNewRoutineInBackend(String routineName) {
        SharedPreferences preferences = getSharedPreferences("GymAppPrefs", MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) return;

        String authHeader = "Token " + token;
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        RoutineResponse newRoutine = new RoutineResponse(routineName);

        apiService.createRoutine(authHeader, newRoutine).enqueue(new Callback<RoutineResponse>() {
            @Override
            public void onResponse(Call<RoutineResponse> call, Response<RoutineResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RoutinesActivity.this, "¡Rutina creada con éxito!", Toast.LENGTH_SHORT).show();
                    loadRoutines(); //Recargamos la lista automáticamente
                } else {
                    Toast.makeText(RoutinesActivity.this, "Error al crear rutina: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutineResponse> call, Throwable t) {
                Toast.makeText(RoutinesActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
