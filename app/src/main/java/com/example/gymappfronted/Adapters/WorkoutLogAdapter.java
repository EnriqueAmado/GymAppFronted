package com.example.gymappfronted.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gymappfronted.Models.WorkoutLogRequest;
import java.util.List;

public class WorkoutLogAdapter extends RecyclerView.Adapter<WorkoutLogAdapter.ViewHolder> {

    private List<WorkoutLogRequest> logList;

    public WorkoutLogAdapter(List<WorkoutLogRequest> logList) {
        this.logList = logList;
    }

    public void addLog(WorkoutLogRequest newLog) {
        this.logList.add(newLog);
        notifyItemInserted(logList.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutLogRequest log = logList.get(position);
        // Pintamos el resumen de la serie (ej: "Serie 1: 80.0 kg x 12 reps")
        holder.textView.setText("Serie " + (position + 1) + ":   " + log.getWeight() + " kg   x   " + log.getReps() + " reps");
        holder.textView.setTextColor(android.graphics.Color.WHITE);
        holder.textView.setTextSize(16);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
