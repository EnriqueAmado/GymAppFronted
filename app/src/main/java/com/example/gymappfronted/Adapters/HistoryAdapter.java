package com.example.gymappfronted.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gymappfronted.Models.WorkoutLogResponse;
import com.example.gymappfronted.R;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class RoutineHeader {
        public String name;
        public RoutineHeader(String name) { this.name = name; }
    }

    private static final int TYPE_ROUTINE = 0;
    private static final int TYPE_MUSCLE = 1;
    private static final int TYPE_ITEM = 2;

    private List<Object> items;

    public HistoryAdapter(List<Object> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof RoutineHeader) return TYPE_ROUTINE;
        if (items.get(position) instanceof String) return TYPE_MUSCLE;
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ROUTINE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new RoutineViewHolder(view);
        } else if (viewType == TYPE_MUSCLE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new MuscleViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_ROUTINE) {
            RoutineViewHolder h = (RoutineViewHolder) holder;
            h.tv.setText(((RoutineHeader) items.get(position)).name.toUpperCase());
            h.tv.setTextColor(android.graphics.Color.WHITE);
            h.tv.setTextSize(22);
            h.tv.setTypeface(null, android.graphics.Typeface.BOLD);
            h.tv.setPadding(0, 40, 0, 10);
        } else if (type == TYPE_MUSCLE) {
            MuscleViewHolder h = (MuscleViewHolder) holder;
            h.tv.setText("  ▸ " + items.get(position));
            h.tv.setTextColor(android.graphics.Color.parseColor("#00E676"));
            h.tv.setTextSize(16);
            h.tv.setPadding(0, 10, 0, 5);
        } else {
            ItemViewHolder h = (ItemViewHolder) holder;
            WorkoutLogResponse log = (WorkoutLogResponse) items.get(position);
            h.tvTitle.setText("      " + log.getExerciseName());
            h.tvSubtitle.setText("        " + log.getWeight() + " kg x " + log.getReps() + " reps");
            h.tvTitle.setTextColor(android.graphics.Color.WHITE);
            h.tvSubtitle.setTextColor(android.graphics.Color.LTGRAY);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        public RoutineViewHolder(@NonNull View itemView) { super(itemView); tv = itemView.findViewById(android.R.id.text1); }
    }

    public static class MuscleViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        public MuscleViewHolder(@NonNull View itemView) { super(itemView); tv = itemView.findViewById(android.R.id.text1); }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(android.R.id.text1);
            tvSubtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}
