package com.example.gymappfronted.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymappfronted.R;
import com.example.gymappfronted.Models.RoutineExerciseResponse;
import com.example.gymappfronted.Models.RoutineResponse;
import com.example.gymappfronted.Remote.RetrofitClient;
import com.example.gymappfronted.RoutineDetailActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutinesAdapter extends RecyclerView.Adapter<RoutinesAdapter.RoutineViewHolder> {

    private final List<RoutineResponse> routineList;

    private Context context;

    public RoutinesAdapter(List<RoutineResponse> routineList) {
        this.routineList = routineList;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RoutineDetailActivity.class);
            // Le pasamos el ID y el Nombre a la nueva pantalla
            intent.putExtra("ROUTINE_ID", routine.getId());
            intent.putExtra("ROUTINE_NAME", routine.getName());

            // Arrancamos la actividad
            v.getContext().startActivity(intent);
        });

        holder.btnDeleteRoutine.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("¿Eliminar rutina?")
                    .setMessage("Se borrará la rutina \"" + routine.getName() + "\" y todos sus ejercicios asociados de forma permanente.")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        deleteRoutineFromBackend(routine.getId(), position);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return routineList == null ? 0 : routineList.size();
    }

    // MÉTODO QUE CONECTA CON DJANGO PARA BORRAR
    private void deleteRoutineFromBackend(int routineId, int position) {
        // Sacamos el token
        SharedPreferences sharedPreferences = context.getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        String token = "Token " + sharedPreferences.getString("token", "");

        // Llama a tu cliente Retrofit
        RetrofitClient.getApiService().deleteRoutine(token, routineId)
            .enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        routineList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, routineList.size());
                        Toast.makeText(context, "Rutina eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error al eliminar en el servidor", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    public static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvExercises;
        ImageView btnDeleteRoutine;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRoutineItemName);
            tvDate = itemView.findViewById(R.id.tvRoutineItemDate);
            tvExercises = itemView.findViewById(R.id.tvRoutineItemExercises);
            btnDeleteRoutine = itemView.findViewById(R.id.btn_delete_routine);
        }
    }
}
