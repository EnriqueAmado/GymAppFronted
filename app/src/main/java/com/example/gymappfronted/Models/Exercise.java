package com.example.gymappfronted.Models;
public class Exercise {
    private int id;
    private String name;
    private String body_part;

    // Getters (Importante para que Android pueda leer los datos)
    public int getId() { return id; }
    public String getName() { return name; }
    public String getBodyPart() { return body_part; }
}
