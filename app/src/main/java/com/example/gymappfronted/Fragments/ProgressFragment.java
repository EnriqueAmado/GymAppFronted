package com.example.gymappfronted.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gymappfronted.Models.Exercise;
import com.example.gymappfronted.Models.WorkoutLogResponse;
import com.example.gymappfronted.R;
import com.example.gymappfronted.Remote.ApiService;
import com.example.gymappfronted.Remote.RetrofitClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressFragment extends Fragment {

    private Spinner spinnerExercises;
    private LineChart lineChart;
    private TextView tvNoData;
    private List<WorkoutLogResponse> allLogs = new ArrayList<>();
    private List<Exercise> catalogExercises = new ArrayList<>();
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        spinnerExercises = view.findViewById(R.id.spinnerExercisesProgress);
        lineChart = view.findViewById(R.id.lineChart);
        tvNoData = view.findViewById(R.id.tvNoData);

        SharedPreferences prefs = getActivity().getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        token = "Token " + prefs.getString("token", "");

        setupChart();
        loadCatalogAndLogs();

        spinnerExercises.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedExercise = catalogExercises.get(position).getName();
                updateChart(selectedExercise);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setNoDataText("Cargando datos...");
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getLegend().setTextColor(Color.WHITE);
    }

    private void loadCatalogAndLogs() {
        ApiService apiService = RetrofitClient.getApiService();

        // 1. Cargar catálogo de ejercicios
        apiService.getExercises().enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    catalogExercises = response.body();
                    List<String> names = new ArrayList<>();
                    for (Exercise ex : catalogExercises) {
                        names.add(ex.getName());
                    }
                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerExercises.setAdapter(adapter);
                    }
                    
                    // 2. Una vez tenemos el catálogo, cargamos los logs
                    fetchLogs();
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Error al cargar ejercicios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLogs() {
        RetrofitClient.getApiService().getWorkoutLogs(token).enqueue(new Callback<List<WorkoutLogResponse>>() {
            @Override
            public void onResponse(Call<List<WorkoutLogResponse>> call, Response<List<WorkoutLogResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allLogs = response.body();
                    if (spinnerExercises.getSelectedItem() != null) {
                        updateChart(spinnerExercises.getSelectedItem().toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutLogResponse>> call, Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Error al cargar logs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChart(String exerciseName) {
        List<Entry> entries = new ArrayList<>();
        int x = 0;
        for (WorkoutLogResponse log : allLogs) {
            if (log.getExerciseName() != null && log.getExerciseName().equals(exerciseName)) {
                entries.add(new Entry(x++, (float) log.getWeight()));
            }
        }

        if (entries.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            lineChart.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);

            LineDataSet dataSet = new LineDataSet(entries, "Peso levantado (kg) en " + exerciseName);
            dataSet.setColor(Color.GREEN);
            dataSet.setCircleColor(Color.WHITE);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(10f);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.invalidate(); // Refrescar
        }
    }
}