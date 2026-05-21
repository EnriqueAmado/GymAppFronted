package com.example.gymappfronted.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymappfronted.R;
import com.example.gymappfronted.Models.RoutineExerciseResponse;
import com.example.gymappfronted.Models.RoutineResponse;

import java.util.List;

public class RoutinesAdapter extends RecyclerView.Adapter<RoutinesAdapter.RoutineViewHolder> {

    private final List<RoutineResponse> routineList;

    public RoutinesAdapter(List<RoutineResponse> routineList) {
        this.routineList = routineList;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        RoutineResponse routine = routineList.get(position);
        holder.tvName.setText(routine.getName());
        holder.tvDate.setText("Creada el: " + routine.getCreatedAt());

        // Construimos el bloque de texto con todos sus ejercicios ordenados
        StringBuilder exercisesBuilder = new StringBuilder();
        if (routine.getExercises() != null && !routine.getExercises().isEmpty()) {
            for (RoutineExerciseResponse ex : routine.getExercises()) {
                exercisesBuilder.append("• ")
                        .append(ex.getExerciseName())
                        .append("  —  ")
                        .append(ex.getSets())
                        .append("x")
                        .append(ex.getReps())
                        .append("\n");
            }
            // Quitamos el último salto de línea
            exercisesBuilder.setLength(exercisesBuilder.length() - 1);
        } else {
            exercisesBuilder.append("Esta rutina no tiene ejercicios asignados todavía.");
        }

        holder.tvExercises.setText(exercisesBuilder.toString());
    }

    @Override
    public int getItemCount() {
        return routineList == null ? 0 : routineList.size();
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvExercises;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRoutineItemName);
            tvDate = itemView.findViewById(R.id.tvRoutineItemDate);
            tvExercises = itemView.findViewById(R.id.tvRoutineItemExercises);
        }
    }
}
