package com.example.gymappfronted;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymappfronted.Adapters.RoutineExercisesAdapter;
import com.example.gymappfronted.Models.Exercise;
import com.example.gymappfronted.Models.RoutineExerciseRequest;
import com.example.gymappfronted.Models.RoutineExerciseResponse;
import com.example.gymappfronted.Models.RoutineResponse;
import com.example.gymappfronted.Remote.ApiService;
import com.example.gymappfronted.Remote.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineDetailActivity extends AppCompatActivity {

    private TextView tvRoutineName;
    private RecyclerView rvExercises;
    private FloatingActionButton fabAddExercise;
    private int routineId;
    private ApiService apiService;
    private String token;

    // Lista global para guardar los ejercicios del catálogo que nos dé Django
    private List<Exercise> catalogExercises = new ArrayList<>();

    private RoutineExercisesAdapter adapter;
    private List<RoutineExerciseResponse> routineExercisesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        // Inicializar vistas y Retrofit
        tvRoutineName = findViewById(R.id.tvRoutineName);
        rvExercises = findViewById(R.id.rvExercisesInRoutine);
        fabAddExercise = findViewById(R.id.fabAddExercise);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Recuperar el Token guardado en el Login
        SharedPreferences sharedPreferences = getSharedPreferences("GymAppPrefs", MODE_PRIVATE);
        token = "Token " + sharedPreferences.getString("token", "");

        // Recibir datos de la pantalla anterior
        if (getIntent().hasExtra("ROUTINE_ID")) {
            routineId = getIntent().getIntExtra("ROUTINE_ID", -1);
            String routineName = getIntent().getStringExtra("ROUTINE_NAME");
            tvRoutineName.setText(routineName);
        }

        adapter = new RoutineExercisesAdapter(routineExercisesList, new RoutineExercisesAdapter.OnExerciseClickListener() {
            @Override
            public void onExerciseClick(RoutineExerciseResponse exercise) {
                // Al pulsar en el ejercicio, viajamos a la nueva pantalla de Logs
                Intent intent = new Intent(RoutineDetailActivity.this, WorkoutLogActivity.class);

                // Pasamos el ID de la relación y el nombre asegurando los métodos del modelo
                intent.putExtra("ROUTINE_EXERCISE_ID", exercise.getId());
                intent.putExtra("EXERCISE_NAME", exercise.getExerciseName());
                intent.putExtra("TARGET_SETS", exercise.getSets());

                // Buscamos el ID real del ejercicio en el catálogo para poder usar el endpoint de progreso
                for (Exercise ex : catalogExercises) {
                    if (ex.getName().equals(exercise.getExerciseName())) {
                        intent.putExtra("EXERCISE_ID", ex.getId());
                        break;
                    }
                }

                startActivity(intent);
            }

            @Override
            public void onDeleteClick(RoutineExerciseResponse exercise, int position) {
                new AlertDialog.Builder(RoutineDetailActivity.this)
                        .setTitle("¿Eliminar ejercicio?")
                        .setMessage("¿Estás seguro de que quieres eliminar " + exercise.getExerciseName() + " de esta rutina?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            deleteExerciseFromRoutine(exercise.getId(), position);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        rvExercises.setAdapter(adapter);

        // Cargar el catálogo de ejercicios de Django para tenerlo listo
        loadExerciseCatalog();

        // Acción del botón flotante para añadir ejercicios
        fabAddExercise.setOnClickListener(v -> showAddExerciseDialog());

        loadRoutineExercises();
    }

    // Trae los ejercicios existentes de la base de datos
    private void loadExerciseCatalog() {
        apiService.getExercises().enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    catalogExercises = response.body();
                } else {
                    Toast.makeText(RoutineDetailActivity.this, "Error al cargar catálogo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                Toast.makeText(RoutineDetailActivity.this, "Fallo de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Despliega el formulario flotante
    private void showAddExerciseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_exercise, null);
        builder.setView(dialogView);

        // Enlazar los campos del XML del diálogo
        Spinner spinner = dialogView.findViewById(R.id.spinnerExercises);
        EditText etSets = dialogView.findViewById(R.id.etSets);
        EditText etReps = dialogView.findViewById(R.id.etReps);
        EditText etOrder = dialogView.findViewById(R.id.etOrder);

        // Meter los nombres de los ejercicios en el Spinner (Desplegable)
        List<String> exerciseNames = new ArrayList<>();
        for (Exercise ex : catalogExercises) {
            exerciseNames.add(ex.getName()); // Asume que tu modelo Exercise tiene getName()
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Configurar botones del diálogo
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            if (catalogExercises.isEmpty() || spinner.getSelectedItem() == null) {
                Toast.makeText(this, "No hay ejercicio seleccionado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Impide que se cierre la app si queda algo vacío

            String strSets = etSets.getText().toString().trim();
            String strReps = etReps.getText().toString().trim();
            String strOrder = etOrder.getText().toString().trim();

            if (strSets.isEmpty() || strReps.isEmpty() || strOrder.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos numéricos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtener el ejercicio seleccionado y sus datos
            int selectedPosition = spinner.getSelectedItemPosition();
            Exercise selectedExercise = catalogExercises.get(selectedPosition);

            int sets = Integer.parseInt(etSets.getText().toString().trim());
            int reps = Integer.parseInt(etReps.getText().toString().trim());
            int order = Integer.parseInt(etOrder.getText().toString().trim());

            // Crear el objeto Request que irá a Django
            RoutineExerciseRequest request = new RoutineExerciseRequest(
                    routineId,
                    selectedExercise.getId(), // Asume que tu modelo Exercise tiene getId()
                    sets,
                    reps,
                    order
            );

            // Enviar el POST a Django
            sendExerciseToBackend(request);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Envía el ejercicio asignado a Django
    private void sendExerciseToBackend(RoutineExerciseRequest request) {
        apiService.addExerciseToRoutine(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RoutineDetailActivity.this, "¡Ejercicio añadido!", Toast.LENGTH_SHORT).show();
                    loadRoutineExercises();

                } else {
                    Toast.makeText(RoutineDetailActivity.this, "Error de servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RoutineDetailActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteExerciseFromRoutine(int exerciseId, int position) {
        apiService.deleteExercise(token, exerciseId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RoutineDetailActivity.this, "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
                    routineExercisesList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, routineExercisesList.size());
                } else {
                    Toast.makeText(RoutineDetailActivity.this, "Error al eliminar ejercicio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RoutineDetailActivity.this, "Fallo de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRoutineExercises() {
        apiService.getUserRoutines(token).enqueue(new Callback<List<RoutineResponse>>() {
            @Override
            public void onResponse(Call<List<RoutineResponse>> call, Response<List<RoutineResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RoutineResponse> allRoutines = response.body();

                    // Buscamos la rutina actual por su ID
                    for (RoutineResponse routine : allRoutines) {
                        if (routine.getId() == routineId) {
                            if (routine.getExercises() != null) {
                                // Guardamos la lista de respuestas con los nombres mapeados
                                routineExercisesList = routine.getExercises();
                                adapter.setExercises(routineExercisesList);
                            }
                            break;
                        }
                    }
                } else {
                    Toast.makeText(RoutineDetailActivity.this, "Error al refrescar lista", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RoutineResponse>> call, Throwable t) {
                Toast.makeText(RoutineDetailActivity.this, "Fallo de red al actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}