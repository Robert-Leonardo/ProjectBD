package com.example.bdsqltester.dtos;

public class User {
    private long id; // Pastikan ini long
    private String username;
    private String password;
    private String role; // Pastikan ini String

    public User(long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public long getId() { // Ini yang diperlukan
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() { // Ini yang diperlukan
        return role;
    }
    // ... (setter jika ada)
}