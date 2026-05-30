package com.example.gymappfronted.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gymappfronted.Models.WorkoutLogResponse;
import java.util.List;

public class WorkoutLogAdapter extends RecyclerView.Adapter<WorkoutLogAdapter.ViewHolder> {

    public interface OnLogLongClickListener {
        void onLogLongClick(WorkoutLogResponse log, int position);
    }

    private List<WorkoutLogResponse> logList;
    private OnLogLongClickListener longClickListener;

    public WorkoutLogAdapter(List<WorkoutLogResponse> logList, OnLogLongClickListener longClickListener) {
        this.logList = logList;
        this.longClickListener = longClickListener;
    }

    public void removeLog(int position) {
        if (position >= 0 && position < logList.size()) {
            logList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, logList.size());
        }
    }

    public void addLog(WorkoutLogResponse newLog) {
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
        WorkoutLogResponse log = logList.get(position);
        holder.textView.setText("Serie " + (position + 1) + ":   " + log.getWeight() + " kg   x   " + log.getReps() + " reps");
        holder.textView.setTextColor(android.graphics.Color.WHITE);
        holder.textView.setTextSize(16);

        // Detectar pulsación larga para borrar
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLogLongClick(log, position);
            }
            return true;
        });
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
