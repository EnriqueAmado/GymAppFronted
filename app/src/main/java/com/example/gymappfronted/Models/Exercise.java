package com.example.gymappfronted.Models;

import com.google.gson.annotations.SerializedName;

public class Exercise {
    private int id;
    @SerializedName("nombre")
    private String name;

    @SerializedName("musculo")
    private String body_part;

    public Exercise() {}

    public Exercise(int id, String name, String body_part) {
        this.id = id;
        this.name = name;
        this.body_part = body_part;
    }

    // Getters (Importante para que Android pueda leer los datos)
    public int getId() { return id; }
    public String getName() { return name; }
    public String getBodyPart() { return body_part; }
}
