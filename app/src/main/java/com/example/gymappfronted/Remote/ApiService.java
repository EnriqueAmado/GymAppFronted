package com.example.gymappfronted.Remote;
import com.example.gymappfronted.Models.AuthResponse;
import com.example.gymappfronted.Models.Exercise;
import com.example.gymappfronted.Models.LoginRequest;
import com.example.gymappfronted.Models.RegisterRequest;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/exercises/") // La ruta que creamos en Django
    Call<List<Exercise>> getExercises();
    @POST("api/login/")  // POST login de usuario
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/register/") // POST registro de usuario
    Call<AuthResponse> register(@Body RegisterRequest request);
}