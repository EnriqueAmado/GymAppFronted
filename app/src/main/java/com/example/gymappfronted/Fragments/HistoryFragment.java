package com.example.gymappfronted.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymappfronted.Adapters.HistoryAdapter;
import com.example.gymappfronted.Models.WorkoutLogResponse;
import com.example.gymappfronted.R;
import com.example.gymappfronted.Remote.ApiService;
import com.example.gymappfronted.Remote.RetrofitClient;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView tvDate, tvNoData;
    private RecyclerView rvLogs;
    private HistoryAdapter adapter;
    private List<WorkoutLogResponse> allLogs = new ArrayList<>();
    private List<Object> groupedItems = new ArrayList<>();
    private String token;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tvDate = view.findViewById(R.id.tvHistoryDate);
        tvNoData = view.findViewById(R.id.tvNoHistoryData);
        rvLogs = view.findViewById(R.id.rvHistoryLogs);

        rvLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(groupedItems);
        rvLogs.setAdapter(adapter);

        apiService = RetrofitClient.getApiService();

        SharedPreferences prefs = getActivity().getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        String rawToken = prefs.getString("auth_token", "");
        if (rawToken.isEmpty()) {
            rawToken = prefs.getString("token", "");
        }
        token = "Token " + rawToken;

        loadAllLogs();

        // Escuchar cambios de fecha en el nuevo calendario
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            // El objeto 'date' de esta librería ya devuelve el mes correcto
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                date.getYear(), date.getMonth(), date.getDay());
            filterAndGroupLogs(selectedDate);
        });

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        filterAndGroupLogs(today);

        return view;
    }

    private void loadAllLogs() {
        apiService.getWorkoutLogs(token).enqueue(new Callback<List<WorkoutLogResponse>>() {
            @Override
            public void onResponse(Call<List<WorkoutLogResponse>> call, Response<List<WorkoutLogResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allLogs = response.body();
                    highlightTrainingDays();
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    filterAndGroupLogs(today);
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutLogResponse>> call, Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Error al cargar historial", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void highlightTrainingDays() {
        HashSet<CalendarDay> days = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (WorkoutLogResponse log : allLogs) {
            try {
                if (log.getCreatedAt() != null) {
                    Date date = sdf.parse(log.getCreatedAt());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    
                    // IMPORTANTE: CalendarDay de esta librería usa meses de 0 a 11, 
                    // igual que java.util.Calendar.
                    CalendarDay day = CalendarDay.from(
                        cal.get(Calendar.YEAR), 
                        cal.get(Calendar.MONTH) + 1, // La librería espera 1-12
                        cal.get(Calendar.DAY_OF_MONTH)
                    );
                    days.add(day);
                }
            } catch (Exception e) {
                android.util.Log.e("History_DEBUG", "Error parseando fecha: " + log.getCreatedAt());
            }
        }
        calendarView.addDecorator(new EventDecorator(android.graphics.Color.parseColor("#00E676"), days));
    }

    private void filterAndGroupLogs(String date) {
        groupedItems.clear();
        List<WorkoutLogResponse> dayLogs = new ArrayList<>();

        for (WorkoutLogResponse log : allLogs) {
            if (log.getCreatedAt() != null && log.getCreatedAt().startsWith(date)) {
                dayLogs.add(log);
            }
        }

        if (dayLogs.isEmpty()) {
            tvDate.setText("Sin entrenamientos el " + date);
            tvNoData.setVisibility(View.VISIBLE);
            rvLogs.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvLogs.setVisibility(View.VISIBLE);
            tvDate.setText("Actividad del " + date);

            java.util.Map<String, java.util.Map<String, List<WorkoutLogResponse>>> hierarchy = new java.util.TreeMap<>();

            for (WorkoutLogResponse log : dayLogs) {
                String rName = (log.getRoutineName() == null || log.getRoutineName().isEmpty()) ? "Entrenamiento Libre" : log.getRoutineName();
                String mGroup = (log.getBodyPart() == null || log.getBodyPart().isEmpty()) ? "Otros" : log.getBodyPart();

                if (!hierarchy.containsKey(rName)) {
                    hierarchy.put(rName, new java.util.TreeMap<>());
                }
                
                java.util.Map<String, List<WorkoutLogResponse>> muscles = hierarchy.get(rName);
                if (muscles != null) {
                    if (!muscles.containsKey(mGroup)) {
                        muscles.put(mGroup, new ArrayList<>());
                    }
                    muscles.get(mGroup).add(log);
                }
            }

            for (java.util.Map.Entry<String, java.util.Map<String, List<WorkoutLogResponse>>> routineEntry : hierarchy.entrySet()) {
                groupedItems.add(new HistoryAdapter.RoutineHeader(routineEntry.getKey()));
                for (java.util.Map.Entry<String, List<WorkoutLogResponse>> muscleEntry : routineEntry.getValue().entrySet()) {
                    groupedItems.add(muscleEntry.getKey());
                    groupedItems.addAll(muscleEntry.getValue());
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Clase para pintar los puntos verdes
    private static class EventDecorator implements DayViewDecorator {
        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, HashSet<CalendarDay> dates) {
            this.color = color;
            this.dates = dates;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(8, color)); // Pone un punto debajo del día
        }
    }
}
