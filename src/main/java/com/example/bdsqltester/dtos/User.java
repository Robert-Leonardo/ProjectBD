package com.example.bdsqltester.dtos;

public class User {
    private long id;
    private String username;
    private String password;
    private String role;

    public User(long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public void setId(long id) { this.id = id; }
}