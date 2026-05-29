package com.example.gymappfronted.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressFragment extends Fragment {

    private Spinner spinnerExercises;
    private LineChart lineChart;
    private TextView tvNoData;
    private List<Exercise> catalogExercises = new ArrayList<>();
    private String token;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        spinnerExercises = view.findViewById(R.id.spinnerExercisesProgress);
        lineChart = view.findViewById(R.id.lineChart);
        tvNoData = view.findViewById(R.id.tvNoData);

        apiService = RetrofitClient.getApiService();

        SharedPreferences prefs = getActivity().getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE);
        String rawToken = prefs.getString("auth_token", "");
        if (rawToken.isEmpty()) {
            rawToken = prefs.getString("token", "");
        }
        Log.d("GymProgress_DEBUG", "--- TOKEN RECUPERADO DE PREFS: [" + rawToken + "]");

        token = "Token " + rawToken;

        setupChartStyle();
        loadExerciseCatalog();

        spinnerExercises.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("GymProgress_DEBUG", "¡Spinner pulsado! Posición: " + position);

                if (catalogExercises != null && !catalogExercises.isEmpty() && position < catalogExercises.size()) {
                    Exercise selected = catalogExercises.get(position);
                    Log.d("GymProgress_DEBUG", "Pidiendo datos para: " + selected.getName() + " (ID: " + selected.getId() + ")");
                    fetchProgressFromServer(selected.getId(), selected.getName());
                } else {
                    Log.d("GymProgress_DEBUG", "La lista de ejercicios del catálogo está vacía o es nula");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void setupChartStyle() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setNoDataText("Selecciona un ejercicio para ver tu progreso...");
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getLegend().setTextColor(Color.WHITE);
        lineChart.getXAxis().setGranularity(1f);
    }

    private void loadExerciseCatalog() {
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
                }
            }

            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Error al conectar con la API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProgressFromServer(int exerciseId, String exerciseName) {
        Log.d("GymProgress_DEBUG", ">>> Enviando petición HTTP a Django para el ejercicio ID: " + exerciseId);
        Log.d("GymProgress_DEBUG", "Using Token: " + token);

        apiService.getProgress(token, exerciseId).enqueue(new Callback<List<WorkoutLogResponse>>() {
            @Override
            public void onResponse(Call<List<WorkoutLogResponse>> call, Response<List<WorkoutLogResponse>> response) {
                Log.d("GymProgress_DEBUG", "<<< Respuesta del servidor recibida. Código de estado HTTP: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<WorkoutLogResponse> logs = response.body();
                    Log.d("GymProgress_DEBUG", "¡Éxito! Registros devueltos por Django: " + logs.size());
                    updateChartWithData(logs, exerciseName); // <--- Llamada correcta
                } else {
                    Log.e("GymProgress_DEBUG", "Error en el cuerpo de la respuesta. Código HTTP: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("GymProgress_DEBUG", "Detalle del error del servidor: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e("GymProgress_DEBUG", "No se pudo leer el cuerpo del error", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutLogResponse>> call, Throwable t) {
                Log.e("GymProgress_DEBUG", "FALLO CRÍTICO DE RED (Retrofit no llega a Django): " + t.getMessage(), t);
            }
        }); // <--- Aquí se cierra correctamente la llamada anónima de Retrofit
    }

    // MÉTODOS DE LA CLASE PRINCIPAL (FUERA DEL CALLBACK)
    private void updateChartWithData(List<WorkoutLogResponse> logs, String exerciseName) {
        if (logs == null || logs.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }

        lineChart.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);

        List<Entry> entries = new ArrayList<>();
        final List<String> fechas = new ArrayList<>();

        for (int i = 0; i < logs.size(); i++) {
            WorkoutLogResponse log = logs.get(i);

            float weight = (float) log.getWeight();
            entries.add(new Entry(i, weight));

            String fechaLog = log.getCreatedAt();
            if (fechaLog == null || fechaLog.isEmpty()) {
                fechas.add("S/F"); // "Sin Fecha" si viniera nulo, para que no rompa
            } else {
                if (fechaLog.length() > 10) {
                    fechaLog = fechaLog.substring(0, 10);
                }
                fechas.add(log.getCreatedAt());
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Progreso en " + exerciseName);
        dataSet.setColor(Color.parseColor("#00E676"));
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        // Diseño de los números encima de los puntos
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + " kg"; // Añade la unidad "kg" directamente en la gráfica
            }
        });

        // CONFIGURACIÓN DEL EJE X (FECHAS)
        com.github.mikephil.charting.components.XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f); //  Rota las fechas -45 grados para que no se pisen entre ellas
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < fechas.size()) {
                    return fechas.get(index);
                }
                return "";
            }
        });

        //Interactividad total para ver las repeticiones al pulsar el punto
        lineChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                int index = Math.round(e.getX());
                if (index >= 0 && index < logs.size()) {
                    WorkoutLogResponse selectedLog = logs.get(index);

                    // Construimos el mensaje completo con la fecha, peso y repeticiones reales
                    String info = "📅 " + fechas.get(index) + "\n" +
                            "💪 Peso: " + selectedLog.getWeight() + " kg\n" +
                            "🔁 Repeticiones: " + selectedLog.getReps();

                    Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected() {}
        });

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.setExtraBottomOffset(20f);
        lineChart.animateX(800);
        lineChart.invalidate();
    }
}