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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiService service = RetrofitClient.getApiService();
        service.getAllExercises().enqueue(new retrofit2.Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, retrofit2.Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Exercise e : response.body()) {
                        Log.d("API_TEST", "Ejercicio recibido: " + e.getName());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                Log.e("API_TEST", "Error de conexión: " + t.getMessage());
            }
        });
    }
}