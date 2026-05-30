package com.example.gymappfronted;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

    // Despliega el formulario flotante con filtros
    private void showAddExerciseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_exercise, null);
        builder.setView(dialogView);

        Spinner spinnerMuscle = dialogView.findViewById(R.id.spinnerMuscleGroup);
        EditText etSearch = dialogView.findViewById(R.id.etSearchExercise);
        Spinner spinnerExercises = dialogView.findViewById(R.id.spinnerExercises);
        
        EditText etSets = dialogView.findViewById(R.id.etSets);
        EditText etReps = dialogView.findViewById(R.id.etReps);
        EditText etOrder = dialogView.findViewById(R.id.etOrder);

        // 1. Cargar Grupos Musculares únicos
        List<String> muscleGroups = new ArrayList<>();
        muscleGroups.add("Todos");
        
        Log.d("RoutineDetail_DEBUG", "Cargando grupos musculares de " + catalogExercises.size() + " ejercicios");
        
        for (Exercise ex : catalogExercises) {
            String part = ex.getBodyPart();
            Log.d("RoutineDetail_DEBUG", "Ejercicio: " + ex.getName() + " | Grupo: " + part);
            
            if (part != null && !part.trim().isEmpty()) {
                String normalizedPart = part.trim();
                if (!muscleGroups.contains(normalizedPart)) {
                    muscleGroups.add(normalizedPart);
                }
            }
        }
        
        Log.d("RoutineDetail_DEBUG", "Grupos detectados: " + muscleGroups.toString());
        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, muscleGroups);
        muscleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscle.setAdapter(muscleAdapter);

        // Lista filtrada que se mostrará en el segundo spinner
        List<Exercise> filteredExercises = new ArrayList<>(catalogExercises);
        List<String> exerciseNames = new ArrayList<>();
        
        // Adaptador para los ejercicios
        ArrayAdapter<String> exerciseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseNames);
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExercises.setAdapter(exerciseAdapter);

        // Función para actualizar el filtro
        Runnable updateFilter = () -> {
            String selectedMuscle = spinnerMuscle.getSelectedItem().toString();
            String searchText = etSearch.getText().toString().toLowerCase().trim();
            
            filteredExercises.clear();
            exerciseNames.clear();
            
            for (Exercise ex : catalogExercises) {
                boolean matchesMuscle = selectedMuscle.equals("Todos") || (ex.getBodyPart() != null && ex.getBodyPart().equals(selectedMuscle));
                boolean matchesSearch = ex.getName().toLowerCase().contains(searchText);
                
                if (matchesMuscle && matchesSearch) {
                    filteredExercises.add(ex);
                    exerciseNames.add(ex.getName());
                }
            }
            exerciseAdapter.notifyDataSetChanged();
        };

        // Escuchar cambios en el filtro de músculo
        spinnerMuscle.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateFilter.run();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Escuchar cambios en la búsqueda por texto
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateFilter.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            if (filteredExercises.isEmpty() || spinnerExercises.getSelectedItem() == null) {
                Toast.makeText(this, "No hay ejercicio seleccionado", Toast.LENGTH_SHORT).show();
                return;
            }

            String strSets = etSets.getText().toString().trim();
            String strReps = etReps.getText().toString().trim();
            String strOrder = etOrder.getText().toString().trim();

            if (strSets.isEmpty() || strReps.isEmpty() || strOrder.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos numéricos", Toast.LENGTH_SHORT).show();
                return;
            }

            // IMPORTANTE: Obtenemos el ejercicio de la lista FILTRADA
            int selectedPosition = spinnerExercises.getSelectedItemPosition();
            Exercise selectedExercise = filteredExercises.get(selectedPosition);

            int sets = Integer.parseInt(strSets);
            int reps = Integer.parseInt(strReps);
            int order = Integer.parseInt(strOrder);

            RoutineExerciseRequest request = new RoutineExerciseRequest(
                    routineId,
                    selectedExercise.getId(),
                    sets,
                    reps,
                    order
            );

            sendExerciseToBackend(request);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
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