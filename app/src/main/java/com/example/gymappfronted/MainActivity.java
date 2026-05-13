package com.example.gymappfronted;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gymappfronted.Models.Exercise;
import com.example.gymappfronted.Remote.ApiService;
import com.example.gymappfronted.Remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GYM_PROGRESS_LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

                    for (Exercise e : exercises) {
                        Log.d(TAG, "Ejercicio: " + e.getName());
                    }
                } else {
                    Log.e(TAG, "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                Log.e(TAG, "Fallo de conexión: " + t.getMessage());
            }
        });
    }
}