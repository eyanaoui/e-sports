package com.esports.models;

public class Message {
    private int id;
    private int sujetId; // This links the comment to the topic
    private String contenu;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSujetId() { return sujetId; }
    public void setSujetId(int sujetId) { this.sujetId = sujetId; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
}