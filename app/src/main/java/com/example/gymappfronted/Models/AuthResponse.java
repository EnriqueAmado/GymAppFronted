package com.example.gymappfronted.Models;

public class AuthResponse {
    private String token;
    private String username;
    private String error;

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getError() { return error; }
}
