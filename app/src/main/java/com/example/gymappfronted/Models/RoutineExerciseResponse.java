package com.example.gymappfronted.Models;

public class RoutineExerciseResponse {
    private int id;
    private String exercise_name;
    private String body_part;
    private int sets;
    private int reps;
    private int order;

    // Getters
    public int getId() { return id; }
    public String getExerciseName() { return exercise_name; }
    public String getBodyPart() { return body_part; }
    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public int getOrder() { return order; }
}