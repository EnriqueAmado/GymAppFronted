package com.example.gymappfronted.Models;

import java.util.List;

public class RoutineResponse {
    private int id;
    private String name;
    private String created_at;
    private List<RoutineExerciseResponse> exercises;

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCreatedAt() { return created_at; }
    public List<RoutineExerciseResponse> getExercises() { return exercises; }
}
