package com.example.gymappfronted.Models;

import com.google.gson.annotations.SerializedName;

public class Exercise {
    private int id; // Si se llama igual en el JSON e igual en Java, no hace falta @SerializedName

    @SerializedName("name") // Coincide con el campo de Django
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("body_part") // Coincide con el campo de Django
    private String bodyPart;

    // Constructor vacío obligatorio para Retrofit
    public Exercise() {}

    public Exercise(int id, String name, String description, String bodyPart) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.bodyPart = bodyPart;
    }

    // --- GETTERS ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBodyPart() { return bodyPart; }
}
