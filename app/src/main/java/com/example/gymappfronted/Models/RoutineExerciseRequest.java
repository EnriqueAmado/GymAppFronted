package com.example.gymappfronted.Models;

public class RoutineExerciseRequest {
    private int routine;      // ID de la rutina
    private int exercise;     // ID del ejercicio del catálogo
    private int sets;
    private int reps;
    private int order;

    public RoutineExerciseRequest(int routine, int exercise, int sets, int reps, int order) {
        this.routine = routine;
        this.exercise = exercise;
        this.sets = sets;
        this.reps = reps;
        this.order = order;
    }

    public int getRoutineId() {
        return routine;
    }

    public int getExerciseId() {
        return exercise;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    public int getOrder() {
        return order;
    }
}
