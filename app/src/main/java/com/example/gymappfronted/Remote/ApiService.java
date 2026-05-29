package com.example.gymappfronted.Remote;
import com.example.gymappfronted.Models.AuthResponse;
import com.example.gymappfronted.Models.Exercise;
import com.example.gymappfronted.Models.LoginRequest;
import com.example.gymappfronted.Models.RegisterRequest;
import com.example.gymappfronted.Models.RoutineExerciseRequest;
import com.example.gymappfronted.Models.RoutineResponse;
import com.example.gymappfronted.Models.WorkoutLogRequest;
import com.example.gymappfronted.Models.WorkoutLogResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/exercises/") // La ruta que creamos en Django
    Call<List<Exercise>> getExercises();
    @POST("api/login/")  // POST login de usuario
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/register/") // POST registro de usuario
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("api/routines/")
    Call<List<RoutineResponse>> getUserRoutines(@Header("Authorization") String token);

    @POST("api/routines/")
    Call<RoutineResponse> createRoutine(@Header("Authorization") String token, @Body RoutineResponse newRoutine);

    @POST("api/routine-exercises/")
    Call<Void> addExerciseToRoutine(@Header("Authorization") String token, @Body RoutineExerciseRequest request);

    @POST("api/workout-logs/")
    Call<Void> saveWorkoutLog(@Header("Authorization") String token, @Body WorkoutLogRequest request);

    @GET("api/workout-logs/")
    Call<List<WorkoutLogResponse>> getWorkoutLogs(@Header("Authorization") String token);

    @DELETE("api/routines/{id}/delete/")
    Call<Void> deleteRoutine(@Header("Authorization") String token, @Path("id") int id);

    @DELETE("api/routine-exercises/{id}/delete/")
    Call<Void> deleteExercise(@Header("Authorization") String token, @Path("id") int id);

    @GET("api/progress/{exercise_id}/")
    Call<List<WorkoutLogResponse>> getProgress(@Header("Authorization") String token, @Path("exercise_id") int exerciseId
    );
}