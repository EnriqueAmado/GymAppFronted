package com.example.gymappfronted.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gymappfronted.Models.Exercise;
import com.example.gymappfronted.R;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exerciseList;

    // Constructor donde le pasamos los datos que vienen de Django
    public ExerciseAdapter(List<Exercise> exerciseList) {

        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        // Lógica donde asignamos los datos del objeto a la vista
        Exercise exercise = exerciseList.get(position);

        // Rellenamos los textos con los datos reales del objeto
        holder.tvExerciseName.setText(exercise.getName());
        holder.tvDescription.setText(exercise.getDescription());
        holder.tvBodyPart.setText(exercise.getBodyPart());
    }

    @Override
    public int getItemCount() {

        return exerciseList.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName, tvDescription, tvBodyPart;
        public ExerciseViewHolder(@NonNull View itemView){
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvBodyPart = itemView.findViewById(R.id.tvBodyPart);
        }
    }
}
