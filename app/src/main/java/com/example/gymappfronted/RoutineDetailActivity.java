package com.example.gymappfronted;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RoutineDetailActivity extends AppCompatActivity {

    private TextView tvRoutineName;
    private RecyclerView rvExercises;
    private FloatingActionButton fabAddExercise;
    private int routineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        // Enlazar componentes de la interfaz
        tvRoutineName = findViewById(R.id.tvRoutineName);
        rvExercises = findViewById(R.id.rvExercisesInRoutine);
        fabAddExercise = findViewById(R.id.fabAddExercise);

        rvExercises.setLayoutManager(new LinearLayoutManager(this));

        // Recibir el ID y el Nombre de la rutina pulsada
        if (getIntent().hasExtra("ROUTINE_ID")) {
            routineId = getIntent().getIntExtra("ROUTINE_ID", -1);
            String routineName = getIntent().getStringExtra("ROUTINE_NAME");

            tvRoutineName.setText(routineName);
        }

        // Acción del botón flotante para añadir ejercicios
        fabAddExercise.setOnClickListener(v -> {

        });
    }
}
