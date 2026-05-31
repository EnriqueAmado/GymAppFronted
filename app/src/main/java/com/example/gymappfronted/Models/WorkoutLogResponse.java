package com.example.gymappfronted.Models;

import com.google.gson.annotations.SerializedName;

public class WorkoutLogResponse {
    private int id;
    private double weight;
    private int reps;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("exercise_name")
    private String exerciseName;
    @SerializedName("body_part")
    private String bodyPart;
    @SerializedName("routine_name")
    private String routineName;

    public int getId() { return id; }
    public double getWeight() { return weight; }
    public int getReps() { return reps; }
    public String getCreatedAt() { return createdAt; }
    public String getExerciseName() { return exerciseName; }
    public String getBodyPart() { return bodyPart; }
    public String getRoutineName() { return routineName; }
}
