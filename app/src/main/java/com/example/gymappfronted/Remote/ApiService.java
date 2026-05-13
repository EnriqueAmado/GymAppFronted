package com.example.gymappfronted.Remote;
import com.example.gymappfronted.Models.Exercise;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/exercises/") // La ruta que creamos en Django
    Call<List<Exercise>> getExercises();
}