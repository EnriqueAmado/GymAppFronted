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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvDate, tvNoData;
    private RecyclerView rvLogs;
    private HistoryAdapter adapter;
    private List<WorkoutLogResponse> allLogs = new ArrayList<>();
    private List<WorkoutLogResponse> filteredLogs = new ArrayList<>();
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
        adapter = new HistoryAdapter(filteredLogs);
        rvLogs.setAdapter(adapter);

        apiService = RetrofitClient.getApiService();

        SharedPreferences prefs = getActivity().getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        String rawToken = prefs.getString("auth_token", "");
        if (rawToken.isEmpty()) {
            rawToken = prefs.getString("token", "");
        }
        token = "Token " + rawToken;

        loadAllLogs();

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // Month is 0-indexed in CalendarView
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvDate.setText("Entrenamientos del " + selectedDate);
            filterLogsByDate(selectedDate);
        });

        // Set current date as default
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvDate.setText("Entrenamientos del " + today);

        return view;
    }

    private void loadAllLogs() {
        android.util.Log.d("History_DEBUG", "Cargando todos los logs del servidor...");
        apiService.getWorkoutLogs(token).enqueue(new Callback<List<WorkoutLogResponse>>() {
            @Override
            public void onResponse(Call<List<WorkoutLogResponse>> call, Response<List<WorkoutLogResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allLogs = response.body();
                    android.util.Log.d("History_DEBUG", "Logs recibidos: " + allLogs.size());
                    
                    // Por defecto mostramos hoy
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    filterLogsByDate(today);
                } else {
                    android.util.Log.e("History_DEBUG", "Error en servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutLogResponse>> call, Throwable t) {
                android.util.Log.e("History_DEBUG", "Fallo de red: " + t.getMessage());
                if (getContext() != null) Toast.makeText(getContext(), "Error al cargar historial", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterLogsByDate(String date) {
        android.util.Log.d("History_DEBUG", "Filtrando logs para la fecha: " + date);
        filteredLogs.clear();
        for (WorkoutLogResponse log : allLogs) {
            String logDate = log.getCreatedAt();
            if (logDate != null && logDate.startsWith(date)) {
                filteredLogs.add(log);
            }
        }

        android.util.Log.d("History_DEBUG", "Encontrados: " + filteredLogs.size() + " registros.");

        if (filteredLogs.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvLogs.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvLogs.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }
}
