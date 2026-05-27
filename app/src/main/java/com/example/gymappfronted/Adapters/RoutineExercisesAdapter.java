package com.example.gymappfronted.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.example.gymappfronted.Models.RoutineExerciseResponse;
import com.example.gymappfronted.R;

public class RoutineExercisesAdapter extends RecyclerView.Adapter<RoutineExercisesAdapter.ViewHolder> {

    private List<RoutineExerciseResponse> exerciseList;
    private OnExerciseClickListener clickListener;

    public interface OnExerciseClickListener {
        void onExerciseClick(RoutineExerciseResponse exercise);
        void onDeleteClick(RoutineExerciseResponse exercise, int position);
    }

    public RoutineExercisesAdapter(List<RoutineExerciseResponse> exerciseList, OnExerciseClickListener clickListener) {
        this.exerciseList = exerciseList;
        this.clickListener = clickListener;
    }

    public void setExercises(List<RoutineExerciseResponse> newList) {
        this.exerciseList = newList;
        notifyDataSetChanged(); // Refresca la lista en pantalla
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoutineExerciseResponse item = exerciseList.get(position);

        holder.tvName.setText(item.getExerciseName());
        holder.tvOrder.setText("Orden: " + item.getOrder());
        holder.tvSetsReps.setText(item.getSets() + " x " + item.getReps());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onExerciseClick(item);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDeleteClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList == null ? 0 : exerciseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvOrder, tvSetsReps;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvOrder = itemView.findViewById(R.id.tvExerciseOrder);
            tvSetsReps = itemView.findViewById(R.id.tvSetsReps);
            btnDelete = itemView.findViewById(R.id.btnDeleteExercise);
        }
    }
}