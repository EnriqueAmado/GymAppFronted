package com.example.gymappfronted;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymappfronted.Adapters.ExerciseAdapter;
import com.example.gymappfronted.Models.Exercise;
import com.example.gymappfronted.Remote.ApiService;
import com.example.gymappfronted.Remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GYM_PROGRESS_LOG";

    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rvExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configuración de los bordes de pantalla (UI)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Lanzamos lógica de red
        fetchExercises();
    }

    private void fetchExercises() {
        //Instanciamos el servicio
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        //Preparamos la llamada
        Call<List<Exercise>> call = apiService.getExercises();

        //Ejecución
        call.enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Exercise> exercises = response.body();
                    Log.d(TAG, "¡Éxito! Recibidos " + exercises.size() + " ejercicios.");

                    adapter = new ExerciseAdapter(exercises);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e(TAG, "Error en respuesta: " + response.code());
                    // Opcional para el futuro: Mostrar un aviso visual al usuario (Toast) de que falló la carga
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                Log.e(TAG, "Fallo de conexión: " + t.getMessage());
            }
        });
    }
}