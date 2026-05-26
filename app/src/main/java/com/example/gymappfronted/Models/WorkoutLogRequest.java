package com.example.gymappfronted.Models;

public class WorkoutLogRequest {
    private int routine_exercise; // ID de la relación intermedia
    private double weight;
    private int reps;

    public WorkoutLogRequest(int routine_exercise, double weight, int reps) {
        this.routine_exercise = routine_exercise;
        this.weight = weight;
        this.reps = reps;
    }

    public int getRoutineExercise() { return routine_exercise; }
    public double getWeight() { return weight; }
    public int getReps() { return reps; }
}
