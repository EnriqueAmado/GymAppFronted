package com.example.gymappfronted;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymappfronted.Adapters.WorkoutLogAdapter;

import com.example.gymappfronted.Models.WorkoutLogRequest;
import com.example.gymappfronted.Models.WorkoutLogResponse;
import com.example.gymappfronted.Remote.ApiService;
import com.example.gymappfronted.Remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutLogActivity extends AppCompatActivity {

    private TextView tvExerciseName, tvLastSession;
    private EditText etWeight, etReps;
    private Button btnSaveSet;
    private RecyclerView rvLogs;

    private int routineExerciseId;
    private int exerciseId; // Añadido para el ID real del ejercicio
    private int targetSets = 0;
    private String exerciseName;
    private String token;
    private ApiService apiService;

    private WorkoutLogAdapter adapter;
    private List<WorkoutLogResponse> logList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_log);

        // Enlazar componentes del XML
        tvExerciseName = findViewById(R.id.tvLogExerciseName);
        tvLastSession = findViewById(R.id.tvLastSession);
        etWeight = findViewById(R.id.etLogWeight);
        etReps = findViewById(R.id.etLogReps);
        btnSaveSet = findViewById(R.id.btnSaveSet);
        rvLogs = findViewById(R.id.rvWorkoutLogs);

        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Recuperar Token (con fallback por seguridad)
        SharedPreferences sharedPreferences = getSharedPreferences("GymAppPrefs", MODE_PRIVATE);
        String rawToken = sharedPreferences.getString("token", "");
        if (rawToken.isEmpty()) {
            rawToken = sharedPreferences.getString("auth_token", "");
        }
        token = "Token " + rawToken;

        // Recibir los datos del ejercicio pulsado
        if (getIntent().hasExtra("ROUTINE_EXERCISE_ID")) {
            routineExerciseId = getIntent().getIntExtra("ROUTINE_EXERCISE_ID", -1);
            exerciseId = getIntent().getIntExtra("EXERCISE_ID", -1); // Recuperamos el ID real
            exerciseName = getIntent().getStringExtra("EXERCISE_NAME");
            tvExerciseName.setText(exerciseName);

            targetSets = getIntent().getIntExtra("TARGET_SETS", 4);

            // Cargar series ya registradas hoy para este ejercicio usando el endpoint de progreso
            fetchTodayLogs();
        }

        // Inicializar el adaptador de las series con pulsación larga para borrar
        adapter = new WorkoutLogAdapter(logList, (log, position) -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("¿Eliminar serie?")
                    .setMessage("¿Estás seguro de que quieres borrar este registro?")
                    .setPositiveButton("Eliminar", (dialog, which) -> deleteLogFromBackend(log.getId(), position))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        rvLogs.setAdapter(adapter);

        // Acción del botón para guardar la serie
        btnSaveSet.setOnClickListener(v -> {
            if (adapter.getItemCount() < targetSets) {
                saveSetToBackend();
            } else {
                Toast.makeText(this, "¡Objetivo cumplido! Ya has registrado las " + targetSets + " series programadas.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteLogFromBackend(int logId, int position) {
        apiService.deleteWorkoutLog(token, logId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    adapter.removeLog(position);
                    Toast.makeText(WorkoutLogActivity.this, "Serie eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WorkoutLogActivity.this, "Error al borrar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(WorkoutLogActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTodayLogs() {
        if (exerciseId == -1) return;

        Log.d("WorkoutLog_DEBUG", "Cargando series de hoy para: " + exerciseName + " (ID: " + exerciseId + ")");
        
        // Usamos el endpoint getProgress que ya sabemos que funciona en el Fragment
        apiService.getProgress(token, exerciseId).enqueue(new Callback<List<WorkoutLogResponse>>() {
            @Override
            public void onResponse(Call<List<WorkoutLogResponse>> call, Response<List<WorkoutLogResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<WorkoutLogResponse> allLogs = response.body();
                    Log.d("WorkoutLog_DEBUG", "Registros recibidos del endpoint de progreso: " + allLogs.size());

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    String today = sdf.format(new java.util.Date());

                    logList.clear();
                    String lastDate = "";
                    StringBuilder lastSessionInfo = new StringBuilder();
                    double max1RM = 0;

                    for (int i = allLogs.size() - 1; i >= 0; i--) {
                        WorkoutLogResponse log = allLogs.get(i);
                        
                        // Calculamos el 1RM de este log histórico
                        double current1RM = log.getWeight() * (1 + (log.getReps() * 0.0333));
                        if (current1RM > max1RM) max1RM = current1RM;

                        String createdAt = log.getCreatedAt();
                        String dateKey = (createdAt == null || createdAt.isEmpty()) ? today : createdAt;

                        if (dateKey.startsWith(today)) {
                            logList.add(0, log);
                        } else {
                            if (lastDate.isEmpty() || lastDate.equals(dateKey)) {
                                lastDate = dateKey;
                                lastSessionInfo.append(log.getWeight()).append("kg x ").append(log.getReps()).append(", ");
                            }
                        }
                    }

                    if (!lastDate.isEmpty()) {
                        String info = lastSessionInfo.toString();
                        if (info.endsWith(", ")) info = info.substring(0, info.length() - 2);
                        
                        // Añadimos el Récord Personal (PR) al texto
                        String prText = String.format(" | PR: %.1f kg", max1RM);
                        tvLastSession.setText("Última vez (" + lastDate + "): " + info + prText);
                    } else {
                        tvLastSession.setText("Primera vez que realizas este ejercicio");
                    }

                    adapter.notifyDataSetChanged();
                    Log.d("WorkoutLog_DEBUG", "Series de hoy cargadas: " + logList.size());
                } else {
                    Log.e("WorkoutLog_DEBUG", "Error en getProgress: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutLogResponse>> call, Throwable t) {
                Log.e("WorkoutLog_DEBUG", "Fallo de red en getProgress: " + t.getMessage());
            }
        });
    }

    private void saveSetToBackend() {
        String strWeight = etWeight.getText().toString().trim();
        String strReps = etReps.getText().toString().trim();

        if (strWeight.isEmpty() || strReps.isEmpty()) {
            Toast.makeText(this, "Introduce peso y repeticiones", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(strWeight);
        int reps = Integer.parseInt(strReps);

        WorkoutLogRequest logRequest = new WorkoutLogRequest(routineExerciseId, weight, reps);

        apiService.saveWorkoutLog(token, logRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(WorkoutLogActivity.this, "¡Serie guardada!", Toast.LENGTH_SHORT).show();
                    
                    // Al guardar, refrescamos para obtener el ID real del servidor
                    fetchTodayLogs();
                    
                    etWeight.setText("");
                    etReps.setText("");
                } else {
                    Toast.makeText(WorkoutLogActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(WorkoutLogActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}